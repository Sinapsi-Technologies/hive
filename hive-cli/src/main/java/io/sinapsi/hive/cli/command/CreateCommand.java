package io.sinapsi.hive.cli.command;

import picocli.CommandLine.Command;

@Command(
        name = "create",
        mixinStandardHelpOptions = true,
        description = "Create hexagonal building blocks.",
        subcommands = {
                CreateUseCaseCommand.class,
                CreatePortCommand.class,
                CreateAdapterCommand.class,
                CreateModuleCommand.class,
                CreateArchTestCommand.class
        }
)
public final class CreateCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive create --help' to see available create commands.");
    }
}
