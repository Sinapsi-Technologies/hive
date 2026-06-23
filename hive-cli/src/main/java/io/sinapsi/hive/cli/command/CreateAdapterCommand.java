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

@Command(
        name = "adapter",
        mixinStandardHelpOptions = true,
        description = "Create an outbound adapter implementing an output port."
)
public final class CreateAdapterCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "AdapterName or moduleName AdapterName.")
    List<String> names;

    @Option(names = "--port", required = true, paramLabel = "PORT", description = "Output port the adapter implements.")
    String port;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        String moduleName = names.size() == 2 ? names.getFirst() : null;
        String adapterName = names.size() == 2 ? names.get(1) : names.getFirst();
        List<Path> created = new FileScaffolder().createAdapter(root, config, moduleName, adapterName, port, force);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create adapter");
            output.put("name", adapterName);
            output.put("port", port);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created adapter " + adapterName + ".");
        }
        return 0;
    }
}
