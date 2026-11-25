package io.sinapsi.hive.mcp.index;

public record ProjectIndexSummary(
        String database,
        long files,
        long symbols,
        long methods,
        long imports,
        long relations) {}
