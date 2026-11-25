package io.sinapsi.hive.mcp.security;

public record OsvFinding(
        String packageName,
        String version,
        String ecosystem,
        String vulnerabilityId,
        String severity,
        String fixedVersion,
        String source) {}
