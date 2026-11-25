package io.sinapsi.hive.mcp.workflow;

import java.util.List;

public enum WorkflowPhase {
    BACKLOG,
    DISCOVERY,
    READY,
    DEVELOPMENT,
    REVIEW,
    APPROVAL,
    DONE;

    public boolean permitsGeneration() {
        return this == DEVELOPMENT;
    }

    public List<WorkflowPhase> allowedNext() {
        return switch (this) {
            case BACKLOG -> List.of(DISCOVERY);
            case DISCOVERY -> List.of(READY);
            case READY -> List.of(DEVELOPMENT);
            case DEVELOPMENT -> List.of(REVIEW);
            case REVIEW -> List.of(APPROVAL, DEVELOPMENT);
            case APPROVAL -> List.of(DONE, DEVELOPMENT);
            case DONE -> List.of();
        };
    }
}
