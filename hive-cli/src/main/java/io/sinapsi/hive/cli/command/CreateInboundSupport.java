package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;
import io.sinapsi.hive.cli.service.inbound.InboundAdapterGenerator;
import io.sinapsi.hive.cli.service.inbound.InboundAdapterType;
import io.sinapsi.hive.cli.service.inbound.InboundGenerationRequest;
import io.sinapsi.hive.cli.service.inbound.InboundOperation;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

abstract class CreateInboundSupport {
    final Integer create(
            InboundAdapterType type,
            List<String> names,
            List<InboundOperation> operations,
            boolean force,
            boolean json
    )
            throws Exception {
        if (operations == null || operations.isEmpty()) {
            throw new IllegalArgumentException("At least one --usecase or --operation is required");
        }
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        HiveConfig config = new HiveConfigLoader().load(root);
        String moduleName = names.size() == 2 ? names.getFirst() : null;
        String adapterName = names.size() == 2 ? names.get(1) : names.getFirst();
        List<Path> created = new InboundAdapterGenerator().generate(
                root,
                config,
                new InboundGenerationRequest(type, moduleName, adapterName, operations, force)
        );
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "create inbound " + type.directory());
            output.put("name", adapterName);
            output.put("type", type.directory());
            output.put("usecases", operations.stream().map(InboundOperation::useCaseName).toList());
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println("Created inbound " + type.directory() + " adapter " + adapterName + ".");
        }
        return 0;
    }

    final List<InboundOperation> useCaseOperations(List<String> useCases) {
        return useCases.stream().map(InboundOperation::useCaseOnly).toList();
    }
}
