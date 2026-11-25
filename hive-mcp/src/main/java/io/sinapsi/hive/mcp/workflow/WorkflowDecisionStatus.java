package io.sinapsi.hive.mcp.workflow;

public enum WorkflowDecisionStatus {
    OPEN,
    PROPOSED,
    CONFIRMED,
    INHERITED,
    NOT_APPLICABLE;

    public boolean resolvesGate() {
        return this == CONFIRMED || this == INHERITED || this == NOT_APPLICABLE;
    }
}
