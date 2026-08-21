package io.sinapsi.hive.cli.service.blueprint;

import java.util.List;

public record BlueprintType(
        String kind,
        String module,
        String name,
        String type,
        String id,
        String message,
        List<String> fields,
        List<String> methods,
        List<String> ports,
        List<String> values,
        boolean notNull,
        boolean notBlank,
        String min,
        String max,
        Integer minLength,
        Integer maxLength,
        String pattern,
        boolean factory,
        boolean getters,
        boolean setters,
        boolean constructor,
        boolean allArgsConstructor
) {
}
