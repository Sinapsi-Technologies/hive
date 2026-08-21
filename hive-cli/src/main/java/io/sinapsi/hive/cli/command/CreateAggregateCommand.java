package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "aggregate", mixinStandardHelpOptions = true, description = "Create a domain aggregate.")
public final class CreateAggregateCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "AggregateName or moduleName AggregateName.")
    List<String> names;

    @Option(names = "--id", required = true, paramLabel = "TYPE", description = "Aggregate identifier type.")
    String id;

    @Option(names = "--field", paramLabel = "name:Type", description = "Aggregate field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String aggregateName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createAggregate(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                aggregateName,
                id,
                fields,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create aggregate",
                aggregateName,
                created,
                "Created aggregate " + aggregateName + "."
        );
        return 0;
    }
}
