package io.sinapsi.hive.cli.service.blueprint;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class BlueprintKindRegistry {
    private static final List<BlueprintKind> KINDS = List.of(
            kind("id", List.of(), List.of("kind", "name"), List.of("module", "type")),
            kind("vo", List.of(), List.of("kind", "name"), List.of(
                    "module", "type", "fields", "notNull", "notBlank", "min", "max",
                    "minLength", "maxLength", "pattern", "factory")),
            kind("entity", List.of(), List.of("kind", "name", "id"), List.of("module", "fields")),
            kind("aggregate", List.of(), List.of("kind", "name", "id"), List.of("module", "fields")),
            kind("enum", List.of(), List.of("kind", "name", "values"), List.of("module")),
            kind("event", List.of("domainEvent"), List.of("kind", "name"), List.of("module", "fields")),
            kind("exception", List.of(), List.of("kind", "name"), List.of("module", "message")),
            kind("domainService", List.of("domainservice"), List.of("kind", "name"), List.of("module")),
            kind("snapshot", List.of(), List.of("kind", "name"), List.of("module", "fields")),
            kind("record", List.of(), List.of("kind", "name"), List.of("module", "fields")),
            kind("class", List.of(), List.of("kind", "name"), List.of(
                    "module", "fields", "getters", "setters", "constructor", "allArgsConstructor")),
            kind("command", List.of(), List.of("kind", "name"), List.of("module", "fields")),
            kind("outputPort", List.of("port"), List.of("kind", "name"), List.of("module", "methods")),
            kind("adapter", List.of(), List.of("kind", "name", "ports"), List.of("module"))
    );

    private final Map<String, BlueprintKind> byKindValue;

    public BlueprintKindRegistry() {
        Map<String, BlueprintKind> resolved = new LinkedHashMap<>();
        for (BlueprintKind kind : KINDS) {
            for (String kindValue : kind.allKindValues()) {
                resolved.put(kindValue, kind);
            }
        }
        byKindValue = Map.copyOf(resolved);
    }

    public List<BlueprintKind> kinds() {
        return KINDS;
    }

    public Optional<BlueprintKind> find(String kind) {
        return Optional.ofNullable(byKindValue.get(kind));
    }

    public BlueprintKind require(String kind) {
        return find(kind).orElseThrow(() -> new IllegalArgumentException("Unsupported blueprint kind: " + kind));
    }

    private static BlueprintKind kind(String name, List<String> aliases, List<String> required, List<String> optional) {
        return new BlueprintKind(name, aliases, required, optional);
    }
}
