package io.sinapsi.hive.mcp;

import io.sinapsi.hive.commons.HiveOperations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import io.sinapsi.hive.mcp.workflow.WorkflowGateException;

class HiveMcpToolsTest {

    @Test
    void createsExactlyTheExpectedValueObject(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        HiveMcpTools tools = new HiveMcpTools(new HiveOperations(), project.toString());

        assertEquals(List.of("src/main/java/com/example/test/domain/valueobjects/Email.java"),
                tools.createValueObject("Email", "String", false, true, false));

        assertEquals("""
                package com.example.test.domain.valueobjects;


                public record Email(String value) {
                    public Email {
                        if (value == null || value.isBlank()) {
                            throw new IllegalArgumentException("value must not be blank");
                        }
                    }

                    public static Email of(String value) {
                        return new Email(value);
                    }


                }
                """, Files.readString(project.resolve("src/main/java/com/example/test/domain/valueobjects/Email.java")));

    }

    @Test
    void createsValueObjectInsideSelectedModule(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        HiveMcpTools tools = new HiveMcpTools(new HiveOperations(), project.toString());

        assertEquals(List.of("src/main/java/com/example/test/modules/orders/domain/valueobjects/Email.java"),
                tools.createValueObject("Email", "String", "orders", false, true, false));
        assertTrue(Files.exists(project.resolve(
                "src/main/java/com/example/test/modules/orders/domain/valueobjects/Email.java")));
    }

    @Test
    void checksTheConfiguredProject(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve(".hive-project"), "hive-toolkit\n");
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        HiveMcpTools tools = new HiveMcpTools(new HiveOperations(), project.toString());
        assertTrue(tools.check().stream().anyMatch(warning -> warning.contains("pom.xml")));
    }

    @Test
    void createsAnId(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createId("UserId", "UUID", null, false).size());
    }

    @Test
    void createsAnEntity(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createEntity("User", "UserId", List.of("email:String"), null, false).size());
    }

    @Test
    void createsAnAggregate(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createAggregate("Order", "OrderId", List.of("status:String"), null, false).size());
    }

    @Test
    void createsAnEnum(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createEnum("OrderStatus", List.of("DRAFT", "PAID"), null, false).size());
    }

    @Test
    void createsAnEvent(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createEvent("OrderCreated", List.of("orderId:OrderId"), null, false).size());
    }

    @Test
    void createsAnException(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createException("OrderFailed", "Order failed", null, false).size());
    }

    @Test
    void createsADomainService(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createDomainService("Pricing", null, false).size());
    }

    @Test
    void createsASnapshot(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createSnapshot("OrderSnapshot", List.of("id:OrderId"), null, false).size());
    }

    @Test
    void createsARecord(@TempDir Path project) throws Exception {
        assertEquals(List.of("src/main/java/com/example/test/commons/UserData.java"),
                tools(project).createRecord("UserData", List.of("id:UUID"), null, false));
        String source = Files.readString(project.resolve("src/main/java/com/example/test/commons/UserData.java"));
        assertTrue(source.contains("public record UserData("));
        assertFalse(source.contains("static UserData of("));
    }

    @Test
    void createsAViewNearInputPortsWithAFactory(@TempDir Path project) throws Exception {
        assertEquals(List.of("src/main/java/com/example/test/application/ports/in/views/UserView.java"),
                tools(project).createRecord("UserView", List.of("id:UUID", "name:String"), null, false));
        String source = Files.readString(project.resolve(
                "src/main/java/com/example/test/application/ports/in/views/UserView.java"));
        assertTrue(source.contains("package com.example.test.application.ports.in.views;"));
        assertTrue(source.contains("public static UserView of(UUID id, String name)"));
        assertTrue(source.contains("return new UserView(id, name);"));
    }

    @Test
    void createsAViewInsideSelectedModule(@TempDir Path project) throws Exception {
        assertEquals(List.of("src/main/java/com/example/test/modules/orders/application/ports/in/views/OrderView.java"),
                tools(project).createRecord("OrderView", List.of(), "orders", false));
        String source = Files.readString(project.resolve(
                "src/main/java/com/example/test/modules/orders/application/ports/in/views/OrderView.java"));
        assertTrue(source.contains("package com.example.test.modules.orders.application.ports.in.views;"));
        assertTrue(source.contains("public static OrderView of()"));
    }

    @Test
    void createsAClass(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createClass("UserDto", List.of("name:String"), null, true, false, false, false, false).size());
    }

    @Test
    void createsACommand(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createCommand("CreateUser", List.of("email:String"), null, false).size());
    }

    @Test
    void createsAUseCase(@TempDir Path project) throws Exception {
        assertEquals(2, tools(project).createUseCase("RegisterUser", List.of("email:String"), null, true, false).size());
    }

    @Test
    void createsAPort(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createPort("LoadUser", List.of("Optional<User> load(UserId id)"), null, false).size());
    }

    @Test
    void createsAnAdapter(@TempDir Path project) throws Exception {
        HiveMcpTools tools = tools(project);
        tools.createPort("SaveUser", List.of("void save(User user)"), null, false);
        assertEquals(1, tools.createAdapter("UserPersistence", List.of("SaveUserPort"), "persistence", null, false).size());
    }

    @Test
    void createsAModule(@TempDir Path project) throws Exception {
        assertTrue(tools(project).createModule("customer").size() > 1);
    }

    @Test
    void createsAnArchitectureTest(@TempDir Path project) throws Exception {
        assertEquals(1, tools(project).createArchTest(false).size());
    }

    @Test
    void createsAnInboundAdapter(@TempDir Path project) throws Exception {
        HiveMcpTools tools = tools(project);
        tools.createUseCase("RegisterUser", List.of("email:String"), null, true, false);
        assertFalse(tools.createInbound("rest", "UserController", null,
            List.of("GET /users -> RegisterUser"), false).isEmpty());
    }

    @Test
    void newlyInitializedProjectBlocksGenerationUntilGreenfieldDecisionsAreResolved(
            @TempDir Path project) throws Exception {
        HiveMcpTools tools = new HiveMcpTools(new HiveOperations(), project.toString());

        List<String> initialized = tools.initProject(project.toString(), false, false);

        assertTrue(initialized.contains(".hive/workflow.yml"));
        WorkflowGateException blocked = assertThrows(
                WorkflowGateException.class,
                () -> tools.createId("UserId", "UUID", null, false));
        assertTrue(blocked.getMessage().contains("GATE_NOT_READY"));
        assertTrue(blocked.getMessage().contains("delivery-channel"));

        tools.prepareWorkflow("TODO-1", "greenfield", "local", "documents/requirements.md", List.of());
        tools.recordWorkflowDecision("delivery-channel", "confirmed", "REST API", "user decision");
        tools.recordWorkflowDecision("framework", "confirmed", "Spring Boot", "user decision");

        WorkflowGateException missingPersistence = assertThrows(
                WorkflowGateException.class,
                () -> tools.advanceWorkflow("ready"));
        assertTrue(missingPersistence.getMessage().contains("persistence"));

        tools.recordWorkflowDecision("persistence", "confirmed", "PostgreSQL", "user decision");
        assertEquals("READY", tools.advanceWorkflow("ready").get("phase").toString());
        assertThrows(WorkflowGateException.class,
                () -> tools.createId("UserId", "UUID", null, false));
        assertEquals("DEVELOPMENT", tools.advanceWorkflow("development").get("phase").toString());
        assertEquals(1, tools.createId("UserId", "UUID", null, false).size());
    }

    private HiveMcpTools tools(Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.example.test\n");
        return new HiveMcpTools(new HiveOperations(), project.toString());
    }
}
