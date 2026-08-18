package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "exception", mixinStandardHelpOptions = true, description = "Create a domain exception.")
public final class CreateExceptionCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ExceptionName or moduleName ExceptionName.")
    List<String> names;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--message", paramLabel = "MESSAGE", description = "Default exception message.")
    String message;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String exceptionName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createException(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                exceptionName,
                message,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create exception",
                exceptionName,
                created,
                "Created exception " + exceptionName + "."
        );
        return 0;
    }
}
