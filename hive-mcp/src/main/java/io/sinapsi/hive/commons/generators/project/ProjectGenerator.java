package io.sinapsi.hive.commons.generators.project;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.project.HiveConfigLoader;
import io.sinapsi.hive.commons.project.MavenPomUpdater;
import io.sinapsi.hive.commons.support.NameResolver;
import io.sinapsi.hive.commons.template.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ProjectGenerator {
    private final HiveConfigLoader configs = new HiveConfigLoader();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();
    private final TemplateRenderer templates = new TemplateRenderer();
    private final NameResolver names = new NameResolver();

    public List<Path> init(Path root, HiveConfig config, boolean force, boolean readme) throws IOException {
        List<Path> created = new ArrayList<>();
        created.add(write(root.resolve(".hive-project"), "hive-toolkit\n", force));
        created.add(write(root.resolve("hive.yml"), configs.render(config), force));
        Files.createDirectories(config.javaSourceRoot(root));
        Files.createDirectories(config.testSourceRoot(root));
        created.add(pomUpdater.ensureBasePom(root, config));
        if (readme && !Files.exists(root.resolve("README.md"))) {
            created.add(write(root.resolve("README.md"), templates.render("project/README.md.ftl", Map.of("projectName", "Hive Project")), force));
        }
        return created;
    }

    public List<Path> module(Path root, HiveConfig config, String name) throws IOException {
        Path moduleRoot = names.packageDirectory(config.javaSourceRoot(root), config.basePackage() + ".modules." + names.modulePackageName(name));
        List<Path> created = new ArrayList<>();
        for (String directory : List.of("commons", "configurations", "application/ports/in/commands", "application/ports/in/views", "application/ports/out", "application/services", "domain/valueobjects", "domain/aggregates", "domain/events", "domain/entities", "domain/services", "domain/snapshots", "domain/exceptions", "infrastructure/configs", "infrastructure/adapters/in", "infrastructure/adapters/out")) {
            Path path = moduleRoot.resolve(directory);
            Files.createDirectories(path);
            created.add(path);
        }
        return created;
    }

    public List<Path> archTest(Path root, HiveConfig config, boolean force) throws IOException {
        Path target = config.testSourceRoot(root).resolve(config.basePackage().replace('.', '/')).resolve("ArchitectureTest.java");
        String source = templates.render("project/architecture-test.java.ftl", Map.of("packageName", config.basePackage(), "basePackage", config.basePackage()));
        write(target, source, force);
        pomUpdater.ensureArchUnitDependencies(root, config);
        return List.of(target);
    }

    public List<String> check(Path root, HiveConfig config) {
        List<String> warnings = new ArrayList<>();
        if (!Files.exists(root.resolve(".hive-project"))) warnings.add("Missing .hive-project marker");
        if (!Files.exists(root.resolve("hive.yml"))) warnings.add("Missing hive.yml");
        if (!Files.exists(root.resolve("pom.xml"))) warnings.add("Missing pom.xml");
        if (!Files.isDirectory(config.javaSourceRoot(root))) warnings.add("Missing java source root: " + config.javaSourceRoot());
        if (!Files.isDirectory(config.testSourceRoot(root))) warnings.add("Missing test source root: " + config.testSourceRoot());
        return warnings;
    }

    private Path write(Path path, String content, boolean force) throws IOException {
        if (Files.exists(path) && !force) throw new IOException("Refusing to overwrite existing file: " + path);
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        return path;
    }
}
