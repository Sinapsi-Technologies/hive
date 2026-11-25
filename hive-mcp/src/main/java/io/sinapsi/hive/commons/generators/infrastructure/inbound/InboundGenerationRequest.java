package io.sinapsi.hive.commons.generators.infrastructure.inbound;

import java.util.List;

public record InboundGenerationRequest(
        InboundAdapterType type,
        String moduleName,
        String adapterName,
        List<InboundOperation> operations,
        boolean force
) {
}
