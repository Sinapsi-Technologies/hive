package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import io.sinapsi.hive.cli.c4.model.ArchitectureRelation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Infers the relations that make Hive's hexagonal flow obvious without drawing a noisy complete
 * graph:
 *
 * <pre>
 * Inbound Adapter -&gt; Input Port -&gt; Application Service -&gt; Domain / Output Port &lt;- Outbound Adapter
 * </pre>
 *
 * Links along the application seam (input port &rarr; service, outbound adapter &rarr; output port)
 * use precise name matching after stripping role/tech words; the rest use shared-domain-token
 * overlap, which naturally keeps a {@code TodoController} away from {@code PricingUseCase}s.
 */
public final class RelationHeuristics {
    private static final Set<String> STOPWORDS = Set.of(
            // roles / suffixes
            "controller", "resource", "endpoint", "rest", "restcontroller", "api", "adapter",
            "usecase", "use", "case", "port", "repository", "repo", "service", "command",
            "factory", "configuration", "config", "impl", "default", "dto", "request", "response",
            "handler", "listener", "provider",
            // technologies
            "inmemory", "memory", "mongo", "mongodb", "http", "https", "jpa", "jdbc", "jms",
            "kafka", "rabbit", "rabbitmq", "sql", "postgres", "postgresql", "mysql", "redis",
            "grpc", "file", "filesystem", "stub", "fake", "mock", "web", "spring", "client",
            "gateway", "db", "s3", "local", "remote",
            // verbs
            "create", "add", "update", "delete", "remove", "get", "find", "complete", "save",
            "load", "fetch", "read", "write", "handle", "process", "register", "send", "publish",
            "subscribe", "cancel", "apply", "make", "build", "set", "assign", "change", "move",
            "open", "close", "start", "stop", "run", "in", "out");

    private static final List<String> TECH_PREFIXES = List.of(
            "InMemory", "MongoDb", "Mongo", "Https", "Http", "Jpa", "Jdbc", "Jms", "Kafka",
            "RabbitMq", "Rabbit", "Postgresql", "Postgres", "MySql", "Mysql", "Redis", "Grpc",
            "FileSystem", "File", "Stub", "Fake", "Mock", "Spring", "Default", "Rest", "Web",
            "S3", "Local", "Remote", "Sql");

    public List<ArchitectureRelation> build(List<ArchitectureElement> elements) {
        Map<ArchitectureElementType, List<ArchitectureElement>> byType = elements.stream()
                .collect(Collectors.groupingBy(ArchitectureElement::type));

        List<ArchitectureElement> inbound = byType.getOrDefault(ArchitectureElementType.INBOUND_ADAPTER, List.of());
        List<ArchitectureElement> inputPorts = byType.getOrDefault(ArchitectureElementType.INPUT_PORT, List.of());
        List<ArchitectureElement> services = byType.getOrDefault(ArchitectureElementType.APPLICATION_SERVICE, List.of());
        List<ArchitectureElement> aggregates = byType.getOrDefault(ArchitectureElementType.DOMAIN_AGGREGATE, List.of());
        List<ArchitectureElement> domainServices = byType.getOrDefault(ArchitectureElementType.DOMAIN_SERVICE, List.of());
        List<ArchitectureElement> domainModels = byType.getOrDefault(ArchitectureElementType.DOMAIN_MODEL, List.of());
        List<ArchitectureElement> outputPorts = byType.getOrDefault(ArchitectureElementType.OUTPUT_PORT, List.of());
        List<ArchitectureElement> outbound = byType.getOrDefault(ArchitectureElementType.OUTBOUND_ADAPTER, List.of());
        List<ArchitectureElement> configs = byType.getOrDefault(ArchitectureElementType.CONFIGURATION, List.of());

        List<ArchitectureRelation> relations = new ArrayList<>();
        Set<String> seen = new HashSet<>();

        // Inbound adapter -> input port: same bounded context (shared domain token).
        for (ArchitectureElement a : inbound) {
            for (ArchitectureElement p : inputPorts) {
                if (tokensOverlap(a, p)) {
                    add(relations, seen, a, p, "calls");
                }
            }
        }
        // Input port -> application service: precise (CreateTodo == CreateTodo).
        for (ArchitectureElement p : inputPorts) {
            for (ArchitectureElement s : services) {
                if (portImplementedBy(p, s)) {
                    add(relations, seen, p, s, "implemented by");
                }
            }
        }
        // Application service -> domain (aggregate first, then domain services / models).
        for (ArchitectureElement s : services) {
            for (ArchitectureElement d : aggregates) {
                if (tokensOverlap(s, d)) {
                    add(relations, seen, s, d, "uses");
                }
            }
            for (ArchitectureElement d : domainServices) {
                if (tokensOverlap(s, d)) {
                    add(relations, seen, s, d, "uses");
                }
            }
            for (ArchitectureElement d : domainModels) {
                if (tokensOverlap(s, d)) {
                    add(relations, seen, s, d, "uses");
                }
            }
        }
        // Application service -> output port.
        for (ArchitectureElement s : services) {
            for (ArchitectureElement p : outputPorts) {
                if (tokensOverlap(s, p)) {
                    add(relations, seen, s, p, "uses");
                }
            }
        }
        // Outbound adapter -> output port: precise, with a conservative token fallback.
        for (ArchitectureElement a : outbound) {
            boolean matched = false;
            for (ArchitectureElement p : outputPorts) {
                if (adapterImplementsPort(a, p)) {
                    add(relations, seen, a, p, "implements");
                    matched = true;
                }
            }
            if (!matched) {
                for (ArchitectureElement p : outputPorts) {
                    if (tokensOverlap(a, p)) {
                        add(relations, seen, a, p, "implements");
                    }
                }
            }
        }
        // Configuration -> application service / outbound adapter.
        for (ArchitectureElement c : configs) {
            for (ArchitectureElement s : services) {
                if (tokensOverlap(c, s)) {
                    add(relations, seen, c, s, "wires");
                }
            }
            for (ArchitectureElement a : outbound) {
                if (tokensOverlap(c, a)) {
                    add(relations, seen, c, a, "wires");
                }
            }
        }

        sort(relations, elements);
        return relations;
    }

