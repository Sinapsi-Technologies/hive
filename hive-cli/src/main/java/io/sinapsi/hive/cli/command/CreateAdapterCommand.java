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
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "adapter",
        mixinStandardHelpOptions = true,
        description = "Create an outbound adapter implementing an output port."
)
public final class CreateAdapterCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "AdapterName or moduleName AdapterName.")
    List<String> names;

    @Option(names = "--port", required = true, paramLabel = "PORT", description = "Output port the adapter implements.")
    List<String> ports;

    @Option(names = "--group", paramLabel = "GROUP", description = "Outbound adapter package group.")
    String group;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        return call(Path.of("."));
    }

    Integer call(Path start) throws Exception {
        Path root = new ProjectLocator().locate(start)
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        String moduleName = names.size() == 2 ? names.getFirst() : null;
        String adapterName = names.size() == 2 ? names.get(1) : names.getFirst();
        List<Path> created = new FileScaffolder().createAdapter(root, config, moduleName, adapterName, ports, group, force);
        if (json) {
            java.util.Map<String, Object> output = new java.util.LinkedHashMap<>();
            output.put("command", "create adapter");
            output.put("name", adapterName);
            output.put("port", ports.getFirst());
            output.put("ports", ports);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created adapter " + adapterName + ".");
        }
        return 0;
    }
}
