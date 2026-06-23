package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.FileScaffolder;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "archtest", mixinStandardHelpOptions = true, description = "Create an ArchUnit test using Hive rules.")
public final class CreateArchTestCommand implements Callable<Integer> {
    @Option(names = "--force", description = "Overwrite an existing architecture test.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        List<Path> created = new FileScaffolder().createArchTest(root, config, force);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create archtest");
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created ArchUnit architecture test.");
        }
        return 0;
    }
}
