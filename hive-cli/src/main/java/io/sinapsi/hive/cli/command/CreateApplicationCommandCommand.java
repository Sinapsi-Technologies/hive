package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "command", mixinStandardHelpOptions = true, description = "Create an application command.")
public final class CreateApplicationCommandCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "CommandName or moduleName CommandName.")
    List<String> names;

    @Option(names = "--field", paramLabel = "name:Type", description = "Command field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String commandName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createCommand(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                commandName,
                fields,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create command",
                commandName,
                created,
                "Created command " + commandName + "."
        );
        return 0;
    }
}
