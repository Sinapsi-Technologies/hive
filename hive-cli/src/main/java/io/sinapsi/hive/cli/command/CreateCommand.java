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
                CreateArchTestCommand.class,
                CreateValueObjectCommand.class,
                CreateIdCommand.class,
                CreateEntityCommand.class,
                CreateAggregateCommand.class,
                CreateEnumCommand.class,
                CreateEventCommand.class,
                CreateExceptionCommand.class,
                CreateDomainServiceCommand.class,
                CreateSnapshotCommand.class,
                CreateRecordCommand.class,
                CreatePlainClassCommand.class,
                CreateApplicationCommandCommand.class,
                CreateInboundCommand.class
        }
)
public final class CreateCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive create --help' to see available create commands.");
    }
}
