package io.sinapsi.hive.mcp.dependencies.application;

import java.util.List;

public record SecurityAssessment(
        String provider,
        Status status,
        List<SecurityAdvisory> advisories,
        String summary) {

    public SecurityAssessment {
        advisories = List.copyOf(advisories == null ? List.of() : advisories);
    }

    public enum Status {
        PASS,
        WARN,
        FAIL,
        UNKNOWN
    }
}
