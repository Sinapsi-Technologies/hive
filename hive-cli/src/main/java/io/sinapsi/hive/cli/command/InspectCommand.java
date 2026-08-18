package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "inspect", mixinStandardHelpOptions = true, description = "Inspect deterministic Hive project metadata.")
public final class InspectCommand implements Callable<Integer> {
    @Parameters(index = "0", paramLabel = "TARGET", description = "config, model, or generated.")
    String target;

    @Option(names = "--json", description = "Print inspection result as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        Map<String, Object> output = switch (target) {
            case "config" -> configOutput(root, config);
            case "model" -> modelOutput(root);
            case "generated" -> generatedOutput(root);
            default -> throw new IllegalArgumentException("Unsupported inspect target: " + target);
        };
        if (json) {
            System.out.println(JsonOutput.render(output));
        } else {
            printHuman(output);
        }
        return 0;
    }

    private Map<String, Object> configOutput(Path root, HiveConfig config) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", "inspect config");
        output.put("projectRoot", root.toAbsolutePath().normalize().toString());
        output.put("basePackage", config.basePackage());
        output.put("layout", config.layout());
        output.put("javaSourceRoot", config.javaSourceRoot());
        output.put("testSourceRoot", config.testSourceRoot());
        return output;
    }

    private Map<String, Object> modelOutput(Path root) throws Exception {
        Path modelRoot = root.resolve(".hive/model");
        List<Path> models;
        if (Files.isDirectory(modelRoot)) {
            try (var stream = Files.walk(modelRoot)) {
                models = stream
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                        .sorted()
                        .toList();
            }
        } else {
            models = List.of();
        }
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", "inspect model");
        output.put("models", JsonOutput.relativePaths(root, models));
        return output;
    }

    private Map<String, Object> generatedOutput(Path root) {
        Path manifest = root.resolve(".hive/generated.yml");
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", "inspect generated");
        output.put("manifest", JsonOutput.relativePath(root, manifest));
        output.put("manifestExists", Files.exists(manifest));
        return output;
    }

    private void printHuman(Map<String, Object> output) {
        output.forEach((key, value) -> System.out.println(key + ": " + value));
    }
}
