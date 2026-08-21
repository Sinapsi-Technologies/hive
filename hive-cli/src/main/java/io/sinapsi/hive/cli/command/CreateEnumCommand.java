package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "enum", mixinStandardHelpOptions = true, description = "Create a domain enum.")
public final class CreateEnumCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "EnumName or moduleName EnumName.")
    List<String> names;

    @Option(names = "--value", required = true, paramLabel = "VALUE", description = "Enum constant.")
    List<String> values = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String enumName = CreateDomainCommandSupport.primitiveName(names);
        List<Path> created = new FileScaffolder().createEnum(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                enumName,
                values,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create enum",
                enumName,
                created,
                "Created enum " + enumName + "."
        );
        return 0;
    }
}
