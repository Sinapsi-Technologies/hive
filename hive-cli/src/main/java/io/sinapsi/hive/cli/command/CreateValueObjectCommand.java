package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.FileScaffolder;
import io.sinapsi.hive.cli.service.FileScaffolder.ValueObjectSpec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(
        name = "vo",
        mixinStandardHelpOptions = true,
        description = "Create a domain value object."
)
public final class CreateValueObjectCommand implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ValueObjectName or moduleName ValueObjectName.")
    List<String> names;

    @Option(names = "--type", defaultValue = "String", description = "Scalar backing type.")
    String type;

    @Option(names = "--not-null", description = "Reject null values.")
    boolean notNull;

    @Option(names = "--not-blank", description = "Reject null or blank String values.")
    boolean notBlank;

    @Option(names = "--min", paramLabel = "VALUE", description = "Minimum numeric value.")
    String min;

    @Option(names = "--max", paramLabel = "VALUE", description = "Maximum numeric value.")
    String max;

    @Option(names = "--min-length", paramLabel = "LENGTH", description = "Minimum String length.")
    Integer minLength;

    @Option(names = "--max-length", paramLabel = "LENGTH", description = "Maximum String length.")
    Integer maxLength;

    @Option(names = "--pattern", paramLabel = "REGEX", description = "Required String pattern.")
    String pattern;

    @Option(names = "--field", paramLabel = "name:Type", description = "Multi-field value object component.")
    List<String> fields = new ArrayList<>();

    @Option(names = "--factory", description = "Create a static of(...) factory method.")
    boolean factory;

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        CreateDomainCommandSupport.ProjectContext project = CreateDomainCommandSupport.locateProject();
        String valueObjectName = CreateDomainCommandSupport.primitiveName(names);
        ValueObjectSpec spec = new ValueObjectSpec(
                valueObjectName,
                type,
                notNull,
                notBlank,
                min,
                max,
                minLength,
                maxLength,
                pattern,
                fields,
                factory
        );
        List<Path> created = new FileScaffolder().createValueObject(
                project.root(),
                project.config(),
                CreateDomainCommandSupport.moduleName(names),
                spec,
                force
        );
        CreateDomainCommandSupport.printResult(
                json,
                project.root(),
                "create vo",
                valueObjectName,
                created,
                "Created value object " + valueObjectName + "."
        );
        return 0;
    }
}
