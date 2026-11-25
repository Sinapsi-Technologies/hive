package io.sinapsi.hive.commons.project;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HiveConfigLoaderTest {

    private final HiveConfigLoader loader = new HiveConfigLoader();

    @Test
    void loadsValuesAndIgnoresComments(@TempDir Path project) throws Exception {

        Files.writeString(project.resolve("hive.yml"), "# comment\nbasePackage: com.acme.orders # inline\nlayout: modular\n");

        HiveConfig config = loader.load(project);

        assertEquals("com.acme.orders", config.basePackage());
        assertEquals("modular", config.layout());
    }

    @Test
    void rendersConfiguration() {

        String rendered = loader.render(HiveConfig.defaults());

        assertEquals("basePackage: com.example.app\nlayout: single\njavaSourceRoot: src/main/java\ntestSourceRoot: src/test/java\n", rendered);
    }
}
