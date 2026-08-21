package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.service.inbound.InboundAdapterType;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

@Command(name = "mcp", mixinStandardHelpOptions = true, description = "Create an MCP inbound adapter.")
public final class CreateInboundMcpCommand extends CreateInboundSupport implements Callable<Integer> {
    @Parameters(arity = "1..2", paramLabel = "NAME", description = "ToolName or moduleName ToolName.")
    List<String> names;

    @Option(names = "--usecase", required = true, paramLabel = "USECASE", description = "Application use case exposed by this MCP tool.")
    List<String> useCases = new ArrayList<>();

    @Option(names = "--force", description = "Overwrite existing generated files.")
    boolean force;

    @Option(names = "--json", description = "Print the created files as JSON.")
    boolean json;

    @Override
    public Integer call() throws Exception {
        return create(InboundAdapterType.MCP, names, useCaseOperations(useCases), force, json);
    }
}
