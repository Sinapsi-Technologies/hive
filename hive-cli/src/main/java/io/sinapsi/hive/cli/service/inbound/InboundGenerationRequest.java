package io.sinapsi.hive.cli.service.inbound;

import java.util.List;

public record InboundGenerationRequest(
        InboundAdapterType type,
        String moduleName,
        String adapterName,
        List<InboundOperation> operations,
        boolean force
) {
}
