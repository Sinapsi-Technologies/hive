package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class FileScaffolder {
    private final HiveConfigLoader configLoader = new HiveConfigLoader();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();
    private final NameResolver names = new NameResolver();

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
        String typeName = names.requireJavaTypeName(useCaseName);
        String base = applicationPackage(config, moduleName);
        Path baseDir = names.packageDirectory(config.javaSourceRoot(projectRoot), base);

        String commandPackage = base + ".ports.in.commands";
        String inputPortPackage = base + ".ports.in";
        String servicePackage = base + ".services";

        List<Path> created = new ArrayList<>();
        created.add(write(
                baseDir.resolve("ports/in/commands").resolve(typeName + "Command.java"),
                commandTemplate(commandPackage, typeName),
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
                    commandFactoryTemplate(commandPackage, typeName),
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
        String typeName = withSuffix(names.requireJavaTypeName(portName), "Port");
        String contextRoot = contextRootPackage(config, moduleName);
        String portPackage = contextRoot + ".application.ports.out";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("application/ports/out").resolve(typeName + ".java"),
                outputPortTemplate(portPackage, typeName),
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
        String adapterType = withSuffix(names.requireJavaTypeName(adapterName), "Adapter");
        String portType = withSuffix(names.requireJavaTypeName(portName), "Port");
        String contextRoot = contextRootPackage(config, moduleName);
        String adapterPackage = contextRoot + ".infrastructure.adapters.out";
        String portPackage = contextRoot + ".application.ports.out";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);

        List<Path> created = new ArrayList<>();
        created.add(write(
                contextDir.resolve("infrastructure/adapters/out").resolve(adapterType + ".java"),
                outboundAdapterTemplate(adapterPackage, portPackage, adapterType, portType),
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

    private String commandTemplate(String packageName, String typeName) {
        return """
                package %s;

                import io.sinapsi.hive.core.command.Command;

                public record %sCommand() implements Command {
                    // TODO: add the fields this command carries
                }
                """.formatted(packageName, typeName);
    }

    private String commandFactoryTemplate(String packageName, String typeName) {
        return """
                package %s;

                import io.sinapsi.hive.factory.AbstractCommandFactory;

                public final class %sCommandFactory extends AbstractCommandFactory<%sCommand> {
                    public %sCommand create() {
                        return validate(new %sCommand());
                    }
                }
                """.formatted(packageName, typeName, typeName, typeName, typeName);
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

    private String outputPortTemplate(String packageName, String typeName) {
        return """
                package %s;

                import io.sinapsi.hive.core.port.OutputPort;

                public interface %s extends OutputPort {
                    // TODO: declare the capability methods this port exposes
                }
                """.formatted(packageName, typeName);
    }

    private String outboundAdapterTemplate(
            String adapterPackage,
            String portPackage,
            String adapterType,
            String portType
    ) {
        return """
                package %s;

                import %s.%s;

                public final class %s implements %s {
                    // TODO: implement the %s methods
                }
                """.formatted(adapterPackage, portPackage, portType, adapterType, portType, portType);
    }
}
