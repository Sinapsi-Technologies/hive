package io.sinapsi.hive.cli.service;

import java.util.List;

public record MethodSpec(String returnType, String name, List<FieldSpec> parameters) {
}
