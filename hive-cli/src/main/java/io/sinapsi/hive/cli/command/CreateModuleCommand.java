package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.FileScaffolder;
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

@Command(name = "module", mixinStandardHelpOptions = true, description = "Create a modular hexagonal context.")
public final class CreateModuleCommand implements Callable<Integer> {
    @Parameters(paramLabel = "moduleName")
    String moduleName;

    @Option(names = "--json", description = "Print the created folders as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        List<Path> created = new FileScaffolder().createModule(root, config, moduleName);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create module");
            output.put("name", moduleName);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created module " + moduleName + ".");
        }
        return 0;
    }
}
