package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "record", mixinStandardHelpOptions = true, description = "Create a deterministic Java record.")
public final class CreateRecordCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "RecordName or moduleName RecordName.")
    List<String> names;

    @Option(names = "--field", paramLabel = "name:Type", description = "Record field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String recordName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createRecord(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                recordName,
                fields,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create record",
                recordName,
                created,
                "Created record " + recordName + "."
        );
        return 0;
    }
}
