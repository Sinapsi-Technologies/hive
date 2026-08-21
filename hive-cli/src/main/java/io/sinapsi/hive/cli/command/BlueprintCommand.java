package io.sinapsi.hive.cli.command;

import picocli.CommandLine.Command;

@Command(
        name = "blueprint",
        mixinStandardHelpOptions = true,
        description = "Inspect and validate Hive blueprint contracts.",
        subcommands = {
                BlueprintSchemaCommand.class,
                BlueprintValidateCommand.class
        }
)
public final class BlueprintCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive blueprint --help' to see available blueprint commands.");
    }
}
