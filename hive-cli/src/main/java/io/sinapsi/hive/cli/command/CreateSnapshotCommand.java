package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "snapshot", mixinStandardHelpOptions = true, description = "Create a domain snapshot.")
public final class CreateSnapshotCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "SnapshotName or moduleName SnapshotName.")
    List<String> names;

    @Option(names = "--field", paramLabel = "name:Type", description = "Snapshot field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String snapshotName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createSnapshot(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                snapshotName,
                fields,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create snapshot",
                snapshotName,
                created,
                "Created snapshot " + snapshotName + "."
        );
        return 0;
    }
}
