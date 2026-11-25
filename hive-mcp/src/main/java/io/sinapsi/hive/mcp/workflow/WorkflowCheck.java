package io.sinapsi.hive.mcp.workflow;

import java.util.List;

public record WorkflowCheck(
        boolean managed,
        boolean passed,
        WorkflowPhase targetPhase,
        WorkflowPhase currentPhase,
        List<String> errors,
        List<String> allowedOperations) {
}
