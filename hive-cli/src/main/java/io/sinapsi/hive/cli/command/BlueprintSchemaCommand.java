package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.blueprint.BlueprintKind;
import io.sinapsi.hive.cli.service.blueprint.BlueprintSchemaProvider;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "schema", mixinStandardHelpOptions = true, description = "Print the installed Hive blueprint schema.")
public final class BlueprintSchemaCommand implements Callable<Integer> {
    @Parameters(index = "0", arity = "0..1", paramLabel = "KIND", description = "Optional artifact kind filter.")
    String kindParameter;

    @Option(names = "--kind", paramLabel = "KIND", description = "Optional artifact kind filter.")
    String kindOption;

    @Option(names = "--json", description = "Print the schema as JSON.")
    boolean json;

    @Override
    public Integer call() {
        String kind = resolveKindFilter();
        BlueprintSchemaProvider provider = new BlueprintSchemaProvider();
        Map<String, Object> schema = provider.schema(kind);
        if (json) {
            System.out.println(JsonOutput.render(schema));
        } else {
            printHuman(provider, kind);
        }
        return 0;
    }

    private String resolveKindFilter() {
        if (kindParameter != null && kindOption != null && !kindParameter.equals(kindOption)) {
            throw new IllegalArgumentException("Conflicting blueprint kind filters: " + kindParameter + " and " + kindOption);
        }
        return kindOption != null ? kindOption : kindParameter;
    }

    private void printHuman(BlueprintSchemaProvider provider, String kindFilter) {
        Map<String, Object> schema = provider.schema(kindFilter);
        System.out.println("HIVE Blueprint Schema");
        System.out.println();
        System.out.println("HIVE version: " + schema.get("hiveVersion"));
        System.out.println("Schema version: " + schema.get("schemaVersion"));
        System.out.println("Blueprint version: " + schema.get("blueprintVersion"));
        System.out.println();
        System.out.println("Kinds:");
        for (BlueprintKind kind : provider.kinds()) {
            if (kindFilter != null && !kind.allKindValues().contains(kindFilter)) {
                continue;
            }
            System.out.println();
            System.out.println(kind.name());
            if (!kind.aliases().isEmpty()) {
                System.out.println("  aliases: " + String.join(", ", kind.aliases()));
            }
            System.out.println("  required:");
            for (String property : kind.required()) {
                System.out.println("    " + property);
            }
            System.out.println("  optional:");
            for (String property : kind.optional()) {
                System.out.println("    " + property);
            }
        }
    }
}
