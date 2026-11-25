package io.sinapsi.hive.mcp.index;

public record SourceContext(String file, int startLine, int endLine, String source) {}
