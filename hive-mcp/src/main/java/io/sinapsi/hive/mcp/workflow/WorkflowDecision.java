package io.sinapsi.hive.mcp.workflow;

public record WorkflowDecision(
        WorkflowDecisionStatus status,
        String value,
        String source) {

    public static WorkflowDecision open() {
        return new WorkflowDecision(WorkflowDecisionStatus.OPEN, null, null);
    }
}
