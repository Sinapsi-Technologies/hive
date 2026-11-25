package io.sinapsi.hive.commons.generators.domain;

import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DomainEntityGeneratorTest {

    @Test
    void createsEntityFromExternalTemplate(@TempDir Path project) throws Exception {

        DomainGenerator generator = new DomainGenerator();

        generator.entity(project, HiveConfig.defaults(), null, "User", "UserId", List.of("email:String"), false);

        String source = Files.readString(project.resolve("src/main/java/com/example/app/domain/entities/User.java"));

        assertTrue(source.contains("public final class User"));
        assertTrue(source.contains("private final UserId id;"));
        assertTrue(source.contains("String email"));
    }
}
