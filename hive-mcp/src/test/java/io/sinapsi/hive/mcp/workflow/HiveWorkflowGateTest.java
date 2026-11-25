package io.sinapsi.hive.mcp.workflow;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiveWorkflowGateTest {

    private final HiveWorkflowGate gate = new HiveWorkflowGate();

    @Test
    void initializesGreenfieldWorkflowWithOpenArchitecturalDecisions(@TempDir Path project) throws Exception {
        Path workflow = gate.initialize(project).orElseThrow();

        WorkflowState state = gate.load(project).orElseThrow();
        assertEquals(project.resolve(".hive/workflow.yml"), workflow);
        assertEquals(WorkflowPhase.BACKLOG, state.phase());
        assertEquals(List.of("delivery-channel", "framework", "persistence"), state.requiredDecisions());
        assertTrue(state.decisions().values().stream()
                .allMatch(decision -> decision.status() == WorkflowDecisionStatus.OPEN));
    }

    @Test
    void rejectsReadyTransitionUntilEveryRequiredDecisionHasEvidence(@TempDir Path project) throws Exception {
        gate.initialize(project);
        gate.prepare(project, "TODO-1", "greenfield", "local", "requirements.md", List.of());
        gate.recordDecision(project, "delivery-channel", "confirmed", "REST", "user message");
        gate.recordDecision(project, "framework", "confirmed", "Spring Boot", "user message");

        WorkflowGateException exception = assertThrows(
                WorkflowGateException.class,
                () -> gate.advance(project, "ready"));

        assertTrue(exception.check().errors().contains("decision persistence is not resolved"));
        assertEquals(WorkflowPhase.DISCOVERY, gate.load(project).orElseThrow().phase());
    }

    @Test
    void permitsGenerationOnlyAfterValidatedReadyTransition(@TempDir Path project) throws Exception {
        gate.initialize(project);
        gate.prepare(project, "TODO-1", "greenfield", "later", "requirements.md", List.of());
        gate.recordDecision(project, "delivery-channel", "confirmed", "desktop", "user message");
        gate.recordDecision(project, "framework", "not-applicable", null, "framework-free decision");
        gate.recordDecision(project, "persistence", "inherited", "SQLite", "ADR-002");

        gate.advance(project, "ready");

        assertThrows(WorkflowGateException.class, () -> gate.requireDevelopment(project));
        gate.advance(project, "development");
        gate.requireDevelopment(project);
        assertEquals(WorkflowPhase.DEVELOPMENT, gate.load(project).orElseThrow().phase());
    }

    @Test
    void leavesLegacyProjectsExplicitlyUnmanaged(@TempDir Path project) throws Exception {
        WorkflowCheck check = gate.check(project, "development");

        assertFalse(check.managed());
        assertTrue(check.passed());
        assertTrue(check.allowedOperations().getFirst().contains("legacy"));
        assertFalse(Files.exists(project.resolve(".hive/workflow.yml")));
    }
}
