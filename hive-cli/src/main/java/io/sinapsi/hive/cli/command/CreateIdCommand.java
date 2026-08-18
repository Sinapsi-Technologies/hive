package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "id", mixinStandardHelpOptions = true, description = "Create a domain identifier.")
public final class CreateIdCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "IdentifierName or moduleName IdentifierName.")
    List<String> names;

    @Option(names = "--type", defaultValue = "UUID", description = "Identifier backing type.")
    String type;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String idName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createIdentifier(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                idName,
                type,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create id",
                idName,
                created,
                "Created identifier " + idName + "."
        );
        return 0;
    }
}
