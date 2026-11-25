package io.sinapsi.hive.commons.model;

import java.util.List;

public record ClassSpec(String name, List<String> fieldValues, boolean getters,
                        boolean setters, boolean constructor, boolean allArgsConstructor) {
}
