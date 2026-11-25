package ${packageName};

import io.sinapsi.hive.core.domain.AggregateId;
${importsBlock}

public record ${className}(${valueType} value) implements AggregateId<${valueType}> {
    public ${className} {
        if (value == null) {
            throw new IllegalArgumentException("value must not be null");
        }
    }

    public static ${className} of(${valueType} value) {
        return new ${className}(value);
    }
}
