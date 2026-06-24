package io.sinapsi.hive.cli.command;

import picocli.CommandLine.Command;

/**
 * The {@code hive c4} command group: generate C4 architecture diagrams from a Hive project's
 * hexagonal package structure.
 */
@Command(
        name = "c4",
        mixinStandardHelpOptions = true,
        description = "Generate C4 architecture diagrams from your hexagonal structure.",
        subcommands = {
                C4GenerateCommand.class
        }
)
public final class C4Command implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive c4 --help' to see available c4 commands.");
    }
}
