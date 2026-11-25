package io.sinapsi.hive.commons.generators.infrastructure.inbound;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InboundAdapterGeneratorTest {

    @Test
    void generatesInboundAdapterAndTransport(@TempDir Path project) throws Exception {

        HiveConfig config = HiveConfig.defaults();
        Path useCase = project.resolve("src/main/java/com/example/app/application/ports/in/CreateUserUseCase.java");
        Files.createDirectories(useCase.getParent());
        Files.writeString(useCase, "package com.example.app.application.ports.in;\npublic interface CreateUserUseCase {}\n");

        InboundAdapterGenerator generator = new InboundAdapterGenerator();
        generator.generate(
                project,
                config,
                new InboundGenerationRequest(
                        InboundAdapterType.REST,
                        null,
                        "User",
                        List.of(InboundOperation.useCaseOnly("CreateUser")),
                        false
                )
        );

        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/infrastructure/adapters/in/rest/UserController.java")));
        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/infrastructure/adapters/in/rest/CreateUserRequest.java")));
    }
}
