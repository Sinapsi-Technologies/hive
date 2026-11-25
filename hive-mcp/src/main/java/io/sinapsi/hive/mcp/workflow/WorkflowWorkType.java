package io.sinapsi.hive.mcp.workflow;

import java.util.List;

public enum WorkflowWorkType {
    GREENFIELD,
    FEATURE,
    BUGFIX,
    DOCUMENTATION;

    public List<String> defaultRequiredDecisions() {
        return this == GREENFIELD
                ? List.of("delivery-channel", "framework", "persistence")
                : List.of();
    }
}
