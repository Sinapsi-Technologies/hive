package io.sinapsi.hive.commons.generators.application;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationGeneratorTest {

    @Test
    void generatesCommandAndUseCase(@TempDir Path project) throws Exception {

        ApplicationGenerator generator = new ApplicationGenerator();

        generator.useCase(project, HiveConfig.defaults(), null, "CreateUser", false, true, List.of("email:String"));
        generator.command(project, HiveConfig.defaults(), null, "CreateUser", List.of("email:String"), false);

        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java")));
        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/application/services/CreateUserService.java")));

        String useCase = Files.readString(project.resolve("src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java"));
        assertTrue(useCase.contains("record CreateUserCommand("));
        assertTrue(useCase.contains("String email"));
        assertTrue(useCase.contains("implements Command"));
        assertTrue(useCase.contains("extends AbstractCommandFactory<CreateUserCommand>"));
    }

    @Test
    void doesNotDuplicatePortSuffix(@TempDir Path project) throws Exception {
        new ApplicationGenerator().port(project, HiveConfig.defaults(), null,
                "TodoListRepositoryPort", List.of("void save(String value)"), false);

        assertTrue(Files.exists(project.resolve(
                "src/main/java/com/example/app/application/ports/out/TodoListRepositoryPort.java")));
        assertTrue(!Files.exists(project.resolve(
                "src/main/java/com/example/app/application/ports/out/TodoListRepositoryPortPort.java")));
    }

    @Test
    void importsGeneratedDomainTypesAndJdkTypesInPort(@TempDir Path project) throws Exception {
        Path valueObject = project.resolve("src/main/java/com/example/app/domain/valueobjects/UserId.java");
        Files.createDirectories(valueObject.getParent());
        Files.writeString(valueObject, "package com.example.app.domain.valueobjects; public record UserId(String value) {}\n");

        new ApplicationGenerator().port(project, HiveConfig.defaults(), null,
                "LoadUser", List.of("Optional<UserId> load(UserId id)"), false);

        String source = Files.readString(project.resolve(
                "src/main/java/com/example/app/application/ports/out/LoadUserPort.java"));
        assertTrue(source.contains("import java.util.Optional;"));
        assertTrue(source.contains("import com.example.app.domain.valueobjects.UserId;"));
    }
}
