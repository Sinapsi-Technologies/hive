package io.sinapsi.hive.cli;

import io.sinapsi.hive.cli.command.C4Command;
import io.sinapsi.hive.cli.command.CheckCommand;
import io.sinapsi.hive.cli.command.CreateCommand;
import io.sinapsi.hive.cli.command.GenerateCommand;
import io.sinapsi.hive.cli.command.InspectCommand;
import io.sinapsi.hive.cli.command.InitCommand;
import picocli.CommandLine.Command;

@Command(
        name = "hive",
        mixinStandardHelpOptions = true,
        description = "Scaffold lightweight hexagonal architecture projects.",
        subcommands = {
                InitCommand.class,
                CreateCommand.class,
                CheckCommand.class,
                GenerateCommand.class,
                InspectCommand.class,
                C4Command.class
        }
)
public final class HiveCli implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive --help' to see available commands.");
    }
}
