package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.C4TestProject;
import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProjectArchitectureScannerTest {
    @TempDir
    Path tempDir;

    private final ProjectArchitectureScanner scanner = new ProjectArchitectureScanner();

    private HiveConfig modularConfig() {
        return new HiveConfig("com.example.app", "modular", "src/main/java", "src/test/java");
    }

    @Test
    void detectsModuleAndKeepsOnlyComponentVisibleElements() throws IOException {
        C4TestProject.todoModule(tempDir);

        List<ArchitectureView> views = scanner.scan(tempDir, modularConfig(), null, DiagramLevel.COMPONENT);

        assertEquals(1, views.size());
        ArchitectureView view = views.getFirst();
        assertEquals("todo", view.slug());
        assertEquals("Todo", view.name());

        // The command and value object are scanned but hidden from the C3 diagram.
        List<String> ids = view.elements().stream().map(ArchitectureElement::id).toList();
        assertEquals(List.of(
                "todoController",
                "createTodoUseCase",
                "createTodoService",
                "todoList",
                "todoListRepository",
                "inMemoryTodoListRepositoryAdapter"
        ), ids);
    }

    @Test
    void classifiesElementsByPackage() throws IOException {
        C4TestProject.todoModule(tempDir);

        ArchitectureView view = scanner.scan(tempDir, modularConfig(), null, DiagramLevel.COMPONENT).getFirst();

        assertEquals(ArchitectureElementType.INBOUND_ADAPTER, typeOf(view, "todoController"));
        assertEquals(ArchitectureElementType.INPUT_PORT, typeOf(view, "createTodoUseCase"));
        assertEquals(ArchitectureElementType.APPLICATION_SERVICE, typeOf(view, "createTodoService"));
        assertEquals(ArchitectureElementType.DOMAIN_AGGREGATE, typeOf(view, "todoList"));
        assertEquals(ArchitectureElementType.OUTPUT_PORT, typeOf(view, "todoListRepository"));
        assertEquals(ArchitectureElementType.OUTBOUND_ADAPTER, typeOf(view, "inMemoryTodoListRepositoryAdapter"));
    }

    @Test
    void inferredRelationsFollowTheHexagonFlow() throws IOException {
        C4TestProject.todoModule(tempDir);

        ArchitectureView view = scanner.scan(tempDir, modularConfig(), null, DiagramLevel.COMPONENT).getFirst();

        assertEquals(5, view.relations().size());
    }

    @Test
    void filtersByRequestedModule() throws IOException {
        C4TestProject.todoModule(tempDir);
        C4TestProject.write(tempDir,
                "src/main/java/com/example/app/modules/billing/domain/aggregates/Invoice.java");

        assertEquals(2, scanner.scan(tempDir, modularConfig(), null, DiagramLevel.COMPONENT).size());
        List<ArchitectureView> todoOnly = scanner.scan(tempDir, modularConfig(), "todo", DiagramLevel.COMPONENT);
        assertEquals(1, todoOnly.size());
        assertEquals("todo", todoOnly.getFirst().slug());
    }

    @Test
    void supportsSingleLayoutUsingBasePackageLastSegment() throws IOException {
        Files.writeString(tempDir.resolve(".hive-project"), "hive-toolkit\n");
        HiveConfig config = new HiveConfig("com.example.todo", "single", "src/main/java", "src/test/java");
        C4TestProject.write(tempDir,
                "src/main/java/com/example/todo/infrastructure/adapters/in/TodoController.java");
        C4TestProject.write(tempDir,
                "src/main/java/com/example/todo/application/ports/in/CreateTodoUseCase.java");

        List<ArchitectureView> views = scanner.scan(tempDir, config, null, DiagramLevel.COMPONENT);

        assertEquals(1, views.size());
        assertEquals("todo", views.getFirst().slug());
    }

    @Test
    void returnsEmptyWhenNoSourceRoot() throws IOException {
        List<ArchitectureView> views = scanner.scan(tempDir, modularConfig(), null, DiagramLevel.COMPONENT);
        assertTrue(views.isEmpty());
    }

    private ArchitectureElementType typeOf(ArchitectureView view, String id) {
        return view.elements().stream()
                .filter(element -> element.id().equals(id))
                .findFirst()
                .orElseThrow()
                .type();
    }
}
