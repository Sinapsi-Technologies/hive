package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.BlueprintGenerator;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "generate", mixinStandardHelpOptions = true, description = "Generate artifacts from Hive blueprint files.")
public final class GenerateCommand implements Callable<Integer> {
    @Parameters(index = "0", arity = "0..1", paramLabel = "FILE", description = "Blueprint YAML file.")
    Path file;

    @Option(names = "--all", description = "Generate all .hive/model YAML files.")
    boolean all;

    @Option(names = "--force", description = "Overwrite generated files.")
    boolean force;

    @Option(names = "--json", description = "Print generated files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        if (!all && file == null) {
            throw new IllegalArgumentException("Provide a blueprint file or --all");
        }
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        BlueprintGenerator generator = new BlueprintGenerator();
        List<Path> created = all
                ? generator.generateAll(root, config, force)
                : generator.generateFile(root, config, root.resolve(file).normalize(), force);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "generate");
            output.put("all", all);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Generated " + created.size() + " artifact(s).");
        }
        return 0;
    }
}
