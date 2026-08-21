package io.sinapsi.hive.cli.service.blueprint;

import java.nio.file.Path;
import java.util.List;

public record BlueprintValidationResult(Path file, List<BlueprintDiagnostic> errors) {
    public boolean valid() {
        return errors.isEmpty();
    }
}
