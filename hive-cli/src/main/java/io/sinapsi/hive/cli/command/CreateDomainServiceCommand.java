package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "domainservice", mixinStandardHelpOptions = true, description = "Create a domain service.")
public final class CreateDomainServiceCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ServiceName or moduleName ServiceName.")
    List<String> names;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String serviceName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createDomainService(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                serviceName,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create domainservice",
                serviceName,
                created,
                "Created domain service " + serviceName + "."
        );
        return 0;
    }
}
