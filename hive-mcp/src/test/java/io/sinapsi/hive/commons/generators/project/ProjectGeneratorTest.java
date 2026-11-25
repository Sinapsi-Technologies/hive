package io.sinapsi.hive.commons.generators.project;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectGeneratorTest {

    @Test
    void initializesProjectAndCreatesModule(@TempDir Path project) throws Exception {

        ProjectGenerator generator = new ProjectGenerator();

        generator.init(project, HiveConfig.defaults(), false, true);
        generator.module(project, HiveConfig.defaults(), "users");

        assertTrue(Files.exists(project.resolve(".hive-project")));
        assertTrue(Files.exists(project.resolve("pom.xml")));
        assertTrue(Files.exists(project.resolve("README.md")));
        assertTrue(Files.isDirectory(project.resolve("src/main/java/com/example/app/modules/users/domain/entities")));
    }
}
