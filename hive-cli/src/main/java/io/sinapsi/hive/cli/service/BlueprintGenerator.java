package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.blueprint.BlueprintKindRegistry;
import io.sinapsi.hive.cli.service.blueprint.BlueprintParser;
import io.sinapsi.hive.cli.service.blueprint.BlueprintType;
import io.sinapsi.hive.cli.service.FileScaffolder.ClassSpec;
import io.sinapsi.hive.cli.service.FileScaffolder.ValueObjectSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class BlueprintGenerator {
    private final FileScaffolder scaffolder = new FileScaffolder();
    private final BlueprintParser parser = new BlueprintParser();
    private final BlueprintKindRegistry kinds = new BlueprintKindRegistry();

    public List<Path> generateFile(Path projectRoot, HiveConfig config, Path modelFile, boolean force) throws IOException {
        List<BlueprintType> types = parser.parse(modelFile);
        List<Path> created = new ArrayList<>();
        for (BlueprintType type : types) {
            created.addAll(generate(projectRoot, config, type, force));
        }
        return created;
    }

    public List<Path> generateAll(Path projectRoot, HiveConfig config, boolean force) throws IOException {
        Path modelRoot = projectRoot.resolve(".hive/model");
        if (!Files.isDirectory(modelRoot)) {
            return List.of();
        }
        List<Path> files;
        try (var stream = Files.walk(modelRoot)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .sorted(Comparator.comparing(path -> projectRoot.relativize(path).toString()))
                    .toList();
        }
        List<Path> created = new ArrayList<>();
        for (Path file : files) {
            created.addAll(generateFile(projectRoot, config, file, force));
        }
        return created;
    }

    private List<Path> generate(Path projectRoot, HiveConfig config, BlueprintType type, boolean force) throws IOException {
        return switch (kinds.require(type.kind()).name()) {
            case "id" -> scaffolder.createIdentifier(projectRoot, config, type.module(), type.name(), blankToNull(type.type()), force);
            case "vo" -> scaffolder.createValueObject(
                    projectRoot,
                    config,
                    type.module(),
                    new ValueObjectSpec(
                            type.name(),
                            blankToNull(type.type()),
                            type.notNull(),
                            type.notBlank(),
                            blankToNull(type.min()),
                            blankToNull(type.max()),
                            type.minLength(),
                            type.maxLength(),
                            blankToNull(type.pattern()),
                            type.fields(),
                            type.factory()
                    ),
                    force
            );
            case "entity" -> scaffolder.createEntity(projectRoot, config, type.module(), type.name(), type.id(), type.fields(), force);
            case "aggregate" -> scaffolder.createAggregate(projectRoot, config, type.module(), type.name(), type.id(), type.fields(), force);
            case "enum" -> scaffolder.createEnum(projectRoot, config, type.module(), type.name(), type.values(), force);
            case "event" -> scaffolder.createEvent(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "exception" -> scaffolder.createException(projectRoot, config, type.module(), type.name(), blankToNull(type.message()), force);
            case "domainService" -> scaffolder.createDomainService(projectRoot, config, type.module(), type.name(), force);
            case "snapshot" -> scaffolder.createSnapshot(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "record" -> scaffolder.createRecord(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "class" -> scaffolder.createPlainClass(
                    projectRoot,
                    config,
                    type.module(),
                    new ClassSpec(type.name(), type.fields(), type.getters(), type.setters(), type.constructor(), type.allArgsConstructor()),
                    force
            );
            case "command" -> scaffolder.createCommand(projectRoot, config, type.module(), type.name(), type.fields(), force);
            case "outputPort" -> scaffolder.createPort(projectRoot, config, type.module(), type.name(), type.methods(), force);
            case "adapter" -> scaffolder.createAdapter(projectRoot, config, type.module(), type.name(), type.ports(), force);
            default -> throw new IllegalArgumentException("Unsupported blueprint kind: " + type.kind());
        };
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

}
