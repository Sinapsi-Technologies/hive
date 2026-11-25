package io.sinapsi.hive.mcp.workflow;

public final class WorkflowGateException extends IllegalStateException {
    private final WorkflowCheck check;

    public WorkflowGateException(WorkflowCheck check) {
        super(format(check));
        this.check = check;
    }

    public WorkflowCheck check() {
        return check;
    }

    private static String format(WorkflowCheck check) {
        return "GATE_NOT_READY for " + check.targetPhase() + ": "
                + String.join("; ", check.errors())
                + ". Allowed operations: " + String.join(", ", check.allowedOperations());
    }
}
