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
import java.util.Optional;
import java.util.concurrent.Callable;

@Command(name = "check", mixinStandardHelpOptions = true, description = "Check expected hive project folders.")
public final class CheckCommand implements Callable<Integer> {
    @Option(names = "--json", description = "Print the check result as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Optional<Path> located = new ProjectLocator().locate(Path.of("."));
        if (located.isEmpty()) {
            if (json) {
                Map<String, Object> output = new LinkedHashMap<>();
                output.put("command", "check");
                output.put("ok", false);
                output.put("error", "No .hive-project found.");
                System.out.println(JsonOutput.render(output));
            } else {
                System.err.println("No .hive-project found.");
            }
            return 2;
        }
        Path root = located.get();
        HiveConfig config = new HiveConfigLoader().load(root);
        List<String> warnings = new FileScaffolder().check(root, config);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "check");
            output.put("ok", warnings.isEmpty());
            output.put("warnings", warnings);
            System.out.println(JsonOutput.render(output));
            return warnings.isEmpty() ? 0 : 1;
        }
        warnings.forEach(warning -> System.err.println("WARNING: " + warning));
        if (warnings.isEmpty()) {
            System.out.println("Hive project structure looks good.");
            return 0;
        }
        return 1;
    }
}
