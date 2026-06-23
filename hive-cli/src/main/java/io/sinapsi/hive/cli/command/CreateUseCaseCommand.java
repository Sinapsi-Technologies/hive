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

@Command(name = "usecase", mixinStandardHelpOptions = true, description = "Create a use case.")
public final class CreateUseCaseCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "UseCaseName or moduleName UseCaseName.")
    List<String> names;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--factory", description = "Create an AbstractCommandFactory for the generated command.")
    boolean factory;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        String moduleName = names.size() == 2 ? names.getFirst() : null;
        String useCaseName = names.size() == 2 ? names.get(1) : names.getFirst();
        List<Path> created = new FileScaffolder().createUseCase(root, config, moduleName, useCaseName, force, factory);
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create usecase");
            output.put("name", useCaseName);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created use case " + useCaseName + ".");
        }
        return 0;
    }
}
