package io.sinapsi.hive.commons.model;

import java.util.List;

public record ValueObjectSpec(String name, String type, boolean notNull, boolean notBlank,
                              String min, String max, Integer minLength, Integer maxLength,
                              String pattern, List<String> fieldValues, boolean factory) {
}
