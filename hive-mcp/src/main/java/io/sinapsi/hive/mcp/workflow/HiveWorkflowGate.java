package io.sinapsi.hive.mcp.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Component
public final class HiveWorkflowGate {
    private static final Path WORKFLOW_FILE = Path.of(".hive", "workflow.yml");
    private static final Set<String> BACKLOG_PROVIDERS = Set.of("LOCAL", "GITHUB", "GITLAB", "JIRA", "LATER");
    private static final List<String> ALLOWED_DISCOVERY_OPERATIONS = List.of(
            "hive_prepare_workflow",
            "hive_record_workflow_decision",
            "hive_save_domain_model",
            "hive_advance_workflow");

    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory()).findAndRegisterModules();

    public Optional<Path> initialize(Path projectRoot) throws IOException {
        Path file = file(projectRoot);
        if (Files.exists(file)) {
            return Optional.empty();
        }
        write(projectRoot, WorkflowState.initial());
        return Optional.of(file);
    }

    public Optional<WorkflowState> load(Path projectRoot) throws IOException {
        Path file = file(projectRoot);
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        WorkflowState state = yaml.readValue(file.toFile(), WorkflowState.class);
        if (state.schemaVersion() != 1) {
            throw new IOException("Unsupported Hive workflow schema version: " + state.schemaVersion());
        }
        return Optional.of(state);
    }

    public WorkflowState prepare(
            Path projectRoot,
            String workItemId,
            String workType,
            String backlogProvider,
            String acceptanceCriteriaSource,
            List<String> additionalRequiredDecisions) throws IOException {
        requireText(workItemId, "workItemId");
        WorkflowWorkType type = parse(WorkflowWorkType.class, workType, "workType");
        String provider = normalizeBacklogProvider(backlogProvider);
        requireText(acceptanceCriteriaSource, "acceptanceCriteriaSource");

        WorkflowState current = load(projectRoot).orElse(WorkflowState.initial());
        if (current.workItemId() != null && !current.workItemId().equals(workItemId.trim())) {
            throw new IllegalStateException("Workflow already belongs to work item " + current.workItemId());
        }
        if (current.phase().ordinal() > WorkflowPhase.DISCOVERY.ordinal()) {
            throw new IllegalStateException(
                    "Cannot prepare a workflow that has already reached " + current.phase());
        }

        LinkedHashSet<String> required = new LinkedHashSet<>(type.defaultRequiredDecisions());
        if (additionalRequiredDecisions != null) {
            additionalRequiredDecisions.stream().map(HiveWorkflowGate::normalizeTopic).forEach(required::add);
        }
        Map<String, WorkflowDecision> decisions = new LinkedHashMap<>();
        required.forEach(topic -> decisions.put(topic,
                current.decisions().getOrDefault(topic, WorkflowDecision.open())));

        WorkflowState prepared = new WorkflowState(
                1,
                workItemId.trim(),
                type,
                WorkflowPhase.DISCOVERY,
                provider,
                acceptanceCriteriaSource.trim(),
                List.copyOf(required),
                decisions);
        write(projectRoot, prepared);
        return prepared;
    }

    public WorkflowState recordDecision(
            Path projectRoot,
            String topic,
            String status,
            String value,
            String source) throws IOException {
        WorkflowState state = requiredState(projectRoot);
        String normalizedTopic = normalizeTopic(topic);
        WorkflowDecisionStatus decisionStatus = parse(WorkflowDecisionStatus.class, status, "status");
        if (decisionStatus.resolvesGate()) {
            requireText(source, "source");
            if (decisionStatus != WorkflowDecisionStatus.NOT_APPLICABLE) {
                requireText(value, "value");
            }
        }
        WorkflowState updated = state.withDecision(normalizedTopic,
                new WorkflowDecision(decisionStatus, trimToNull(value), trimToNull(source)));
        write(projectRoot, updated);
        return updated;
    }

    public WorkflowState advance(Path projectRoot, String targetPhase) throws IOException {
        WorkflowState state = requiredState(projectRoot);
        WorkflowPhase target = parse(WorkflowPhase.class, targetPhase, "targetPhase");
        if (target.ordinal() > WorkflowPhase.DEVELOPMENT.ordinal()) {
            throw new UnsupportedOperationException(
                    "Workflow gates after DEVELOPMENT are not implemented in this slice");
        }
        if (target == state.phase()) {
            return state;
        }
        if (!state.phase().allowedNext().contains(target)) {
            throw new IllegalStateException(
                    "Invalid workflow transition: " + state.phase() + " -> " + target);
        }
        WorkflowCheck check = checkPrerequisites(state, target);
        if (!check.passed()) {
            throw new WorkflowGateException(check);
        }
        WorkflowState updated = state.withPhase(target);
        write(projectRoot, updated);
        return updated;
    }

    public WorkflowCheck check(Path projectRoot, String targetPhase) throws IOException {
        WorkflowPhase target = parse(WorkflowPhase.class, targetPhase, "targetPhase");
        Optional<WorkflowState> state = load(projectRoot);
        if (state.isEmpty()) {
            return new WorkflowCheck(false, true, target, null, List.of(), List.of(
                    "legacy project: workflow gate is not enabled",
                    "run hive_prepare_workflow to enable it"));
        }
        return checkPrerequisites(state.get(), target);
    }

    public void requireDevelopment(Path projectRoot) throws IOException {
        Optional<WorkflowState> state = load(projectRoot);
        if (state.isEmpty()) {
            return;
        }
        WorkflowCheck prerequisites = checkPrerequisites(state.get(), WorkflowPhase.READY);
        List<String> errors = new ArrayList<>(prerequisites.errors());
        if (!state.get().phase().permitsGeneration()) {
            errors.add("current phase " + state.get().phase() + " does not permit code generation");
        }
        if (!errors.isEmpty()) {
            throw new WorkflowGateException(new WorkflowCheck(
                    true,
                    false,
                    WorkflowPhase.DEVELOPMENT,
                    state.get().phase(),
                    List.copyOf(errors),
                    ALLOWED_DISCOVERY_OPERATIONS));
        }
    }

    public Map<String, Object> describe(Path projectRoot, WorkflowState state) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("managed", true);
        result.put("file", projectRoot.relativize(file(projectRoot)).toString());
        result.put("schemaVersion", state.schemaVersion());
        result.put("workItemId", state.workItemId());
        result.put("workType", state.workType());
        result.put("phase", state.phase());
        result.put("backlogProvider", state.backlogProvider());
        result.put("acceptanceCriteriaSource", state.acceptanceCriteriaSource());
        result.put("requiredDecisions", state.requiredDecisions());
        result.put("decisions", state.decisions());
        return result;
    }

    public Map<String, Object> describe(WorkflowCheck check) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("managed", check.managed());
        result.put("passed", check.passed());
        result.put("targetPhase", check.targetPhase());
        result.put("currentPhase", check.currentPhase());
        result.put("errors", check.errors());
        result.put("allowedOperations", check.allowedOperations());
        return result;
    }

    private WorkflowCheck checkPrerequisites(WorkflowState state, WorkflowPhase target) {
        List<String> errors = new ArrayList<>();
        if (target.ordinal() >= WorkflowPhase.DISCOVERY.ordinal()) {
            if (isBlank(state.workItemId())) {
                errors.add("work item is not configured");
            }
            if (isBlank(state.backlogProvider())) {
                errors.add("backlog provider is not configured");
            }
        }
        if (target.ordinal() >= WorkflowPhase.READY.ordinal()) {
            if (isBlank(state.acceptanceCriteriaSource())) {
                errors.add("acceptance criteria source is not configured");
            }
            for (String topic : state.requiredDecisions()) {
                WorkflowDecision decision = state.decisions().get(topic);
                if (decision == null || decision.status() == null || !decision.status().resolvesGate()) {
                    errors.add("decision " + topic + " is not resolved");
                } else if (isBlank(decision.source())) {
                    errors.add("decision " + topic + " has no source");
                }
            }
        }
        return new WorkflowCheck(
                true,
                errors.isEmpty(),
                target,
                state.phase(),
                List.copyOf(errors),
                errors.isEmpty() ? List.of("hive_advance_workflow") : ALLOWED_DISCOVERY_OPERATIONS);
    }

    private WorkflowState requiredState(Path projectRoot) throws IOException {
        return load(projectRoot).orElseThrow(() -> new IllegalStateException(
                "Workflow is not enabled. Call hive_prepare_workflow first."));
    }

    private void write(Path projectRoot, WorkflowState state) throws IOException {
        Path target = file(projectRoot);
        Files.createDirectories(target.getParent());
        Path temporary = Files.createTempFile(target.getParent(), "workflow-", ".tmp");
        try {
            yaml.writerWithDefaultPrettyPrinter().writeValue(temporary.toFile(), state);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static Path file(Path projectRoot) {
        return projectRoot.resolve(WORKFLOW_FILE);
    }

    private static String normalizeBacklogProvider(String value) {
        requireText(value, "backlogProvider");
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!BACKLOG_PROVIDERS.contains(normalized)) {
            throw new IllegalArgumentException("Unsupported backlog provider: " + value);
        }
        return normalized;
    }

    private static String normalizeTopic(String value) {
        requireText(value, "decision topic");
        return value.trim().toLowerCase(Locale.ROOT).replace('_', '-').replace(' ', '-');
    }

    private static <E extends Enum<E>> E parse(Class<E> type, String value, String field) {
        requireText(value, field);
        try {
            return Enum.valueOf(type, value.trim().toUpperCase(Locale.ROOT).replace('-', '_'));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported " + field + ": " + value, exception);
        }
    }

    private static void requireText(String value, String field) {
        if (isBlank(value)) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }

    private static String trimToNull(String value) {
        return isBlank(value) ? null : value.trim();
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
