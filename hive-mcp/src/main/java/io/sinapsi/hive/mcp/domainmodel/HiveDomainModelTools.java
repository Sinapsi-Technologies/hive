package io.sinapsi.hive.mcp.domainmodel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.sinapsi.hive.mcp.project.HiveProjectContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Component
public final class HiveDomainModelTools {
    private final HiveProjectContext projectContext;
    private final DomainModelValidator validator = new DomainModelValidator();
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    @Autowired
    public HiveDomainModelTools(HiveProjectContext projectContext) {
        this.projectContext = projectContext;
    }

    public HiveDomainModelTools(String projectRoot) {
        this(new HiveProjectContext(projectRoot, ""));
    }

    @McpTool(name = "hive_validate_domain_model", description = "Validate a structured Hive domain model before generation. Use this after the LLM has extracted bounded contexts, aggregates, invariants, behaviors, events, and use cases from requirements. The server checks schema version, Java names, duplicates, required identifiers, and event references; it does not write files.")
    public DomainModelValidation validate(DomainModel model) {
        return validator.validate(model);
    }

    @McpTool(name = "hive_save_domain_model", description = "Validate and persist a Hive domain model as .hive/model/domain-model.yml. Use this only after the LLM has produced a complete structured domain analysis. The file is written atomically and is not created when validation fails.")
    public Map<String, Object> save(DomainModel model) throws IOException {
        DomainModelValidation validation = validator.validate(model);
        if (!validation.valid()) {
            return Map.of("saved", false, "valid", false, "errors", validation.errors());
        }
        Path projectRoot = projectContext.current();
        Path directory = projectRoot.resolve(".hive/model");
        Files.createDirectories(directory);
        Path target = directory.resolve("domain-model.yml");
        Path temporary = Files.createTempFile(directory, "domain-model-", ".tmp");
        yaml.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), model);
        Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        return Map.of("saved", true, "valid", true, "file", projectRoot.relativize(target).toString());
    }
}
