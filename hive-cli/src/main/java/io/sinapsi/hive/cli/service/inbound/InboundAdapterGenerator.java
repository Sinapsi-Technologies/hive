package io.sinapsi.hive.cli.service.inbound;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FieldSpec;
import io.sinapsi.hive.cli.service.MavenPomUpdater;
import io.sinapsi.hive.cli.service.NameResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class InboundAdapterGenerator {
    private final NameResolver names = new NameResolver();
    private final UseCaseResolver useCases = new UseCaseResolver();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();

    public List<Path> generate(Path projectRoot, HiveConfig config, InboundGenerationRequest request) throws IOException {
        String adapterBase = names.requireJavaTypeName(request.adapterName());
        String adapterType = withSuffix(adapterBase, request.type().adapterSuffix());
        String contextRoot = contextRootPackage(config, request.moduleName());
        String adapterPackage = contextRoot + ".infrastructure.adapters.in." + request.type().directory();
        Path adapterDir = names.packageDirectory(config.javaSourceRoot(projectRoot), adapterPackage);

        List<OperationModel> operations = new ArrayList<>();
        for (InboundOperation operation : request.operations()) {
            ResolvedUseCase resolved = useCases.resolve(projectRoot, config, request.moduleName(), operation.useCaseName());
            operations.add(new OperationModel(operation, resolved, transportType(resolved, request.type())));
        }

        List<Path> created = new ArrayList<>();
        created.add(write(
                adapterDir.resolve(adapterType + ".java"),
                adapterTemplate(adapterPackage, adapterType, request.type(), operations),
                request.force()
        ));
        for (OperationModel operation : operations) {
            created.add(write(
                    adapterDir.resolve(operation.transportType() + ".java"),
                    transportTemplate(
                            adapterPackage,
                            operation.transportType(),
                            operation.useCase().commandFields(),
                            operation.useCase().commandImports()
                    ),
                    request.force()
            ));
        }
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    private String adapterTemplate(
            String adapterPackage,
            String adapterType,
            InboundAdapterType type,
            List<OperationModel> operations
    ) {
        return """
                package %s;

                %s
                public final class %s {
                %s
                    public %s(%s) {
                %s    }
                %s}
                """.formatted(
                adapterPackage,
                imports(operations),
                adapterType,
                fields(operations),
                adapterType,
                constructorParameters(operations),
                constructorAssignments(operations),
                operationMethods(type, operations)
        );
    }

    private String imports(List<OperationModel> operations) {
        Map<String, String> imports = new LinkedHashMap<>();
        imports.put("io.sinapsi.hive.core.result.Result", "io.sinapsi.hive.core.result.Result");
        for (OperationModel operation : operations) {
            ResolvedUseCase useCase = operation.useCase();
            imports.put(useCase.useCasePackage() + "." + useCase.useCaseType(), useCase.useCasePackage() + "." + useCase.useCaseType());
            if (!useCase.nestedCommand() && useCase.commandType() != null) {
                imports.put(useCase.commandPackage() + "." + useCase.commandType(), useCase.commandPackage() + "." + useCase.commandType());
            }
            if (!useCase.nestedCommand() && useCase.commandType() != null && useCase.hasCommandFactory()
                    && !"Factory".equals(useCase.factoryType())) {
                imports.put(useCase.commandPackage() + "." + useCase.factoryType(), useCase.commandPackage() + "." + useCase.factoryType());
            }
        }
        return imports.values().stream()
                .map(value -> "import " + value + ";")
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
    }

    private String fields(List<OperationModel> operations) {
        StringBuilder builder = new StringBuilder();
        for (OperationModel operation : operations) {
            ResolvedUseCase useCase = operation.useCase();
            builder.append("    private final ")
                    .append(useCase.useCaseType())
                    .append(" ")
                    .append(fieldName(useCase.useCaseType()))
                    .append(";\n");
            if (useCase.hasCommandFactory()) {
                builder.append("    private final ")
                        .append(useCase.factoryReference())
                        .append(" ")
                        .append(useCase.factoryFieldName())
                        .append(";\n");
            }
        }
        return builder.toString();
    }

    private String constructorParameters(List<OperationModel> operations) {
        List<String> parameters = new ArrayList<>();
        for (OperationModel operation : operations) {
            ResolvedUseCase useCase = operation.useCase();
            parameters.add(useCase.useCaseType() + " " + fieldName(useCase.useCaseType()));
            if (useCase.hasCommandFactory()) {
                parameters.add(useCase.factoryReference() + " " + useCase.factoryFieldName());
            }
        }
        return String.join(", ", parameters);
    }

    private String constructorAssignments(List<OperationModel> operations) {
        StringBuilder builder = new StringBuilder();
        for (OperationModel operation : operations) {
            ResolvedUseCase useCase = operation.useCase();
            builder.append("        this.")
                    .append(fieldName(useCase.useCaseType()))
                    .append(" = ")
                    .append(fieldName(useCase.useCaseType()))
                    .append(";\n");
            if (useCase.hasCommandFactory()) {
                builder.append("        this.")
                        .append(useCase.factoryFieldName())
                        .append(" = ")
                        .append(useCase.factoryFieldName())
                        .append(";\n");
            }
        }
        return builder.toString();
    }

    private String operationMethods(InboundAdapterType type, List<OperationModel> operations) {
        StringBuilder builder = new StringBuilder();
        for (OperationModel operation : operations) {
            ResolvedUseCase useCase = operation.useCase();
            String transport = operation.operation().transport();
            if (transport != null) {
                builder.append("\n    // TODO: bind transport operation ")
                        .append(transport)
                        .append(" to this method.");
            } else {
                builder.append("\n    // TODO: bind this ")
                        .append(type.directory())
                        .append(" adapter method to the project-specific transport.");
            }
            builder.append("\n    public Result ")
                    .append(fieldName(useCase.baseName()))
                    .append("(")
                    .append(operation.transportType())
                    .append(" input) {\n");
            if (useCase.commandType() != null && useCase.hasCommandFactory()) {
                builder.append("        ")
                        .append(useCase.commandReference())
                        .append(" command = ")
                        .append(useCase.factoryFieldName())
                        .append(".create(")
                        .append(requestArguments(useCase.commandFields()))
                        .append(");\n")
                        .append("        return ")
                        .append(fieldName(useCase.useCaseType()))
                        .append(".handle(command);\n");
            } else {
                builder.append("        // TODO: wire the transport model to the application contract once the command factory is available.\n")
                        .append("        throw new UnsupportedOperationException(\"TODO: wire inbound adapter to ")
                        .append(useCase.useCaseType())
                        .append("\");\n");
            }
            builder.append("    }\n");
        }
        return builder.toString();
    }

    private String transportTemplate(String packageName, String typeName, List<FieldSpec> fields, List<String> imports) {
        String importBlock = imports.isEmpty() ? "" : String.join("\n", imports) + "\n";
        if (fields.isEmpty()) {
            return """
                    package %s;

                    %s
                    public record %s() {
                        // TODO: add transport fields only when they are part of the public inbound contract.
                    }
                    """.formatted(packageName, importBlock, typeName);
        }
        return """
                package %s;

                %s
                public record %s(%s) {
                }
                """.formatted(packageName, importBlock, typeName, recordParameters(fields));
    }

    private String requestArguments(List<FieldSpec> fields) {
        return fields.stream()
                .map(field -> "input." + field.name() + "()")
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String recordParameters(List<FieldSpec> fields) {
        return fields.stream()
                .map(field -> field.type() + " " + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String transportType(ResolvedUseCase useCase, InboundAdapterType type) {
        return useCase.baseName() + type.transportSuffix();
    }

    private String contextRootPackage(HiveConfig config, String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return config.basePackage();
        }
        return config.basePackage() + ".modules." + names.modulePackageName(moduleName);
    }

    private String withSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value : value + suffix;
    }

    private String fieldName(String typeName) {
        return Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
    }

    private Path write(Path path, String content, boolean force) throws IOException {
        if (Files.exists(path) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }

    private record OperationModel(InboundOperation operation, ResolvedUseCase useCase, String transportType) {
    }
}
