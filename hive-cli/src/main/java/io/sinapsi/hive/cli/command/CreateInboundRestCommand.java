package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.inbound.InboundAdapterType;
import io.sinapsi.hive.cli.service.inbound.InboundOperation;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "rest", mixinStandardHelpOptions = true, description = "Create a REST inbound adapter.")
public final class CreateInboundRestCommand extends CreateInboundSupport implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ControllerName or moduleName ControllerName.")
    List<String> names;

    @Option(names = "--usecase", paramLabel = "USECASE", description = "Application use case exposed by this controller.")
    List<String> useCases = new ArrayList<>();

    @Option(
            names = "--operation",
            paramLabel = "\"METHOD /path -> USECASE\"",
            description = "Explicit REST operation bound to a use case."
    )
    List<String> operations = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        List<InboundOperation> resolved = new ArrayList<>(useCaseOperations(useCases));
        resolved.addAll(operations.stream().map(InboundOperation::parseRest).toList());
        return create(InboundAdapterType.REST, names, resolved, force, json);
    }
}