    private void add(
            List<ArchitectureRelation> relations,
            Set<String> seen,
            ArchitectureElement source,
            ArchitectureElement target,
            String description
    ) {
        if (source.id().equals(target.id())) {
            return;
        }
        if (seen.add(source.id() + "->" + target.id())) {
            relations.add(new ArchitectureRelation(source.id(), target.id(), description));
        }
    }

    private void sort(List<ArchitectureRelation> relations, List<ArchitectureElement> elements) {
        Map<String, ArchitectureElement> byId = elements.stream()
                .collect(Collectors.toMap(ArchitectureElement::id, Function.identity()));
        Comparator<ArchitectureRelation> comparator = Comparator
                .comparingInt((ArchitectureRelation r) -> byId.get(r.sourceId()).type().displayOrder())
                .thenComparing(r -> byId.get(r.sourceId()).name())
                .thenComparingInt(r -> byId.get(r.targetId()).type().displayOrder())
                .thenComparing(r -> byId.get(r.targetId()).name());
        relations.sort(comparator);
    }

    private boolean portImplementedBy(ArchitectureElement port, ArchitectureElement service) {
        String portCore = stripSuffix(port.className(), "UseCase");
        if (portCore.equalsIgnoreCase(port.className())) {
            portCore = stripSuffix(port.className(), "Port");
        }
        String serviceCore = stripSuffix(service.className(), "Service");
        return !portCore.isBlank() && portCore.equalsIgnoreCase(serviceCore);
    }

    private boolean adapterImplementsPort(ArchitectureElement adapter, ArchitectureElement port) {
        String adapterCore = stripSuffix(stripTechPrefixes(adapter.className()), "Adapter");
        String portCore = stripSuffix(port.className(), "Port");
        if (adapterCore.isBlank() || portCore.isBlank()) {
            return false;
        }
        String a = adapterCore.toLowerCase(Locale.ROOT);
        String p = portCore.toLowerCase(Locale.ROOT);
        return a.equals(p) || a.contains(p) || p.contains(a);
    }

    private boolean tokensOverlap(ArchitectureElement a, ArchitectureElement b) {
        Set<String> tokens = significantTokens(a.className());
        tokens.retainAll(significantTokens(b.className()));
        return !tokens.isEmpty();
    }

    Set<String> significantTokens(String className) {
        Set<String> tokens = new LinkedHashSet<>();
        for (String token : tokenize(className)) {
            String lower = token.toLowerCase(Locale.ROOT);
            if (lower.length() >= 2 && !STOPWORDS.contains(lower)) {
                tokens.add(lower);
            }
        }
        return tokens;
    }

    private List<String> tokenize(String name) {
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!Character.isLetterOrDigit(c)) {
                flush(tokens, current);
                continue;
            }
            if (Character.isUpperCase(c) && current.length() > 0) {
                char prev = name.charAt(i - 1);
                if (Character.isLowerCase(prev) || Character.isDigit(prev)) {
                    flush(tokens, current);
                }
            }
            current.append(c);
        }
        flush(tokens, current);
        return tokens;
    }

    private void flush(List<String> tokens, StringBuilder current) {
        if (current.length() > 0) {
            tokens.add(current.toString());
            current.setLength(0);
        }
    }

    private String stripTechPrefixes(String name) {
        String result = name;
        boolean changed = true;
        while (changed) {
            changed = false;
            for (String prefix : TECH_PREFIXES) {
                if (result.length() > prefix.length()
                        && result.startsWith(prefix)
                        && Character.isUpperCase(result.charAt(prefix.length()))) {
                    result = result.substring(prefix.length());
                    changed = true;
                    break;
                }
            }
        }
        return result;
    }

    private String stripSuffix(String name, String suffix) {
        if (name.length() > suffix.length()
                && name.regionMatches(true, name.length() - suffix.length(), suffix, 0, suffix.length())) {
            return name.substring(0, name.length() - suffix.length());
        }
        return name;
    }
}
