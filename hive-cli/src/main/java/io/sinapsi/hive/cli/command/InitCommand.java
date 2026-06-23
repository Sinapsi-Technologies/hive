package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "init", mixinStandardHelpOptions = true, description = "Initialize a hive project.")
public final class InitCommand implements Callable<Integer> {
    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--readme", description = "Create a small README when missing.")
    boolean readme;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = Path.of(".");
        List<Path> created = new FileScaffolder().init(root, HiveConfig.defaults(), force, readme);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "init");
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Initialized hive project.");
        }
        return 0;
    }
}
