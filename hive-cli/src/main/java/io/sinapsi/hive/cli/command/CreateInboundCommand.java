package io.sinapsi.hive.cli.command;

import picocli.CommandLine.Command;

@Command(
        name = "inbound",
        mixinStandardHelpOptions = true,
        description = "Create inbound adapters for transports that call application use cases.",
        subcommands = {
                CreateInboundRestCommand.class,
                CreateInboundMcpCommand.class,
                CreateInboundListenerCommand.class,
                CreateInboundSchedulerCommand.class
        }
)
public final class CreateInboundCommand implements Runnable {
    @Override
    public void run() {
        System.out.println("Use 'hive create inbound --help' to see available inbound adapter commands.");
    }
}
