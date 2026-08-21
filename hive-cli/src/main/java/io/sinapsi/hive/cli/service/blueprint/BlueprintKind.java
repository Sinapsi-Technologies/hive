package io.sinapsi.hive.cli.service.blueprint;

import java.util.List;

public record BlueprintKind(
        String name,
        List<String> aliases,
        List<String> required,
        List<String> optional
) {
    public List<String> allKindValues() {
        return java.util.stream.Stream.concat(java.util.stream.Stream.of(name), aliases.stream()).toList();
    }
}
