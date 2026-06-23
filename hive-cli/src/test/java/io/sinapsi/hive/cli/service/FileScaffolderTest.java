package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScaffolderTest {
    @TempDir
    Path tempDir;

    private final FileScaffolder scaffolder = new FileScaffolder();

    @Test
    void initCreatesMarkerConfigAndSourceRoots() throws Exception {
        scaffolder.init(tempDir, HiveConfig.defaults(), false, false);

        assertTrue(Files.exists(tempDir.resolve(".hive-project")));
        assertTrue(Files.exists(tempDir.resolve("hive.yml")));
        assertTrue(Files.isDirectory(tempDir.resolve("src/main/java")));
        assertTrue(Files.isDirectory(tempDir.resolve("src/test/java")));
        assertTrue(Files.readString(tempDir.resolve("hive.yml")).contains("layout: single"));
        assertPomContains("hive-core");
    }

    @Test
    void createUseCaseGeneratesExpectedFiles() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, false);

        Path packageRoot = tempDir.resolve("src/main/java/com/example/app/application");
        assertTrue(Files.exists(packageRoot.resolve("ports/in/commands/CreateUserCommand.java")));
        assertTrue(Files.exists(packageRoot.resolve("ports/in/CreateUserUseCase.java")));
        assertTrue(Files.exists(packageRoot.resolve("services/CreateUserService.java")));
        assertTrue(Files.readString(packageRoot.resolve("services/CreateUserService.java")).contains("TODO"));
        assertFilesCompile(List.of(
                packageRoot.resolve("ports/in/commands/CreateUserCommand.java"),
                packageRoot.resolve("ports/in/CreateUserUseCase.java"),
                packageRoot.resolve("services/CreateUserService.java")
        ));
    }

    @Test
    void createUseCaseWithFactoryGeneratesExpectedFactory() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, true);

        Path packageRoot = tempDir.resolve("src/main/java/com/example/app/application");
        assertTrue(Files.exists(packageRoot.resolve("ports/in/commands/CreateUserCommandFactory.java")));
        assertPomContains("hive-validator");
        assertFilesCompile(List.of(
                packageRoot.resolve("ports/in/commands/CreateUserCommand.java"),
                packageRoot.resolve("ports/in/commands/CreateUserCommandFactory.java"),
                packageRoot.resolve("ports/in/CreateUserUseCase.java"),
                packageRoot.resolve("services/CreateUserService.java")
        ));
    }

    @Test
    void createModuleGeneratesExpectedFolders() throws Exception {
        HiveConfig config = new HiveConfig("com.example.app", "modular", "src/main/java", "src/test/java");
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createModule(tempDir, config, "customer");

        Path moduleRoot = tempDir.resolve("src/main/java/com/example/app/modules/customer");
        List<String> folders = List.of(
                "commons",
                "configurations",
                "application/ports/in/commands",
                "application/ports/out",
                "application/services",
                "domain/valueobjects",
                "domain/aggregates",
                "domain/events",
                "domain/entities",
                "domain/services",
                "domain/snapshots",
                "domain/exceptions",
                "infrastructure/configs",
                "infrastructure/adapters/in",
                "infrastructure/adapters/out"
        );
        for (String folder : folders) {
            assertTrue(Files.isDirectory(moduleRoot.resolve(folder)), "Missing " + folder);
        }
    }

    @Test
    void createArchTestGeneratesExpectedArchitectureTest() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createArchTest(tempDir, config, false);

        Path architectureTest = tempDir.resolve("src/test/java/com/example/app/ArchitectureTest.java");
        assertTrue(Files.exists(architectureTest));
        assertTrue(Files.readString(architectureTest).contains("HexagonalRules.allBaseRules(\"com.example.app\")"));
        assertPomContains("hive-archunit");
        assertPomContains("archunit-junit5");
        assertFilesCompile(List.of(architectureTest));
    }

    @Test
    void createPortGeneratesCompilableOutputPort() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUser", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        assertTrue(Files.exists(port));
        assertTrue(Files.readString(port).contains("extends OutputPort"));
        assertTrue(Files.readString(port).contains("TODO"));
        assertFilesCompile(List.of(port));
    }

    @Test
    void createPortDoesNotDuplicatePortSuffix() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUserPort", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        assertTrue(Files.exists(port));
    }

    @Test
    void createAdapterGeneratesCompilableAdapterImplementingPort() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUser", false);
        scaffolder.createAdapter(tempDir, config, null, "InMemorySaveUser", "SaveUser", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        Path adapter = tempDir.resolve(
                "src/main/java/com/example/app/infrastructure/adapters/out/InMemorySaveUserAdapter.java");
        assertTrue(Files.exists(adapter));
        assertTrue(Files.readString(adapter).contains("implements SaveUserPort"));
        assertTrue(Files.readString(adapter).contains("TODO"));
        assertFilesCompile(List.of(port, adapter));
    }

    @Test
    void createUseCaseDoesNotDuplicateMavenDependencies() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, true);
        scaffolder.createUseCase(tempDir, config, null, "CreateUser", true, true);

        String pom = Files.readString(tempDir.resolve("pom.xml"));
        assertEquals(1, occurrences(pom, "<artifactId>hive-core</artifactId>"));
        assertEquals(1, occurrences(pom, "<artifactId>hive-validator</artifactId>"));
    }

    private void assertFilesCompile(List<Path> sourceFiles) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path output = tempDir.resolve("compiled");
        Files.createDirectories(output);
        List<String> arguments = new ArrayList<>();
        arguments.add("--release");
        arguments.add("21");
        arguments.add("-classpath");
        arguments.add(System.getProperty("java.class.path"));
        arguments.add("-d");
        arguments.add(output.toString());
        sourceFiles.stream()
                .map(Path::toString)
                .forEach(arguments::add);

        int result = compiler.run(
                null,
                null,
                null,
                arguments.toArray(String[]::new)
        );

        assertEquals(0, result);
    }

    private void assertPomContains(String artifactId) throws Exception {
        assertTrue(
                Files.readString(tempDir.resolve("pom.xml")).contains("<artifactId>" + artifactId + "</artifactId>"),
                "Missing Maven dependency " + artifactId
        );
    }

    private int occurrences(String text, String value) {
        int count = 0;
        int index = text.indexOf(value);
        while (index >= 0) {
            count++;
            index = text.indexOf(value, index + value.length());
        }
        return count;
    }
}
