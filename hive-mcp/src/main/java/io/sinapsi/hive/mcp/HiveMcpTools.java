package io.sinapsi.hive.mcp;

import io.sinapsi.hive.commons.HiveOperations;
import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.project.HiveConfigLoader;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundAdapterGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundAdapterType;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundGenerationRequest;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundOperation;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.sinapsi.hive.mcp.project.HiveProjectContext;
import io.sinapsi.hive.mcp.workflow.HiveWorkflowGate;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

@Component
public final class HiveMcpTools {
    private final HiveOperations hive;
    private final HiveProjectContext projectContext;
    private final HiveWorkflowGate workflow;
    private final HiveConfigLoader configs = new HiveConfigLoader();
    private final InboundAdapterGenerator inbound = new InboundAdapterGenerator();

    @Autowired
    public HiveMcpTools(HiveOperations hive, HiveProjectContext projectContext, HiveWorkflowGate workflow) {
        this.hive = hive;
        this.projectContext = projectContext;
        this.workflow = workflow;
    }

    public HiveMcpTools(HiveOperations hive, String projectRoot) {
        this(hive, new HiveProjectContext(projectRoot, ""), new HiveWorkflowGate());
    }

    @McpTool(name = "hive_init", description = "Initialize the selected Hive project root. Call hive_select_project first; this tool never assumes the MCP server working directory.")
    public List<String> init(boolean force, boolean readme) throws IOException {
        List<Path> generated = new ArrayList<>(hive.init(projectContext.current(), force, readme));
        workflow.initialize(projectContext.current()).ifPresent(generated::add);
        return paths(generated);
    }

    @McpTool(name = "hive_init_project", description = "Select an existing project directory and initialize it in one atomic workflow. Use this as the first call for a new project; it avoids relying on the MCP server working directory.")
    public List<String> initProject(String projectRoot, boolean force, boolean readme) throws IOException {
        projectContext.select(projectRoot);
        return init(force, readme);
    }

