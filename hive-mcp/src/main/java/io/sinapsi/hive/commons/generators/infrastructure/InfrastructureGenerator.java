package io.sinapsi.hive.commons.generators.infrastructure;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.project.MavenPomUpdater;
import io.sinapsi.hive.commons.support.NameResolver;
import io.sinapsi.hive.commons.support.JavaSourceSupport;
import io.sinapsi.hive.commons.model.MethodSpec;
import io.sinapsi.hive.commons.template.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class InfrastructureGenerator {
    private static final Pattern METHOD = Pattern.compile("\\s*([A-Za-z0-9_$.<>?, ]+)\\s+(\\w+)\\(([^)]*)\\)\\s*;");
    private final NameResolver names = new NameResolver();
    private final TemplateRenderer templates = new TemplateRenderer();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();
    private final JavaSourceSupport java = new JavaSourceSupport();

    public List<Path> adapter(Path root, HiveConfig config, String module, String name, List<String> ports, String group, boolean force) throws IOException {
        String requestedType = names.requireJavaTypeName(name);
        String adapterType = requestedType.endsWith("Adapter") ? requestedType : requestedType + "Adapter";
        String contextRoot = contextRootPackage(config, module);
        String relativePackage = "infrastructure.adapters.out" + (group == null || group.isBlank() ? "" : "." + names.requirePackageSegment("adapter group", group));
        String packageName = contextRoot + "." + relativePackage;
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("infrastructure/adapters/out")
                .resolve(group == null || group.isBlank() ? "" : names.requirePackageSegment("adapter group", group))
                .resolve(adapterType + ".java");
        List<String> portTypes = ports.stream().map(this::portTypeName).toList();
        String interfaces = portTypes.stream().reduce((a, b) -> a + ", " + b).orElse("Object");
        List<String> imports = new ArrayList<>(portTypes.stream()
                .map(portType -> contextRoot + ".application.ports.out." + portType)
                .toList());
        List<Map<String, String>> methods = new ArrayList<>();
        for (String portType : portTypes) {
            Path portFile = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                    .resolve("application/ports/out").resolve(portType + ".java");
            if (!Files.exists(portFile)) {
                continue;
            }
            for (String line : Files.readAllLines(portFile)) {
                Matcher matcher = METHOD.matcher(line);
                if (!matcher.matches()) {
                    continue;
                }
                String returnType = matcher.group(1).trim();
                String methodName = matcher.group(2);
                String parameters = matcher.group(3).trim();
                methods.add(Map.of(
                        "returnType", returnType,
                        "name", methodName,
                        "parameters", parameters,
                        "body", defaultBody(returnType, methodName)));
            }
        }
        imports.addAll(projectImports(root, contextRoot, methods));
        String source = templates.render("infrastructure/outbound-adapter.java.ftl", Map.of("packageName", packageName, "imports", imports, "className", adapterType, "interfaces", interfaces, "methods", methods));
        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    private String contextRootPackage(HiveConfig config, String module) {
        return module == null || module.isBlank() ? config.basePackage() : config.basePackage() + ".modules." + names.modulePackageName(module);
    }

    private void write(Path target, String source, boolean force) throws IOException {
        if (Files.exists(target) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + target);
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, source);
    }

    private String portTypeName(String name) {
        String typeName = names.requireJavaTypeName(name);
        return typeName.endsWith("Port") ? typeName : typeName + "Port";
    }

    private String defaultBody(String returnType, String methodName) {
        if ("void".equals(returnType)) {
            return "throw new UnsupportedOperationException(\"TODO: implement " + methodName + "\");";
        }
        return "throw new UnsupportedOperationException(\"TODO: implement " + methodName + "\");";
    }

    private List<String> projectImports(Path root, String contextRoot, List<Map<String, String>> methods) {
        List<String> types = methods.stream()
                .flatMap(method -> referencedMethodTypes(method).stream())
                .toList();
        List<String> imports = new ArrayList<>();
        String standard = java.importsFor(types);
        if (!standard.isBlank()) {
            for (String line : standard.split("\\R")) {
                if (!line.isBlank()) imports.add(line.substring("import ".length(), line.length() - 1));
            }
        }
        for (String type : types) {
            if (type.contains(".") || java.importsFor(List.of(type)).contains("import ") || isPrimitive(type)) continue;
            for (String relative : List.of("domain/valueobjects", "domain/entities", "domain/aggregates")) {
                Path candidate = root.resolve("src/main/java").resolve(contextRoot.replace('.', '/')).resolve(relative).resolve(type + ".java");
                if (Files.exists(candidate)) {
                    imports.add(contextRoot + "." + relative.replace('/', '.') + "." + type);
                    break;
                }
            }
        }
        return imports.stream().distinct().toList();
    }

    private List<String> referencedMethodTypes(Map<String, String> method) {
        String signature = method.get("returnType") + " " + method.get("name")
                + "(" + method.get("parameters") + ")";
        MethodSpec parsed = java.parseMethods(List.of(signature)).getFirst();
        return Stream.concat(
                java.referencedTypes(parsed.returnType()).stream(),
                parsed.parameters().stream()
                        .flatMap(parameter -> java.referencedTypes(parameter.type()).stream()))
                .toList();
    }

    private boolean isPrimitive(String type) {
        return List.of("void", "boolean", "byte", "short", "int", "long", "float", "double", "char").contains(type);
    }
}
