package io.sinapsi.hive.mcp.monitor;

import java.time.Instant;

public record OperationRecord(
        String id,
        String tool,
        String projectRoot,
        String status,
        Instant startedAt,
        Instant finishedAt,
        long durationMillis,
        String error) {
}
