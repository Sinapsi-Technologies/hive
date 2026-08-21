package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "entity", mixinStandardHelpOptions = true, description = "Create a domain entity.")
public final class CreateEntityCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "EntityName or moduleName EntityName.")
    List<String> names;

    @Option(names = "--id", required = true, paramLabel = "TYPE", description = "Identifier type.")
    String id;

    @Option(names = "--field", paramLabel = "name:Type", description = "Entity field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String entityName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createEntity(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                entityName,
                id,
                fields,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create entity",
                entityName,
                created,
                "Created entity " + entityName + "."
        );
        return 0;
    }
}
