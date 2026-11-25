package io.sinapsi.hive.commons.project;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class MavenPomUpdaterTest {

    @Test
    void createsBasePomAndDependenciesIdempotently(@TempDir Path project) throws Exception {

        MavenPomUpdater updater = new MavenPomUpdater();

        updater.ensureBasePom(project, HiveConfig.defaults());
        updater.ensureBasePom(project, HiveConfig.defaults());

        String pom = Files.readString(project.resolve("pom.xml"));

        assertTrue(pom.contains("<artifactId>hive-core</artifactId>"));
        assertEquals(1, count(pom, "<artifactId>hive-core</artifactId>"));
        assertFalse(pom.contains("\n\n\n"));
    }

    private int count(String value, String fragment) {

        return (value.length() - value.replace(fragment, "").length()) / fragment.length();
    }
}
