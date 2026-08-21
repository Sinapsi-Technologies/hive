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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "port", mixinStandardHelpOptions = true, description = "Create an output port.")
public final class CreatePortCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "PortName or moduleName PortName.")
    List<String> names;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--method", paramLabel = "SIGNATURE", description = "Output port method signature.")
    List<String> methods = new ArrayList<>();

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        String moduleName = names.size() == 2 ? names.getFirst() : null;
        String portName = names.size() == 2 ? names.get(1) : names.getFirst();
        List<Path> created = new FileScaffolder().createPort(root, config, moduleName, portName, methods, force);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create port");
            output.put("name", portName);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created output port " + portName + ".");
        }
        return 0;
    }
}
