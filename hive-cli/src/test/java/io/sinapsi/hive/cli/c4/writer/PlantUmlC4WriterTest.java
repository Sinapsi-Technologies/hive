package io.sinapsi.hive.cli.c4.writer;

import io.sinapsi.hive.cli.c4.C4TestProject;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.c4.scan.ProjectArchitectureScanner;
import io.sinapsi.hive.cli.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlantUmlC4WriterTest {
    @TempDir
    Path tempDir;

    private final PlantUmlC4Writer writer = new PlantUmlC4Writer();

    private ArchitectureView todoView(DiagramLevel level) throws IOException {
        C4TestProject.todoModule(tempDir);
        HiveConfig config = new HiveConfig("com.example.app", "modular", "src/main/java", "src/test/java");
        return new ProjectArchitectureScanner().scan(tempDir, config, "todo", level).getFirst();
    }

    @Test
    void fileNameUsesLevelPrefixAndSlug() throws IOException {
        assertEquals("c3-todo.puml", writer.fileName(todoView(DiagramLevel.COMPONENT)));
        assertEquals("c2-todo.puml", writer.fileName(todoView(DiagramLevel.CONTAINER)));
        assertEquals("c1-todo.puml", writer.fileName(todoView(DiagramLevel.CONTEXT)));
    }

    @Test
    void componentDiagramMatchesTheAcceptanceExample() throws IOException {
        String puml = writer.render(todoView(DiagramLevel.COMPONENT));

        assertTrue(puml.contains("@startuml C3_Todo"), puml);
        assertTrue(puml.contains("C4_Component.puml"), puml);
        assertTrue(puml.contains("title Todo Module - C3 Component Diagram"), puml);
        assertTrue(puml.contains("LAYOUT_WITH_LEGEND()"), puml);
        assertTrue(puml.contains("AddElementTag(\"Inbound Adapter\""), puml);

        assertTrue(puml.contains(
                "Component(todoController, \"TodoController\", \"Inbound Adapter\""), puml);
        assertTrue(puml.contains("Component(createTodoUseCase, \"CreateTodoUseCase\", \"Input Port\""), puml);
        assertTrue(puml.contains("Component(createTodoService, \"CreateTodoService\", \"Application Service\""), puml);
        assertTrue(puml.contains("Component(todoList, \"TodoList\", \"Domain Aggregate\""), puml);
        assertTrue(puml.contains("Component(todoListRepository, \"TodoListRepository\", \"Output Port\""), puml);
        assertTrue(puml.contains("Component(inMemoryTodoListRepositoryAdapter, "
                + "\"InMemoryTodoListRepositoryAdapter\", \"Outbound Adapter\""), puml);

        assertTrue(puml.contains("Rel(todoController, createTodoUseCase, \"calls\")"), puml);
        assertTrue(puml.contains("Rel(createTodoUseCase, createTodoService, \"implemented by\")"), puml);
        assertTrue(puml.contains("Rel(createTodoService, todoList, \"uses\")"), puml);
        assertTrue(puml.contains("Rel(createTodoService, todoListRepository, \"uses\")"), puml);
        assertTrue(puml.contains("Rel(inMemoryTodoListRepositoryAdapter, todoListRepository, \"implements\")"), puml);

        assertTrue(puml.contains("@enduml"), puml);
    }

    @Test
    void componentDiagramDoesNotIncludeHiddenTypes() throws IOException {
        String puml = writer.render(todoView(DiagramLevel.COMPONENT));

        assertTrue(!puml.contains("CreateTodoCommand"), "Commands should be hidden for cleanliness");
        assertTrue(!puml.contains("TodoTitle"), "Value objects should be hidden for cleanliness");
    }

    @Test
    void renderingIsDeterministic() throws IOException {
        ArchitectureView view = todoView(DiagramLevel.COMPONENT);
        assertEquals(writer.render(view), writer.render(view));
    }

    @Test
    void containerAndContextDiagramsAreValid() throws IOException {
        String container = writer.render(todoView(DiagramLevel.CONTAINER));
        assertTrue(container.contains("@startuml C2_Todo"), container);
        assertTrue(container.contains("C4_Container.puml"), container);
        assertTrue(container.contains("@enduml"), container);

        String context = writer.render(todoView(DiagramLevel.CONTEXT));
        assertTrue(context.contains("@startuml C1_Todo"), context);
        assertTrue(context.contains("C4_Context.puml"), context);
        assertTrue(context.contains("Person(user"), context);
        assertTrue(context.contains("@enduml"), context);
    }
}
