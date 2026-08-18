package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class FileScaffolder {
    private final HiveConfigLoader configLoader = new HiveConfigLoader();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();
    private final NameResolver names = new NameResolver();
    private final JavaSourceSupport java = new JavaSourceSupport();

    public List<Path> init(Path projectRoot, HiveConfig config, boolean force, boolean readme) throws IOException {
        List<Path> created = new ArrayList<>();
        created.add(write(projectRoot.resolve(".hive-project"), "hive-toolkit\n", force));
        created.add(write(projectRoot.resolve("hive.yml"), configLoader.render(config), force));
        created.add(createDirectory(config.javaSourceRoot(projectRoot)));
        created.add(createDirectory(config.testSourceRoot(projectRoot)));
        created.add(pomUpdater.ensureBasePom(projectRoot, config));
        if (readme) {
            Path readmePath = projectRoot.resolve("README.md");
            if (!Files.exists(readmePath)) {
                created.add(write(readmePath, "# Hive Project\n\nGenerated with hive-toolkit.\n", force));
            }
        }
        return created;
    }

    public List<Path> createUseCase(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String useCaseName,
            boolean force,
            boolean factory
    )
            throws IOException {
        return createUseCase(projectRoot, config, moduleName, useCaseName, force, factory, List.of());
    }

    public List<Path> createUseCase(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String useCaseName,
            boolean force,
            boolean factory,
            List<String> fieldValues
    )
            throws IOException {
        String typeName = names.requireJavaTypeName(useCaseName);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String base = applicationPackage(config, moduleName);
        Path baseDir = names.packageDirectory(config.javaSourceRoot(projectRoot), base);

        String commandPackage = base + ".ports.in.commands";
        String inputPortPackage = base + ".ports.in";
        String servicePackage = base + ".services";

        List<Path> created = new ArrayList<>();
        created.add(write(
                baseDir.resolve("ports/in/commands").resolve(typeName + "Command.java"),
                commandTemplate(contextRootPackage(config, moduleName), commandPackage, typeName, fields),
                force
        ));
        created.add(write(
                baseDir.resolve("ports/in").resolve(typeName + "UseCase.java"),
                useCasePortTemplate(inputPortPackage, commandPackage, typeName),
                force
        ));
        created.add(write(
                baseDir.resolve("services").resolve(typeName + "Service.java"),
                serviceTemplate(servicePackage, commandPackage, inputPortPackage, typeName),
                force
        ));
        if (factory) {
            created.add(write(
                    baseDir.resolve("ports/in/commands").resolve(typeName + "CommandFactory.java"),
                    commandFactoryTemplate(contextRootPackage(config, moduleName), commandPackage, typeName, fields),
                    force
            ));
            pomUpdater.ensureValidatorDependency(projectRoot, config);
        } else {
            pomUpdater.ensureBasePom(projectRoot, config);
        }
        return created;
    }

    public List<Path> createArchTest(Path projectRoot, HiveConfig config, boolean force) throws IOException {
        Path testPackageRoot = names.packageDirectory(config.testSourceRoot(projectRoot), config.basePackage());
        Path testPath = testPackageRoot.resolve("ArchitectureTest.java");
        List<Path> created = new ArrayList<>();
        created.add(write(testPath, archTestTemplate(config.basePackage()), force));
        pomUpdater.ensureArchUnitDependencies(projectRoot, config);
        return created;
    }

    public List<Path> createModule(Path projectRoot, HiveConfig config, String moduleName) throws IOException {
        String modulePackage = config.basePackage() + ".modules." + names.modulePackageName(moduleName);
        Path moduleRoot = names.packageDirectory(config.javaSourceRoot(projectRoot), modulePackage);
        List<Path> created = new ArrayList<>();
        created.add(createDirectory(moduleRoot.resolve("commons")));
        created.add(createDirectory(moduleRoot.resolve("configurations")));
        created.add(createDirectory(moduleRoot.resolve("application/ports/in/commands")));
        created.add(createDirectory(moduleRoot.resolve("application/ports/out")));
        created.add(createDirectory(moduleRoot.resolve("application/services")));
        created.add(createDirectory(moduleRoot.resolve("domain/valueobjects")));
        created.add(createDirectory(moduleRoot.resolve("domain/aggregates")));
        created.add(createDirectory(moduleRoot.resolve("domain/events")));
        created.add(createDirectory(moduleRoot.resolve("domain/entities")));
        created.add(createDirectory(moduleRoot.resolve("domain/services")));
        created.add(createDirectory(moduleRoot.resolve("domain/snapshots")));
        created.add(createDirectory(moduleRoot.resolve("domain/exceptions")));
        created.add(createDirectory(moduleRoot.resolve("infrastructure/configs")));
        created.add(createDirectory(moduleRoot.resolve("infrastructure/adapters/in")));
        created.add(createDirectory(moduleRoot.resolve("infrastructure/adapters/out")));
        return created;
    }

    public List<Path> createPort(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String portName,
            boolean force
    ) throws IOException {
        return createPort(projectRoot, config, moduleName, portName, List.of(), force);
    }

    public List<Path> createPort(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String portName,
            List<String> methodValues,
            boolean force
    ) throws IOException {
        String typeName = withSuffix(names.requireJavaTypeName(portName), "Port");
        List<MethodSpec> methods = java.parseMethods(methodValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String portPackage = contextRoot + ".application.ports.out";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("application/ports/out").resolve(typeName + ".java"),
                outputPortTemplate(contextRoot, portPackage, typeName, methods),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createAdapter(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String adapterName,
            String portName,
            boolean force
    ) throws IOException {
        return createAdapter(projectRoot, config, moduleName, adapterName, List.of(portName), force);
    }

    public List<Path> createAdapter(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String adapterName,
            List<String> portNames,
            boolean force
    ) throws IOException {
        if (portNames == null || portNames.isEmpty()) {
            throw new IllegalArgumentException("At least one --port is required");
        }
        String adapterType = withSuffix(names.requireJavaTypeName(adapterName), "Adapter");
        List<String> portTypes = portNames.stream()
                .map(name -> withSuffix(names.requireJavaTypeName(name), "Port"))
                .toList();
        String contextRoot = contextRootPackage(config, moduleName);
        String adapterPackage = contextRoot + ".infrastructure.adapters.out";
        String portPackage = contextRoot + ".application.ports.out";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);
        List<MethodSpec> methods = adapterMethods(projectRoot, config, contextDir, portTypes);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("infrastructure/adapters/out").resolve(adapterType + ".java"),
                outboundAdapterTemplate(contextRoot, adapterPackage, portPackage, adapterType, portTypes, methods),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createValueObject(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            ValueObjectSpec spec,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(spec.name());
        String valueType = requireScalarType(spec.type());
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.valueobjects";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/valueobjects").resolve(typeName + ".java"),
                valueObjectTemplate(packageName, typeName, valueType, spec),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createIdentifier(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String identifierName,
            String valueType,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(identifierName);
        String scalarType = requireIdentifierType(valueType);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.valueobjects";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/valueobjects").resolve(typeName + ".java"),
                identifierTemplate(packageName, typeName, scalarType),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createEntity(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String entityName,
            String idType,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(entityName);
        String resolvedIdType = names.requireJavaTypeName(idType);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.entities";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/entities").resolve(typeName + ".java"),
                entityTemplate(contextRoot, packageName, typeName, resolvedIdType, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createAggregate(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String aggregateName,
            String idType,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(aggregateName);
        String resolvedIdType = names.requireJavaTypeName(idType);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.aggregates";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/aggregates").resolve(typeName + ".java"),
                aggregateTemplate(contextRoot, packageName, typeName, resolvedIdType, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createEnum(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String enumName,
            List<String> values,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(enumName);
        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException("At least one --value is required");
        }
        List<String> resolvedValues = values.stream()
                .map(this::requireEnumConstant)
                .toList();
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.valueobjects";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/valueobjects").resolve(typeName + ".java"),
                enumTemplate(packageName, typeName, resolvedValues),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createEvent(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String eventName,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(eventName);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.events";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/events").resolve(typeName + ".java"),
                eventTemplate(contextRoot, packageName, typeName, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createException(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String exceptionName,
            boolean force
    ) throws IOException {
        return createException(projectRoot, config, moduleName, exceptionName, null, force);
    }

    public List<Path> createException(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String exceptionName,
            String message,
            boolean force
    ) throws IOException {
        String typeName = withSuffix(names.requireJavaTypeName(exceptionName), "Exception");
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.exceptions";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/exceptions").resolve(typeName + ".java"),
                exceptionTemplate(packageName, typeName, message),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createDomainService(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String serviceName,
            boolean force
    ) throws IOException {
        String typeName = withSuffix(names.requireJavaTypeName(serviceName), "Service");
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.services";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/services").resolve(typeName + ".java"),
                domainServiceTemplate(packageName, typeName),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createSnapshot(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String snapshotName,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(snapshotName);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".domain.snapshots";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("domain/snapshots").resolve(typeName + ".java"),
                recordTemplate(contextRoot, "domain.snapshots", packageName, typeName, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createCommand(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String commandName,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = withSuffix(names.requireJavaTypeName(commandName), "Command");
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String commandPackage = applicationPackage(config, moduleName) + ".ports.in.commands";
        Path commandDir = names.packageDirectory(config.javaSourceRoot(projectRoot), commandPackage);

        List<Path> created = new ArrayList<>();
        created.add(write(
                commandDir.resolve(typeName + ".java"),
                commandRecordTemplate(contextRootPackage(config, moduleName), commandPackage, typeName, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createRecord(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            String recordName,
            List<String> fieldValues,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(recordName);
        List<FieldSpec> fields = java.parseFields(fieldValues);
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".commons";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("commons").resolve(typeName + ".java"),
                recordTemplate(contextRoot, "commons", packageName, typeName, fields),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<Path> createPlainClass(
            Path projectRoot,
            HiveConfig config,
            String moduleName,
            ClassSpec spec,
            boolean force
    ) throws IOException {
        String typeName = names.requireJavaTypeName(spec.name());
        List<FieldSpec> fields = java.parseFields(spec.fieldValues());
        String contextRoot = contextRootPackage(config, moduleName);
        String packageName = contextRoot + ".commons";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("commons").resolve(typeName + ".java"),
                classTemplate(contextRoot, "commons", packageName, typeName, fields, spec),
                force
        ));
        pomUpdater.ensureBasePom(projectRoot, config);
        return created;
    }

    public List<String> check(Path projectRoot, HiveConfig config) {
        List<String> warnings = new ArrayList<>();
        if (!Files.exists(projectRoot.resolve(".hive-project"))) {
            warnings.add("Missing .hive-project marker");
        }
        if (!Files.exists(projectRoot.resolve("hive.yml"))) {
            warnings.add("Missing hive.yml");
        }
        if (!Files.exists(projectRoot.resolve("pom.xml"))) {
            warnings.add("Missing pom.xml");
        }
        if (!Files.isDirectory(config.javaSourceRoot(projectRoot))) {
            warnings.add("Missing java source root: " + config.javaSourceRoot());
        }
        if (!Files.isDirectory(config.testSourceRoot(projectRoot))) {
            warnings.add("Missing test source root: " + config.testSourceRoot());
        }
        return warnings;
    }

    private String applicationPackage(HiveConfig config, String moduleName) {
        return contextRootPackage(config, moduleName) + ".application";
    }

    private String contextRootPackage(HiveConfig config, String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return config.basePackage();
        }
        return config.basePackage() + ".modules." + names.modulePackageName(moduleName);
    }

    private String withSuffix(String name, String suffix) {
        return name.endsWith(suffix) ? name : name + suffix;
    }

    private Path createDirectory(Path directory) throws IOException {
        Files.createDirectories(directory);
        return directory;
    }

    private Path write(Path path, String content, boolean force) throws IOException {
        if (Files.exists(path) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + path);
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }

    private String commandTemplate(String contextRoot, String packageName, String typeName, List<FieldSpec> fields) {
        if (fields.isEmpty()) {
            return """
                    package %s;

                    import io.sinapsi.hive.core.command.Command;

                    public record %sCommand() implements Command {
                        // TODO: add the fields this command carries
                    }
                    """.formatted(packageName, typeName);
        }
        return commandRecordTemplate(contextRoot, packageName, typeName + "Command", fields);
    }

    private String commandRecordTemplate(String contextRoot, String packageName, String typeName, List<FieldSpec> fields) {
        return """
                package %s;

                %s
                import io.sinapsi.hive.core.command.Command;

                public record %s(%s) implements Command {
                }
                """.formatted(
                packageName,
                importsForFields(contextRoot, "application.ports.in.commands", fields),
                typeName,
                recordParametersMultiline(fields)
        );
    }

    private String commandFactoryTemplate(String contextRoot, String packageName, String typeName, List<FieldSpec> fields) {
        String parameters = fields.stream()
                .map(field -> field.type() + " " + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        String arguments = fields.stream()
                .map(FieldSpec::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
        return """
                package %s;

                %s
                import io.sinapsi.hive.factory.AbstractCommandFactory;

                public final class %sCommandFactory extends AbstractCommandFactory<%sCommand> {
                    public %sCommand create(%s) {
                        return validate(new %sCommand(%s));
                    }
                }
                """.formatted(
                packageName,
                importsForFields(contextRoot, "application.ports.in.commands", fields),
                typeName,
                typeName,
                typeName,
                parameters,
                typeName,
                arguments
        );
    }

    private String useCasePortTemplate(String packageName, String commandPackage, String typeName) {
        return """
                package %s;

                import %s.%sCommand;
                import io.sinapsi.hive.core.result.Result;
                import io.sinapsi.hive.core.usecase.UseCase;

                public interface %sUseCase extends UseCase<%sCommand, Result> {
                }
                """.formatted(packageName, commandPackage, typeName, typeName, typeName);
    }

    private String serviceTemplate(String packageName, String commandPackage, String inputPortPackage, String typeName) {
        return """
                package %s;

                import %s.%sCommand;
                import %s.%sUseCase;
                import io.sinapsi.hive.core.result.Result;

                public final class %sService implements %sUseCase {
                    @Override
                    public Result handle(%sCommand input) {
                        // TODO: implement the use case logic
                        return new %sResult();
                    }

                    private record %sResult() implements Result {
                        // TODO: add the fields this result returns
                    }
                }
                """.formatted(
                packageName,
                commandPackage,
                typeName,
                inputPortPackage,
                typeName,
                typeName,
                typeName,
                typeName,
                typeName,
                typeName
        );
    }

    private String archTestTemplate(String basePackage) {
        return """
                package %s;

                import com.tngtech.archunit.junit.AnalyzeClasses;
                import com.tngtech.archunit.junit.ArchTest;
                import com.tngtech.archunit.lang.ArchRule;
                import io.sinapsi.hive.archunit.HexagonalRules;

                @AnalyzeClasses(packages = "%s")
                class ArchitectureTest {
                    @ArchTest
                    static final ArchRule hiveBaseRules = HexagonalRules.allBaseRules("%s");
                }
                """.formatted(basePackage, basePackage, basePackage);
    }

    private String outputPortTemplate(String contextRoot, String packageName, String typeName, List<MethodSpec> methods) {
        return """
                package %s;

                %s
                import io.sinapsi.hive.core.port.OutputPort;

                public interface %s extends OutputPort {
                %s
                }
                """.formatted(
                packageName,
                importsForMethods(contextRoot, "application.ports.out", methods),
                typeName,
                portMethodLines(methods)
        );
    }

    private String outboundAdapterTemplate(
            String contextRoot,
            String adapterPackage,
            String portPackage,
            String adapterType,
            List<String> portTypes,
            List<MethodSpec> methods
    ) {
        return """
                package %s;

                %s

                public final class %s implements %s {
                %s
                }
                """.formatted(
                adapterPackage,
                adapterImports(contextRoot, portPackage, portTypes, methods),
                adapterType,
                String.join(", ", portTypes),
                adapterMethodLines(methods, portTypes)
        );
    }

    private String valueObjectTemplate(String packageName, String typeName, String valueType, ValueObjectSpec spec) {
        List<FieldSpec> fields = java.parseFields(spec.fieldValues());
        if (!fields.isEmpty()) {
            if (spec.type() != null && !spec.type().isBlank() && !"String".equals(spec.type())) {
                throw new IllegalArgumentException("--type cannot be combined with --field for value objects");
            }
            if (spec.notNull() || spec.notBlank() || spec.min() != null || spec.max() != null
                    || spec.minLength() != null || spec.maxLength() != null || spec.pattern() != null) {
                throw new IllegalArgumentException("Scalar constraints cannot be combined with multi-field value objects");
            }
            String factory = spec.factory() ? factoryMethod(typeName, fields) : "";
            return """
                    package %s;

                    %s
                    public record %s(%s) {
                    %s}
                    """.formatted(
                    packageName,
                    importsForFields("", "", fields),
                    typeName,
                    recordParametersMultiline(fields),
                    factory
            );
        }
        String factory = spec.factory() ? scalarFactoryMethod(typeName, valueType) : "";
        return """
                package %s;

                %s
                public record %s(%s value) {
                    public %s {
                %s    }
                %s
                }
                """.formatted(
                packageName,
                importsForScalars(List.of(valueType)),
                typeName,
                valueType,
                typeName,
                validationLines(valueType, spec),
                factory
        );
    }

    private String identifierTemplate(String packageName, String typeName, String valueType) {
        return """
                package %s;

                import io.sinapsi.hive.core.domain.AggregateId;
                %s
                public record %s(%s value) implements AggregateId<%s> {
                    public %s {
                        if (value == null) {
                            throw new IllegalArgumentException("value must not be null");
                        }
                    }
                }
                """.formatted(packageName, importsForScalars(List.of(valueType)), typeName, valueType, valueType, typeName);
    }

    private String entityTemplate(
            String contextRoot,
            String packageName,
            String typeName,
            String idType,
            List<FieldSpec> fields
    ) {
        return """
                package %s;

                %s
                public final class %s {
                    private final %s id;
                %s
                    public %s(%s id%s) {
                        if (id == null) {
                            throw new IllegalArgumentException("id must not be null");
                        }
                        this.id = id;
                %s    }

                    public %s getId() {
                        return id;
                    }
                %s}
                """.formatted(
                packageName,
                importsForDomainTypes(contextRoot, "domain.entities", idType, fields, false),
                typeName,
                idType,
                fieldDeclarations(fields),
                typeName,
                idType,
                constructorParameters(fields),
                constructorAssignments(fields),
                idType,
                accessors(fields)
        );
    }

    private String aggregateTemplate(
            String contextRoot,
            String packageName,
            String typeName,
            String idType,
            List<FieldSpec> fields
    ) {
        return """
                package %s;

                import io.sinapsi.hive.core.domain.AggregateRoot;
                import io.sinapsi.hive.core.event.DomainEvent;
                import java.util.ArrayList;
                import java.util.List;
                %s
                public final class %s implements AggregateRoot<%s> {
                    private final %s id;
                    private final List<DomainEvent> domainEvents = new ArrayList<>();
                %s
                    public %s(%s id%s) {
                        if (id == null) {
                            throw new IllegalArgumentException("id must not be null");
                        }
                        this.id = id;
                %s    }

                    @Override
                    public %s getId() {
                        return id;
                    }

                    @Override
                    public List<DomainEvent> pullDomainEvents() {
                        List<DomainEvent> events = List.copyOf(domainEvents);
                        domainEvents.clear();
                        return events;
                    }

                    // TODO: add domain behavior here.
                %s}
                """.formatted(
                packageName,
                importsForDomainTypes(contextRoot, "domain.aggregates", idType, fields, true),
                typeName,
                idType,
                idType,
                fieldDeclarations(fields),
                typeName,
                idType,
                constructorParameters(fields),
                constructorAssignments(fields),
                idType,
                accessors(fields)
        );
    }

    private String enumTemplate(String packageName, String typeName, List<String> values) {
        return """
                package %s;

                public enum %s {
                    %s
                }
                """.formatted(packageName, typeName, String.join(",\n    ", values));
    }

    private String eventTemplate(String contextRoot, String packageName, String typeName, List<FieldSpec> fields) {
        return """
                package %s;

                import io.sinapsi.hive.core.event.DomainEvent;
                %s
                public record %s(%s) implements DomainEvent {
                }
                """.formatted(
                packageName,
                importsForDomainTypes(contextRoot, "domain.events", null, fields, false),
                typeName,
                recordParametersMultiline(fields)
        );
    }

    private String exceptionTemplate(String packageName, String typeName, String message) {
        String defaultConstructor = message == null || message.isBlank()
                ? ""
                : """
                    public %s() {
                        super("%s");
                    }

                """.formatted(typeName, escapeJavaString(message));
        return """
                package %s;

                public class %s extends RuntimeException {
                %s    public %s(String message) {
                        super(message);
                    }

                    public %s(String message, Throwable cause) {
                        super(message, cause);
                    }
                }
                """.formatted(packageName, typeName, defaultConstructor, typeName, typeName);
    }

    private String domainServiceTemplate(String packageName, String typeName) {
        return """
                package %s;

                public final class %s {
                    // TODO: add domain service behavior here.
                }
                """.formatted(packageName, typeName);
    }

    private String recordTemplate(
            String contextRoot,
            String currentPackage,
            String packageName,
            String typeName,
            List<FieldSpec> fields
    ) {
        return """
                package %s;

                %s
                public record %s(%s) {
                }
                """.formatted(
                packageName,
                importsForFields(contextRoot, currentPackage, fields),
                typeName,
                recordParametersMultiline(fields)
        );
    }

    private String classTemplate(
            String contextRoot,
            String currentPackage,
            String packageName,
            String typeName,
            List<FieldSpec> fields,
            ClassSpec spec
    ) {
        return """
                package %s;

                %s
                public class %s {
                %s%s%s%s}
                """.formatted(
                packageName,
                importsForFields(contextRoot, currentPackage, fields),
                typeName,
                classFieldDeclarations(fields),
                constructors(typeName, fields, spec),
                getterMethods(fields, spec.getters()),
                setterMethods(fields, spec.setters())
        );
    }

    private String classFieldDeclarations(List<FieldSpec> fields) {
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("    private ").append(field.type()).append(" ").append(field.name()).append(";\n");
        }
        return lines.isEmpty() ? "" : lines.append("\n").toString();
    }

    private String constructors(String typeName, List<FieldSpec> fields, ClassSpec spec) {
        StringBuilder lines = new StringBuilder();
        if (spec.constructor()) {
            lines.append("    public ").append(typeName).append("() {\n");
            lines.append("    }\n\n");
        }
        if (spec.allArgsConstructor()) {
            lines.append("    public ").append(typeName).append("(").append(recordParametersSingleLine(fields)).append(") {\n");
            for (FieldSpec field : fields) {
                lines.append("        this.").append(field.name()).append(" = ").append(field.name()).append(";\n");
            }
            lines.append("    }\n\n");
        }
        return lines.toString();
    }

    private String getterMethods(List<FieldSpec> fields, boolean enabled) {
        if (!enabled) {
            return "";
        }
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("    public ").append(field.type()).append(" get")
                    .append(capitalize(field.name())).append("() {\n");
            lines.append("        return ").append(field.name()).append(";\n");
            lines.append("    }\n\n");
        }
        return lines.toString();
    }

    private String setterMethods(List<FieldSpec> fields, boolean enabled) {
        if (!enabled) {
            return "";
        }
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("    public void set").append(capitalize(field.name()))
                    .append("(").append(field.type()).append(" ").append(field.name()).append(") {\n");
            lines.append("        this.").append(field.name()).append(" = ").append(field.name()).append(";\n");
            lines.append("    }\n\n");
        }
        return lines.toString();
    }

    private String portMethodLines(List<MethodSpec> methods) {
        if (methods.isEmpty()) {
            return "    // TODO: declare the capability methods this port exposes\n";
        }
        StringBuilder lines = new StringBuilder();
        for (MethodSpec method : methods) {
            lines.append("    ").append(method.returnType()).append(" ").append(method.name())
                    .append("(").append(recordParametersSingleLine(method.parameters())).append(");\n");
        }
        return lines.toString();
    }

    private String adapterMethodLines(List<MethodSpec> methods, List<String> portTypes) {
        if (methods.isEmpty()) {
            return "    // TODO: implement the " + String.join(", ", portTypes) + " methods\n";
        }
        StringBuilder lines = new StringBuilder();
        for (MethodSpec method : methods) {
            lines.append("    @Override\n");
            lines.append("    public ").append(method.returnType()).append(" ").append(method.name())
                    .append("(").append(recordParametersSingleLine(method.parameters())).append(") {\n");
            lines.append("        // TODO: implement adapter logic\n");
            if (!"void".equals(method.returnType())) {
                lines.append("        return ").append(defaultReturnValue(method.returnType())).append(";\n");
            }
            lines.append("    }\n\n");
        }
        return lines.toString();
    }

    private String defaultReturnValue(String type) {
        return switch (type) {
            case "boolean" -> "false";
            case "byte", "short", "int", "long", "float", "double", "char" -> "0";
            default -> "null";
        };
    }

    private String adapterImports(
            String contextRoot,
            String portPackage,
            List<String> portTypes,
            List<MethodSpec> methods
    ) {
        Set<String> imports = new LinkedHashSet<>();
        for (String portType : portTypes) {
            imports.add(portPackage + "." + portType);
        }
        collectMethodImports(imports, contextRoot, "infrastructure.adapters.out", methods);
        return renderImports(imports);
    }

    private String importsForMethods(String contextRoot, String currentPackage, List<MethodSpec> methods) {
        Set<String> imports = new LinkedHashSet<>();
        collectMethodImports(imports, contextRoot, currentPackage, methods);
        return renderImports(imports);
    }

    private void collectMethodImports(
            Set<String> imports,
            String contextRoot,
            String currentPackage,
            List<MethodSpec> methods
    ) {
        for (MethodSpec method : methods) {
            collectTypeImports(imports, contextRoot, currentPackage, method.returnType(), "domain.aggregates");
            for (FieldSpec parameter : method.parameters()) {
                collectTypeImports(imports, contextRoot, currentPackage, parameter.type(), "domain.aggregates");
            }
        }
    }

    private String importsForFields(String contextRoot, String currentPackage, List<FieldSpec> fields) {
        Set<String> imports = new LinkedHashSet<>();
        for (FieldSpec field : fields) {
            collectTypeImports(imports, contextRoot, currentPackage, field.type(), "domain.valueobjects");
        }
        return renderImports(imports);
    }

    private void collectTypeImports(Set<String> imports, String contextRoot, String currentPackage, String type) {
        collectTypeImports(imports, contextRoot, currentPackage, type, "domain.valueobjects");
    }

    private void collectTypeImports(
            Set<String> imports,
            String contextRoot,
            String currentPackage,
            String type,
            String defaultProjectPackage
    ) {
        Set<String> jdkImports = new LinkedHashSet<>();
        for (String referenced : java.referencedTypes(type)) {
            String jdkImport = java.importsFor(List.of(referenced)).trim();
            if (!jdkImport.isBlank()) {
                for (String line : jdkImport.split("\\R")) {
                    if (line.startsWith("import ") && line.endsWith(";")) {
                        jdkImports.add(line.substring("import ".length(), line.length() - 1));
                    }
                }
            } else if (!contextRoot.isBlank() && !referenced.contains(".") && !isJavaLang(referenced)
                    && !"void".equals(referenced) && !isPrimitive(referenced)) {
                String targetPackage = targetPackageFor(referenced, defaultProjectPackage);
                if (!currentPackage.equals(targetPackage)) {
                    imports.add(contextRoot + "." + targetPackage + "." + referenced);
                }
            }
        }
        imports.addAll(jdkImports);
    }

    private String targetPackageFor(String referenced, String defaultProjectPackage) {
        if (referenced.endsWith("Exception")) {
            return "domain.exceptions";
        }
        if (referenced.endsWith("Snapshot")) {
            return "domain.snapshots";
        }
        if (referenced.endsWith("Id") || referenced.endsWith("Status")) {
            return "domain.valueobjects";
        }
        return defaultProjectPackage;
    }

    private List<MethodSpec> adapterMethods(Path projectRoot, HiveConfig config, Path contextDir, List<String> portTypes)
            throws IOException {
        List<MethodSpec> methods = new ArrayList<>();
        for (String portType : portTypes) {
            Path portPath = contextDir.resolve("application/ports/out").resolve(portType + ".java");
            if (Files.exists(portPath)) {
                methods.addAll(extractPortMethods(Files.readString(portPath)));
            }
        }
        return methods;
    }

    private List<MethodSpec> extractPortMethods(String source) {
        List<MethodSpec> methods = new ArrayList<>();
        for (String rawLine : source.lines().toList()) {
            String line = rawLine.trim();
            if (line.isBlank() || line.startsWith("//") || line.startsWith("public interface")
                    || line.startsWith("package ") || line.startsWith("import ") || line.startsWith("}")) {
                continue;
            }
            if (line.endsWith(";") && line.contains("(")) {
                methods.add(java.parseMethod(line.substring(0, line.length() - 1)));
            }
        }
        return methods;
    }

    private String factoryMethod(String typeName, List<FieldSpec> fields) {
        return """

                    public static %s of(%s) {
                        return new %s(%s);
                    }
                """.formatted(typeName, recordParametersSingleLine(fields), typeName, argumentList(fields));
    }

    private String scalarFactoryMethod(String typeName, String valueType) {
        return """

                    public static %s of(%s value) {
                        return new %s(value);
                    }
                """.formatted(typeName, valueType, typeName);
    }

    private String capitalize(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        return Character.toUpperCase(value.charAt(0)) + value.substring(1);
    }

    private boolean isPrimitive(String type) {
        return switch (type) {
            case "void", "boolean", "byte", "short", "int", "long", "float", "double", "char" -> true;
            default -> false;
        };
    }

    private String recordParametersSingleLine(List<FieldSpec> fields) {
        return fields.stream()
                .map(field -> field.type() + " " + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String recordParametersMultiline(List<FieldSpec> fields) {
        if (fields.isEmpty()) {
            return "";
        }
        if (fields.size() == 1) {
            FieldSpec field = fields.getFirst();
            return field.type() + " " + field.name();
        }
        StringBuilder parameters = new StringBuilder("\n");
        for (int i = 0; i < fields.size(); i++) {
            FieldSpec field = fields.get(i);
            parameters.append("        ").append(field.type()).append(" ").append(field.name());
            if (i < fields.size() - 1) {
                parameters.append(",");
            }
            parameters.append("\n");
        }
        return parameters.toString();
    }

    private String argumentList(List<FieldSpec> fields) {
        return fields.stream()
                .map(FieldSpec::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String requireScalarType(String type) {
        if (type == null || type.isBlank()) {
            return "String";
        }
        return switch (type) {
            case "String", "UUID", "Integer", "Long", "BigDecimal", "Boolean" -> type;
            default -> throw new IllegalArgumentException("Unsupported value object type: " + type);
        };
    }

    private String requireIdentifierType(String type) {
        if (type == null || type.isBlank()) {
            return "UUID";
        }
        return switch (type) {
            case "UUID", "Long", "String" -> type;
            default -> throw new IllegalArgumentException("Unsupported identifier type: " + type);
        };
    }

    private String requireEnumConstant(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Invalid enum value: " + value);
        }
        String resolved = value.toUpperCase(Locale.ROOT);
        for (int i = 0; i < resolved.length(); i++) {
            char c = resolved.charAt(i);
            if (!(c == '_' || Character.isDigit(c) || c >= 'A' && c <= 'Z')) {
                throw new IllegalArgumentException("Invalid enum value: " + value);
            }
        }
        if (!Character.isJavaIdentifierStart(resolved.charAt(0))) {
            throw new IllegalArgumentException("Invalid enum value: " + value);
        }
        return resolved;
    }

    private String importsForScalars(List<String> types) {
        Set<String> imports = new LinkedHashSet<>();
        for (String type : types) {
            if ("UUID".equals(type)) {
                imports.add("java.util.UUID");
            } else if ("BigDecimal".equals(type)) {
                imports.add("java.math.BigDecimal");
            } else if ("Instant".equals(type)) {
                imports.add("java.time.Instant");
            }
        }
        return renderImports(imports);
    }

    private String importsForDomainTypes(
            String contextRoot,
            String currentDomainPackage,
            String idType,
            List<FieldSpec> fields,
            boolean aggregate
    ) {
        Set<String> imports = new LinkedHashSet<>();
        if (idType != null) {
            collectTypeImports(imports, contextRoot, currentDomainPackage, idType);
        }
        for (FieldSpec field : fields) {
            collectTypeImports(imports, contextRoot, currentDomainPackage, field.type());
        }
        if (aggregate) {
            imports.remove("java.util.List");
        }
        return renderImports(imports);
    }

    private void addTypeImport(Set<String> imports, String contextRoot, String currentDomainPackage, String type) {
        if (isJavaLang(type)) {
            return;
        }
        switch (type) {
            case "UUID" -> imports.add("java.util.UUID");
            case "BigDecimal" -> imports.add("java.math.BigDecimal");
            case "Instant" -> imports.add("java.time.Instant");
            default -> {
                String targetDomainPackage = type.endsWith("Exception") ? "domain.exceptions" : "domain.valueobjects";
                if (!currentDomainPackage.equals(targetDomainPackage)) {
                    imports.add(contextRoot + "." + targetDomainPackage + "." + type);
                }
            }
        }
    }

    private boolean isJavaLang(String type) {
        return switch (type) {
            case "String", "Integer", "Long", "Boolean", "Double", "Float", "Short", "Byte", "Character" -> true;
            default -> false;
        };
    }

    private String renderImports(Set<String> imports) {
        if (imports.isEmpty()) {
            return "";
        }
        StringBuilder rendered = new StringBuilder();
        for (String importName : new java.util.TreeSet<>(imports)) {
            rendered.append("import ").append(importName).append(";\n");
        }
        return rendered.append("\n").toString();
    }

    private String validationLines(String valueType, ValueObjectSpec spec) {
        StringBuilder lines = new StringBuilder();
        if (spec.notNull()) {
            lines.append("        if (value == null) {\n");
            lines.append("            throw new IllegalArgumentException(\"value must not be null\");\n");
            lines.append("        }\n");
        }
        if (spec.notBlank()) {
            requireStringConstraint(valueType, "--not-blank");
            lines.append("        if (value == null || value.isBlank()) {\n");
            lines.append("            throw new IllegalArgumentException(\"value must not be blank\");\n");
            lines.append("        }\n");
        }
        if (spec.minLength() != null) {
            requireStringConstraint(valueType, "--min-length");
            lines.append("        if (value == null || value.length() < ").append(spec.minLength()).append(") {\n");
            lines.append("            throw new IllegalArgumentException(\"value length must be at least ")
                    .append(spec.minLength()).append("\");\n");
            lines.append("        }\n");
        }
        if (spec.maxLength() != null) {
            requireStringConstraint(valueType, "--max-length");
            lines.append("        if (value != null && value.length() > ").append(spec.maxLength()).append(") {\n");
            lines.append("            throw new IllegalArgumentException(\"value length must be at most ")
                    .append(spec.maxLength()).append("\");\n");
            lines.append("        }\n");
        }
        if (spec.pattern() != null) {
            requireStringConstraint(valueType, "--pattern");
            lines.append("        if (value == null || !value.matches(\"").append(escapeJavaString(spec.pattern())).append("\")) {\n");
            lines.append("            throw new IllegalArgumentException(\"value must match pattern\");\n");
            lines.append("        }\n");
        }
        if (spec.min() != null) {
            appendMinCheck(lines, valueType, spec.min());
        }
        if (spec.max() != null) {
            appendMaxCheck(lines, valueType, spec.max());
        }
        if (lines.isEmpty()) {
            lines.append("        // TODO: add deterministic invariants when needed.\n");
        }
        return lines.toString();
    }

    private void requireStringConstraint(String valueType, String option) {
        if (!"String".equals(valueType)) {
            throw new IllegalArgumentException(option + " is only supported for String value objects");
        }
    }

    private void appendMinCheck(StringBuilder lines, String valueType, String min) {
        switch (valueType) {
            case "Integer", "Long" -> lines.append("        if (value == null || value < ").append(min).append(") {\n")
                    .append("            throw new IllegalArgumentException(\"value must be at least ").append(min).append("\");\n")
                    .append("        }\n");
            case "BigDecimal" -> lines.append("        if (value == null || value.compareTo(new BigDecimal(\"")
                    .append(escapeJavaString(min)).append("\")) < 0) {\n")
                    .append("            throw new IllegalArgumentException(\"value must be at least ").append(min).append("\");\n")
                    .append("        }\n");
            default -> throw new IllegalArgumentException("--min is only supported for Integer, Long, and BigDecimal value objects");
        }
    }

    private void appendMaxCheck(StringBuilder lines, String valueType, String max) {
        switch (valueType) {
            case "Integer", "Long" -> lines.append("        if (value == null || value > ").append(max).append(") {\n")
                    .append("            throw new IllegalArgumentException(\"value must be at most ").append(max).append("\");\n")
                    .append("        }\n");
            case "BigDecimal" -> lines.append("        if (value == null || value.compareTo(new BigDecimal(\"")
                    .append(escapeJavaString(max)).append("\")) > 0) {\n")
                    .append("            throw new IllegalArgumentException(\"value must be at most ").append(max).append("\");\n")
                    .append("        }\n");
            default -> throw new IllegalArgumentException("--max is only supported for Integer, Long, and BigDecimal value objects");
        }
    }

    private String escapeJavaString(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private String fieldDeclarations(List<FieldSpec> fields) {
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("    private final ").append(field.type()).append(" ").append(field.name()).append(";\n");
        }
        return lines.isEmpty() ? "" : lines.append("\n").toString();
    }

    private String constructorParameters(List<FieldSpec> fields) {
        StringBuilder parameters = new StringBuilder();
        for (FieldSpec field : fields) {
            parameters.append(", ").append(field.type()).append(" ").append(field.name());
        }
        return parameters.toString();
    }

    private String constructorAssignments(List<FieldSpec> fields) {
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("        this.").append(field.name()).append(" = ").append(field.name()).append(";\n");
        }
        return lines.toString();
    }

    private String accessors(List<FieldSpec> fields) {
        StringBuilder lines = new StringBuilder();
        for (FieldSpec field : fields) {
            lines.append("\n    public ").append(field.type()).append(" ").append(field.name()).append("() {\n");
            lines.append("        return ").append(field.name()).append(";\n");
            lines.append("    }\n");
        }
        return lines.toString();
    }

    public record ValueObjectSpec(
            String name,
            String type,
            boolean notNull,
            boolean notBlank,
            String min,
            String max,
            Integer minLength,
            Integer maxLength,
            String pattern,
            List<String> fieldValues,
            boolean factory
    ) {
        public ValueObjectSpec(
                String name,
                String type,
                boolean notNull,
                boolean notBlank,
                String min,
                String max,
                Integer minLength,
                Integer maxLength,
                String pattern
        ) {
            this(name, type, notNull, notBlank, min, max, minLength, maxLength, pattern, List.of(), false);
        }
    }

    public record ClassSpec(
            String name,
            List<String> fieldValues,
            boolean getters,
            boolean setters,
            boolean constructor,
            boolean allArgsConstructor
    ) {
    }
}
