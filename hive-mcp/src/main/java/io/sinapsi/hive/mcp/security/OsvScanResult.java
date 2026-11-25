package io.sinapsi.hive.mcp.security;

import java.util.List;

public record OsvScanResult(Status status, int exitCode, List<OsvFinding> findings, String error) {

    public OsvScanResult {
        findings = List.copyOf(findings == null ? List.of() : findings);
    }

    public enum Status {
        PASS,
        FAIL,
        DISABLED,
        UNAVAILABLE,
        TIMEOUT
    }
}
