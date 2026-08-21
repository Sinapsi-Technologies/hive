package io.sinapsi.hive.cli.service.blueprint;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BlueprintValidator {
    private final BlueprintParser parser = new BlueprintParser();
    private final BlueprintKindRegistry kinds = new BlueprintKindRegistry();

    public BlueprintValidationResult validate(Path file) {
        List<BlueprintDiagnostic> errors = new ArrayList<>();
        List<BlueprintType> types;
        try {
            types = parser.parse(file);
        } catch (BlueprintParseException exception) {
            errors.add(new BlueprintDiagnostic(
                    "BLUEPRINT_PARSE_ERROR",
                    null,
                    exception.getMessage(),
                    null,
                    null,
                    file.toString(),
                    exception.line()
            ));
            return new BlueprintValidationResult(file, errors);
        } catch (IOException exception) {
            errors.add(new BlueprintDiagnostic(
                    "BLUEPRINT_FILE_ERROR",
                    null,
                    exception.getMessage(),
                    null,
                    null,
                    file.toString(),
                    null
            ));
            return new BlueprintValidationResult(file, errors);
        } catch (RuntimeException exception) {
            errors.add(new BlueprintDiagnostic(
                    "BLUEPRINT_PARSE_ERROR",
                    null,
                    exception.getMessage(),
                    null,
                    null,
                    file.toString(),
                    null
            ));
            return new BlueprintValidationResult(file, errors);
        }

        for (int i = 0; i < types.size(); i++) {
            BlueprintType type = types.get(i);
            int index = i;
            BlueprintKind kind = kinds.find(type.kind()).orElse(null);
            if (kind == null) {
                errors.add(error(
                        "BLUEPRINT_UNSUPPORTED_KIND",
                        index,
                        "kind",
                        "Unsupported blueprint kind: " + type.kind(),
                        type
                ));
                continue;
            }
            for (String required : kind.required()) {
                if (missing(type, required)) {
                    errors.add(error(
                            "BLUEPRINT_REQUIRED_FIELD_MISSING",
                            index,
                            required,
                            kind.name() + " " + type.name() + " requires property '" + required + "'",
                            type
                    ));
                }
            }
            errors.addAll(validateFieldSpecs(type.fields(), index, "fields", type));
            errors.addAll(validateMethodSpecs(type.methods(), index, type));
        }

        return new BlueprintValidationResult(file, List.copyOf(errors));
    }

    private List<BlueprintDiagnostic> validateFieldSpecs(
            List<String> fields,
            int artifactIndex,
            String property,
            BlueprintType type
    ) {
        List<BlueprintDiagnostic> errors = new ArrayList<>();
        for (int i = 0; i < fields.size(); i++) {
            String field = fields.get(i);
            String[] parts = field.split(":", 2);
            if (parts.length != 2 || parts[0].isBlank() || parts[1].isBlank() || "null".equals(parts[1])) {
                errors.add(error(
                        "BLUEPRINT_INVALID_FIELD",
                        artifactIndex,
                        property + "[" + i + "]",
                        "Field definitions must include non-blank name and type",
                        type
                ));
            }
        }
        return errors;
    }

    private List<BlueprintDiagnostic> validateMethodSpecs(List<String> methods, int artifactIndex, BlueprintType type) {
        List<BlueprintDiagnostic> errors = new ArrayList<>();
        for (int i = 0; i < methods.size(); i++) {
            String method = methods.get(i);
            if (!method.contains("(") || !method.endsWith(")") || method.substring(0, method.indexOf('(')).isBlank()) {
                errors.add(error(
                        "BLUEPRINT_INVALID_METHOD",
                        artifactIndex,
                        "methods[" + i + "]",
                        "Method definitions must include return type, name, and parameter list",
                        type
                ));
            }
        }
        return errors;
    }

    private boolean missing(BlueprintType type, String property) {
        return switch (property) {
            case "kind" -> blank(type.kind());
            case "name" -> blank(type.name());
            case "type" -> blank(type.type());
            case "id" -> blank(type.id());
            case "values" -> type.values().isEmpty();
            case "ports" -> type.ports().isEmpty();
            default -> false;
        };
    }

    private boolean blank(String value) {
        return value == null || value.isBlank();
    }

    private BlueprintDiagnostic error(
            String code,
            int artifactIndex,
            String property,
            String message,
            BlueprintType type
    ) {
        return new BlueprintDiagnostic(
                code,
                "types[" + artifactIndex + "]." + property,
                message,
                type.kind(),
                type.name(),
                null,
                null
        );
    }
}
