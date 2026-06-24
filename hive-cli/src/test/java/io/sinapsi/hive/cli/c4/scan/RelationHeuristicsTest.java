package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import io.sinapsi.hive.cli.c4.model.ArchitectureRelation;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelationHeuristicsTest {
    private final RelationHeuristics heuristics = new RelationHeuristics();

    private static ArchitectureElement element(String id, String className, ArchitectureElementType type) {
        return new ArchitectureElement(id, className, className, type);
    }

    @Test
    void buildsTheHexagonFlowForTheTodoModule() {
        List<ArchitectureElement> elements = List.of(
                element("todoController", "TodoController", ArchitectureElementType.INBOUND_ADAPTER),
                element("createTodoUseCase", "CreateTodoUseCase", ArchitectureElementType.INPUT_PORT),
                element("createTodoService", "CreateTodoService", ArchitectureElementType.APPLICATION_SERVICE),
                element("todoList", "TodoList", ArchitectureElementType.DOMAIN_AGGREGATE),
                element("todoListRepository", "TodoListRepository", ArchitectureElementType.OUTPUT_PORT),
                element("inMemoryTodoListRepositoryAdapter", "InMemoryTodoListRepositoryAdapter",
                        ArchitectureElementType.OUTBOUND_ADAPTER));

        List<ArchitectureRelation> relations = heuristics.build(elements);

        assertEquals(List.of(
                new ArchitectureRelation("todoController", "createTodoUseCase", "calls"),
                new ArchitectureRelation("createTodoUseCase", "createTodoService", "implemented by"),
                new ArchitectureRelation("createTodoService", "todoList", "uses"),
                new ArchitectureRelation("createTodoService", "todoListRepository", "uses"),
                new ArchitectureRelation("inMemoryTodoListRepositoryAdapter", "todoListRepository", "implements")
        ), relations);
    }

    @Test
    void doesNotConnectAcrossBoundedContexts() {
        List<ArchitectureElement> elements = List.of(
                element("todoController", "TodoController", ArchitectureElementType.INBOUND_ADAPTER),
                element("pricingUseCase", "GetPricingUseCase", ArchitectureElementType.INPUT_PORT));

        List<ArchitectureRelation> relations = heuristics.build(elements);

        assertTrue(relations.isEmpty(), "Unrelated context should not be wired together");
    }

    @Test
    void matchesAdapterToPortByNameAcrossTechnologies() {
        List<ArchitectureElement> elements = List.of(
                element("httpPricingAdapter", "HttpPricingAdapter", ArchitectureElementType.OUTBOUND_ADAPTER),
                element("pricingPort", "PricingPort", ArchitectureElementType.OUTPUT_PORT));

        List<ArchitectureRelation> relations = heuristics.build(elements);

        assertEquals(List.of(new ArchitectureRelation("httpPricingAdapter", "pricingPort", "implements")), relations);
    }

    @Test
    void significantTokensStripRoleAndTechWords() {
        assertEquals(Set.of("todo", "list"),
                heuristics.significantTokens("InMemoryTodoListRepositoryAdapter"));
        assertEquals(Set.of("todo"), heuristics.significantTokens("CreateTodoUseCase"));
        assertFalse(heuristics.significantTokens("CreateTodoService").contains("create"));
    }
}