    @McpTool(name = "hive_create_value_object_in_module", description = "Create a domain value object record inside modules/<module>. Use this variant when the value object belongs to a bounded module; the module name is explicit and the tool returns the exact generated path.")
    public List<String> createValueObjectInModule(String name, String type, String module, boolean notNull,
                                          boolean notBlank, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.createValueObject(projectContext.current(), module, name, type, notNull, notBlank, force));
    }

    /**
     * Java compatibility overload for callers compiled against the previous
     * six-argument API. It is intentionally not annotated as an MCP tool: the
     * public MCP schema uses the stable five-argument tool plus the explicitly
     * named module variant above.
     */
    public List<String> createValueObject(String name, String type, String module, boolean notNull,
                                          boolean notBlank, boolean force) throws IOException {
        return createValueObjectInModule(name, type, module, notNull, notBlank, force);
    }

    @McpTool(name = "hive_create_value_object", description = "Create a project-level domain value object record. Use it for immutable validated values such as Email or Money. This five-argument form is kept stable for existing MCP clients; use hive_create_value_object_in_module for module-scoped generation.")
    public List<String> createValueObject(String name, String type, boolean notNull,
                                          boolean notBlank, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.createValueObject(projectContext.current(), null, name, type, notNull, notBlank, force));
    }

    @McpTool(name = "hive_create", description = "Generic Hive generator for advanced cases. Use kind to create domain, application, infrastructure, module, or test artifacts when a specialized hive_create_* tool is not sufficient; fields use name:Type syntax.")
    public List<String> create(String kind, String name, String module, String type, String id,
                               List<String> fields, List<String> values, List<String> ports,
                               String group, String message, boolean force, boolean notNull, boolean notBlank,
                               String min, String max, Integer minLength, Integer maxLength, String pattern,
                               boolean factory, boolean getters, boolean setters, boolean constructor,
                               boolean allArgsConstructor) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), kind, module, name, type, id, fields, values, message, ports, group,
                force, notNull, notBlank, min, max, minLength, maxLength, pattern, factory, getters, setters,
                constructor, allArgsConstructor));
    }

    @McpTool(name = "hive_create_id", description = "Create a typed domain identifier implementing AggregateId. Use it before generating an entity or aggregate that references the identifier.")
    public List<String> createId(String name, String type, String module, boolean force) throws IOException {
        return createSimple("id", name, module, type, "Long", force);
    }

    @McpTool(name = "hive_create_entity", description = "Create a domain entity with an identifier and fields. Use it for identity-based domain objects; fields must use name:Type syntax.")
    public List<String> createEntity(String name, String id, List<String> fields, String module, boolean force) throws IOException {
        return createWithFields("entity", name, module, "String", id, fields, force);
    }

    @McpTool(name = "hive_create_aggregate", description = "Create an AggregateRoot with a private constructor and create/rehydrate factories. Use it as the consistency boundary for related domain state. implementationGuidance is optional natural-language context for the next LLM coding pass: it is emitted only as // LLM comments inside the generated factory methods, is never executed by Hive, and should contain invariants, domain rules, events, or constraints the agent already knows. Do not use it to request a signature change.")
    public List<String> createAggregate(String name, String id, List<String> fields, String module,
                                        String implementationGuidance, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.createAggregate(projectContext.current(), module, name, id,
                fields == null ? List.of() : fields, force, implementationGuidance));
    }

    public List<String> createAggregate(String name, String id, List<String> fields, String module,
                                        boolean force) throws IOException {
        return createAggregate(name, id, fields, module, null, force);
    }

    @McpTool(name = "hive_create_enum", description = "Create a domain enum from the supplied values. Use it for finite domain states such as OrderStatus.")
    public List<String> createEnum(String name, List<String> values, String module, boolean force) throws IOException {
        return createWithValues("enum", name, module, values, force);
    }

    @McpTool(name = "hive_create_event", description = "Create an immutable DomainEvent record. Use it to represent a fact that occurred in the domain; fields use name:Type syntax.")
    public List<String> createEvent(String name, List<String> fields, String module, boolean force) throws IOException {
        return createWithFields("event", name, module, "String", "Long", fields, force);
    }

    @McpTool(name = "hive_create_exception", description = "Create a domain exception type with an optional default message. Use it only for exceptional domain conditions that are not represented as a Result failure.")
    public List<String> createException(String name, String message, String module, boolean force) throws IOException {
        return createWithMessage("exception", name, module, message, force);
    }

    @McpTool(name = "hive_create_domain_service", description = "Create a domain service shell. Use it for domain logic that does not naturally belong to one entity or value object; the generated body is a TODO.")
    public List<String> createDomainService(String name, String module, boolean force) throws IOException {
        return createSimple("domainService", name, module, "String", "Long", force);
    }

    @McpTool(name = "hive_create_snapshot", description = "Create an immutable domain snapshot with an of(...) factory. Use it to represent a persisted or reconstructed state at a point in time.")
    public List<String> createSnapshot(String name, List<String> fields, String module, boolean force) throws IOException {
        return createWithFields("snapshot", name, module, "String", "Long", fields, force);
    }

    @McpTool(name = "hive_create_record", description = "Create a plain Java record. Names ending in View are application output models generated in application.ports.in.views with a static of factory; other records go in commons. Not for domain value objects.")
    public List<String> createRecord(String name, List<String> fields, String module, boolean force) throws IOException {
        return createWithFields("record", name, module, "String", "Long", fields, force);
    }

    @McpTool(name = "hive_create_class", description = "Create a plain Java class in the module commons package. Use the getter, setter, and constructor flags to choose the generated boilerplate.")
    public List<String> createClass(String name, List<String> fields, String module, boolean getters,
                                    boolean setters, boolean constructor, boolean allArgsConstructor,
                                    boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), "class", module, name, "String", "Long", fields, List.of(),
                null, List.of(), null, force, false, false, null, null, null, null, null, false,
                getters, setters, constructor, allArgsConstructor));
    }

    @McpTool(name = "hive_create_command", description = "Create an application command nested in its UseCase. Use it when the command contract is needed without generating the service; fields use name:Type syntax.")
    public List<String> createCommand(String name, List<String> fields, String module, boolean force) throws IOException {
        return createWithFields("command", name, module, "String", "Long", fields, force);
    }

    @McpTool(name = "hive_create_usecase", description = "Create an input UseCase, nested Command, and application service. Set factory=true when the agent should also generate a CommandFactory that validates construction through Hive's validator module.")
    public List<String> createUseCase(String name, List<String> fields, String module, boolean factory, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), "usecase", module, name, "String", "Long", fields, List.of(),
                null, List.of(), null, force, false, false, null, null, null, null, null, factory,
                false, false, false, false));
    }

    @McpTool(name = "hive_create_port", description = "Create an application output port extending OutputPort. Use it to define the interface that infrastructure adapters must implement; methods use Java signatures. The name may be supplied with or without the Port suffix; Hive normalizes it and never creates PortPort.")
    public List<String> createPort(String name, List<String> methods, String module, boolean force) throws IOException {
        return createWithFields("port", name, module, "String", "Long", methods, force);
    }

    @McpTool(name = "hive_create_adapter", description = "Create an outbound infrastructure adapter implementing one or more output ports. Use group to place it under a technology or persistence subpackage. Port names may be supplied with or without the Port suffix; create the port before the adapter so Hive can generate implementation stubs for its methods.")
    public List<String> createAdapter(String name, List<String> ports, String group, String module, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), "adapter", module, name, "String", "Long", List.of(), List.of(),
                null, ports, group, force, false, false, null, null, null, null, null, false,
                false, false, false, false));
    }

    @McpTool(name = "hive_create_module", description = "Create the standard Hive module package structure. Use it before generating artifacts when the project is organized into bounded modules.")
    public List<String> createModule(String name) throws IOException {
        return createSimple("module", name, null, "String", "Long", false);
    }

    @McpTool(name = "hive_create_archtest", description = "Create an ArchUnit architecture test scaffold and add the required test dependencies. Use it to enforce Hive hexagonal boundaries in the generated project.")
    public List<String> createArchTest(boolean force) throws IOException {
        return createSimple("archtest", "ArchitectureTest", null, "String", "Long", force);
    }

    private List<String> createSimple(String kind, String name, String module, String type, String id, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), kind, module, name, type, id, List.of(), List.of(), null, List.of(), null,
                force, false, false, null, null, null, null, null, false, false, false, false, false));
    }

    private List<String> createWithFields(String kind, String name, String module, String type, String id,
                                          List<String> fields, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), kind, module, name, type, id, fields == null ? List.of() : fields,
                List.of(), null, List.of(), null, force, false, false, null, null, null, null, null, false,
                false, false, false, false));
    }

    private List<String> createWithValues(String kind, String name, String module, List<String> values, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), kind, module, name, "String", "Long", List.of(), values == null ? List.of() : values,
                null, List.of(), null, force, false, false, null, null, null, null, null, false,
                false, false, false, false));
    }

    private List<String> createWithMessage(String kind, String name, String module, String message, boolean force) throws IOException {
        requireDevelopmentGate();
        return paths(hive.create(projectContext.current(), kind, module, name, "String", "Long", List.of(), List.of(), message,
                List.of(), null, force, false, false, null, null, null, null, null, false,
                false, false, false, false));
    }

    @McpTool(name = "hive_check", description = "Check whether the configured workspace has the required Hive marker, configuration, pom.xml, and source roots. Use it before generating artifacts when project state is unknown.")
    public List<String> check() throws IOException {
        return hive.check(projectContext.current());
    }

    @McpTool(name = "hive_inspect", description = "Inspect the current Hive workspace. Use target=config for configuration, target=model for .hive/model files, or target=generated for the generated manifest.")
    public Map<String, Object> inspect(String target) throws IOException {
        Path projectRoot = projectContext.current();
        HiveConfig config = configs.load(projectRoot);
        return switch (target == null ? "config" : target) {
            case "config" -> Map.of("projectRoot", projectRoot.toString(), "basePackage", config.basePackage(),
                    "layout", config.layout(), "javaSourceRoot", config.javaSourceRoot(),
                    "testSourceRoot", config.testSourceRoot());
            case "model" -> Map.of("files", Files.isDirectory(projectRoot.resolve(".hive/model"))
                    ? Files.walk(projectRoot.resolve(".hive/model")).filter(Files::isRegularFile)
                    .map(path -> projectRoot.relativize(path).toString()).sorted().toList() : List.of());
            case "generated" -> Map.of("manifest", Files.exists(projectRoot.resolve(".hive/generated.yml")));
            case "workflow" -> workflow.load(projectRoot)
                    .map(state -> workflow.describe(projectRoot, state))
                    .orElseGet(() -> Map.of("managed", false));
            default -> throw new IllegalArgumentException("Unknown inspect target: " + target);
        };
    }

    @McpTool(name = "hive_prepare_workflow", description = "Prepare the selected project's workflow gate for a work item. GREENFIELD work requires delivery-channel, framework, and persistence decisions by default. This records sources but does not infer or approve decisions.")
    public Map<String, Object> prepareWorkflow(
            String workItemId,
            String workType,
            String backlogProvider,
            String acceptanceCriteriaSource,
            List<String> additionalRequiredDecisions) throws IOException {
        return workflow.describe(projectContext.current(), workflow.prepare(
                projectContext.current(), workItemId, workType, backlogProvider,
                acceptanceCriteriaSource, additionalRequiredDecisions));
    }

    @McpTool(name = "hive_record_workflow_decision", description = "Record one workflow decision with status, value, and evidence source. CONFIRMED, INHERITED, and NOT_APPLICABLE resolve a gate; PROPOSED and OPEN do not.")
    public Map<String, Object> recordWorkflowDecision(
            String topic,
            String status,
            String value,
            String source) throws IOException {
        return workflow.describe(projectContext.current(), workflow.recordDecision(
                projectContext.current(), topic, status, value, source));
    }

    @McpTool(name = "hive_workflow_check", description = "Check deterministic prerequisites for a target workflow phase without changing files. Legacy projects without .hive/workflow.yml are reported as unmanaged.")
    public Map<String, Object> workflowCheck(String targetPhase) throws IOException {
        return workflow.describe(workflow.check(projectContext.current(), targetPhase));
    }

    @McpTool(name = "hive_advance_workflow", description = "Advance the selected project's workflow by one valid transition. Moving to READY fails until backlog, acceptance criteria, and all required decisions are resolved with sources.")
    public Map<String, Object> advanceWorkflow(String targetPhase) throws IOException {
        return workflow.describe(projectContext.current(), workflow.advance(projectContext.current(), targetPhase));
    }

    @McpTool(name = "hive_create_inbound", description = "Create an inbound REST, MCP, listener, or scheduler adapter plus transport models. Use operations as 'METHOD /path -> UseCase' for REST, or as UseCase names for other adapter types.")
    public List<String> createInbound(String type, String adapterName, String module, List<String> operations,
                                      boolean force) throws IOException {
        requireDevelopmentGate();
        InboundAdapterType adapterType = InboundAdapterType.valueOf(type.toUpperCase());
        List<InboundOperation> parsed = operations == null ? List.of() : operations.stream()
                .map(operation -> adapterType == InboundAdapterType.REST
                        ? InboundOperation.parseRest(operation)
                        : InboundOperation.useCaseOnly(operation))
                .toList();
        Path projectRoot = projectContext.current();
        HiveConfig config = configs.load(projectRoot);
        return paths(inbound.generate(projectRoot, config,
                new InboundGenerationRequest(adapterType, module, adapterName, parsed, force)));
    }


    private List<String> paths(List<Path> files) {
        return files.stream().map(projectContext.current()::relativize).map(Path::toString).toList();
    }

    private void requireDevelopmentGate() throws IOException {
        workflow.requireDevelopment(projectContext.current());
    }
}
