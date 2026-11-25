package io.sinapsi.hive.commons.model;

import java.util.List;

public record MethodSpec(String returnType, String name, List<FieldSpec> parameters) {
}
