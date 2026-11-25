package io.sinapsi.hive.mcp.domainmodel;

import java.util.List;

public record DomainModelValidation(boolean valid, List<String> errors) {

    public DomainModelValidation {
        errors = errors == null ? List.of() : List.copyOf(errors);
    }
}
