package io.sinapsi.hive.cli.model;

import java.nio.file.Path;

public record HiveConfig(
        String basePackage,
        String layout,
        String javaSourceRoot,
        String testSourceRoot
) {
    public static HiveConfig defaults() {
        return new HiveConfig("com.example.app", "single", "src/main/java", "src/test/java");
    }

    public Path javaSourceRoot(Path projectRoot) {
        return projectRoot.resolve(javaSourceRoot);
    }

    public Path testSourceRoot(Path projectRoot) {
        return projectRoot.resolve(testSourceRoot);
    }
}
