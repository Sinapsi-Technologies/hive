package io.sinapsi.hive.commons;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.model.ClassSpec;
import io.sinapsi.hive.commons.model.ValueObjectSpec;
import io.sinapsi.hive.commons.project.HiveConfigLoader;
import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.generators.application.ApplicationGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.InfrastructureGenerator;
import io.sinapsi.hive.commons.generators.project.ProjectGenerator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** Application operations exposed through the MCP adapters. */
public final class HiveOperations {
    private final HiveConfigLoader configs = new HiveConfigLoader();
    private final DomainGenerator domain = new DomainGenerator();
    private final ApplicationGenerator application = new ApplicationGenerator();
    private final InfrastructureGenerator infrastructure = new InfrastructureGenerator();
    private final ProjectGenerator project = new ProjectGenerator();

    public List<Path> createValueObject(Path projectRoot, String name, String type,
                                        boolean notNull, boolean notBlank, boolean force) throws IOException {
        return createValueObject(projectRoot, null, name, type, notNull, notBlank, force);
    }

    public List<Path> createValueObject(Path projectRoot, String module, String name, String type,
                                        boolean notNull, boolean notBlank, boolean force) throws IOException {

        HiveConfig config = configs.load(projectRoot);

        return domain.valueObject(
                projectRoot, config, module,
                new ValueObjectSpec(name, type, notNull, notBlank,
                        null, null, null, null, null, List.of(), false),
                force);

    }

    public List<Path> init(Path projectRoot, boolean force, boolean readme) throws IOException {

        return project.init(projectRoot, HiveConfig.defaults(), force, readme);

    }

    public List<String> check(Path projectRoot) throws IOException {

        return project.check(projectRoot, configs.load(projectRoot));

    }

    public List<Path> create(Path projectRoot, String kind, String module, String name,
                             String type, String id, List<String> fields, List<String> values,
                             String message, List<String> ports, String group, boolean force,
                             boolean notNull, boolean notBlank, String min, String max,
                             Integer minLength, Integer maxLength, String pattern, boolean factory,
                             boolean getters, boolean setters, boolean constructor,
                             boolean allArgsConstructor) throws IOException {

        HiveConfig config = configs.load(projectRoot);

        return switch (kind) {
            case "vo" -> domain.valueObject(projectRoot, config, module,
                    new ValueObjectSpec(name, type, notNull, notBlank, min, max,
                            minLength, maxLength, pattern, fields, factory), force);
            case "id" -> domain.identifier(projectRoot, config, module, name, type, force);
            case "entity" -> domain.entity(projectRoot, config, module, name, id, fields, force);
            case "aggregate" -> domain.aggregate(projectRoot, config, module, name, id, fields, force);
            case "enum" -> domain.enumeration(projectRoot, config, module, name, values, force);
            case "event" -> domain.event(projectRoot, config, module, name, fields, force);
            case "exception" -> domain.exception(projectRoot, config, module, name, message, force);
            case "domainService" -> domain.service(projectRoot, config, module, name, force);
            case "snapshot" -> domain.snapshot(projectRoot, config, module, name, fields, force);
            case "command" -> application.command(projectRoot, config, module, name, fields, force);
            case "class" -> application.plainClass(projectRoot, config, module,
                    new ClassSpec(name, fields, getters, setters, constructor, allArgsConstructor), force);
            case "record" -> application.record(projectRoot, config, module, name, fields, force);
            case "usecase" -> application.useCase(projectRoot, config, module, name, force, factory, fields);
            case "port" -> application.port(projectRoot, config, module, name, fields, force);
            case "adapter" -> infrastructure.adapter(projectRoot, config, module, name, ports, group, force);
            case "module" -> project.module(projectRoot, config, name);
            case "archtest" -> project.archTest(projectRoot, config, force);
            default -> throw new IllegalArgumentException("Unsupported create kind: " + kind);
        };

    }

    public List<Path> createAggregate(Path projectRoot, String module, String name, String id,
                                      List<String> fields, boolean force, String llmInstruction) throws IOException {
        HiveConfig config = configs.load(projectRoot);
        return domain.aggregate(projectRoot, config, module, name, id, fields, force, llmInstruction);
    }
}
