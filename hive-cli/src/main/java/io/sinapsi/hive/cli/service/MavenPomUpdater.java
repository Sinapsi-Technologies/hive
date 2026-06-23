package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MavenPomUpdater {
    private static final String HIVE_GROUP_ID = "io.sinapsi.hive";
    private static final String ARCHUNIT_GROUP_ID = "com.tngtech.archunit";

    /** Matches a closing {@code </dependencies>} tag, capturing its leading indentation. */
    private static final Pattern CLOSING_DEPENDENCIES = Pattern.compile("([ \\t]*)</dependencies>");

    public Path ensureBasePom(Path projectRoot, HiveConfig config) throws IOException {
        Path pom = projectRoot.resolve("pom.xml");
        if (!Files.exists(pom)) {
            Files.writeString(pom, minimalPom(config));
        }
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-core", HiveVersions.hive(), null);
        return pom;
    }

    public void ensureValidatorDependency(Path projectRoot, HiveConfig config) throws IOException {
        ensureBasePom(projectRoot, config);
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-validator", HiveVersions.hive(), null);
    }

    public void ensureArchUnitDependencies(Path projectRoot, HiveConfig config) throws IOException {
        ensureBasePom(projectRoot, config);
        ensureDependency(projectRoot, HIVE_GROUP_ID, "hive-archunit", HiveVersions.hive(), "test");
        ensureDependency(projectRoot, ARCHUNIT_GROUP_ID, "archunit-junit5", HiveVersions.archUnit(), "test");
    }

    private void ensureDependency(Path projectRoot, String groupId, String artifactId, String version, String scope)
            throws IOException {
        Path pom = projectRoot.resolve("pom.xml");
        String content = Files.readString(pom);
        if (containsDependency(content, groupId, artifactId)) {
            return;
        }

        String dependency = dependencyBlock(groupId, artifactId, version, scope);
        Matcher closing = CLOSING_DEPENDENCIES.matcher(content);
        String updated;
        if (closing.find()) {
            String indent = closing.group(1);
            updated = content.substring(0, closing.start())
                    + dependency + "\n" + indent + "</dependencies>"
                    + content.substring(closing.end());
        } else {
            updated = content.replaceFirst(
                    "(\\s*)</project>\\s*$",
                    Matcher.quoteReplacement("\n  <dependencies>\n" + dependency + "\n  </dependencies>\n</project>\n")
            );
        }
        Files.writeString(pom, updated);
    }

    private boolean containsDependency(String pom, String groupId, String artifactId) {
        return pom.contains("<groupId>" + groupId + "</groupId>")
                && pom.contains("<artifactId>" + artifactId + "</artifactId>");
    }

    private String dependencyBlock(String groupId, String artifactId, String version, String scope) {
        String scopeBlock = scope == null || scope.isBlank()
                ? ""
                : "\n      <scope>" + scope + "</scope>";
        return "    <dependency>\n"
                + "      <groupId>" + groupId + "</groupId>\n"
                + "      <artifactId>" + artifactId + "</artifactId>\n"
                + "      <version>" + version + "</version>"
                + scopeBlock + "\n"
                + "    </dependency>";
    }

    private String minimalPom(HiveConfig config) {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0"
                         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
                  <modelVersion>4.0.0</modelVersion>

                  <groupId>%s</groupId>
                  <artifactId>hive-app</artifactId>
                  <version>0.1.0-SNAPSHOT</version>

                  <properties>
                    <maven.compiler.release>21</maven.compiler.release>
                    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
                  </properties>
                </project>
                """.formatted(config.basePackage());
    }
}
