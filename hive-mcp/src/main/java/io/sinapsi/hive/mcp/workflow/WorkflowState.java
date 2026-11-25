package io.sinapsi.hive.mcp.workflow;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record WorkflowState(
        int schemaVersion,
        String workItemId,
        WorkflowWorkType workType,
        WorkflowPhase phase,
        String backlogProvider,
        String acceptanceCriteriaSource,
        List<String> requiredDecisions,
        Map<String, WorkflowDecision> decisions) {

    public WorkflowState {
        requiredDecisions = requiredDecisions == null ? List.of() : List.copyOf(requiredDecisions);
        decisions = decisions == null ? Map.of() : Map.copyOf(decisions);
    }

    public static WorkflowState initial() {
        return new WorkflowState(
                1,
                null,
                WorkflowWorkType.GREENFIELD,
                WorkflowPhase.BACKLOG,
                null,
                null,
                WorkflowWorkType.GREENFIELD.defaultRequiredDecisions(),
                openDecisions(WorkflowWorkType.GREENFIELD.defaultRequiredDecisions()));
    }

    public WorkflowState withDecision(String topic, WorkflowDecision decision) {
        Map<String, WorkflowDecision> updated = new LinkedHashMap<>(decisions);
        updated.put(topic, decision);
        return new WorkflowState(schemaVersion, workItemId, workType, phase, backlogProvider,
                acceptanceCriteriaSource, requiredDecisions, updated);
    }

    public WorkflowState withPhase(WorkflowPhase nextPhase) {
        return new WorkflowState(schemaVersion, workItemId, workType, nextPhase, backlogProvider,
                acceptanceCriteriaSource, requiredDecisions, decisions);
    }

    public static Map<String, WorkflowDecision> openDecisions(List<String> topics) {
        Map<String, WorkflowDecision> decisions = new LinkedHashMap<>();
        topics.forEach(topic -> decisions.put(topic, WorkflowDecision.open()));
        return decisions;
    }
}
