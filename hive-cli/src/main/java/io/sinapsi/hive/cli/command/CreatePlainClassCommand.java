package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import io.sinapsi.hive.cli.service.FileScaffolder.ClassSpec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "class", mixinStandardHelpOptions = true, description = "Create a deterministic Java class.")
public final class CreatePlainClassCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ClassName or moduleName ClassName.")
    List<String> names;

    @Option(names = "--field", paramLabel = "name:Type", description = "Class field.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--getters", description = "Generate getters.")
    boolean getters;

    @Option(names = "--setters", description = "Generate setters.")
    boolean setters;

    @Option(names = "--constructor", description = "Generate a no-args constructor.")
    boolean constructor;

    @Option(names = "--all-args-constructor", description = "Generate an all-args constructor.")
    boolean allArgsConstructor;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String className = CreateDomainCommandSupport.primitiveName(names);
        ClassSpec spec = new ClassSpec(className, fields, getters, setters, constructor, allArgsConstructor);
        List<Path> created = new FileScaffolder().createPlainClass(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                spec,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create class",
                className,
                created,
                "Created class " + className + "."
        );
        return 0;
    }
}
