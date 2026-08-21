package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.inbound.InboundAdapterType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "listener", mixinStandardHelpOptions = true, description = "Create a message listener inbound adapter.")
public final class CreateInboundListenerCommand extends CreateInboundSupport implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ListenerName or moduleName ListenerName.")
    List<String> names;

    @Option(names = "--usecase", required = true, paramLabel = "USECASE", description = "Application use case triggered by this listener.")
    List<String> useCases = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        return create(InboundAdapterType.LISTENER, names, useCaseOperations(useCases), force, json);
    }
}
