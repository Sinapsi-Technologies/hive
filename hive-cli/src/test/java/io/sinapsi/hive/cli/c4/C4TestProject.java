package io.sinapsi.hive.cli.c4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Builds the canonical {@code todo} module fixture used across the C4 tests — the same six files
 * from the feature's acceptance example, derived purely from their package locations.
 */
public final class C4TestProject {
    private C4TestProject() {
    }

    /** Modular layout: a single {@code todo} module under {@code com.example.app.modules.todo}. */
    public static void todoModule(Path root) throws IOException {
        Files.writeString(root.resolve(".hive-project"), "hive-toolkit\n");
        Files.writeString(root.resolve("hive.yml"), """
                basePackage: com.example.app
                layout: modular
                javaSourceRoot: src/main/java
                testSourceRoot: src/test/java
                """);
        String base = "src/main/java/com/example/app/modules/todo/";
        write(root, base + "infrastructure/adapters/in/TodoController.java");
        write(root, base + "application/ports/in/CreateTodoUseCase.java");
        write(root, base + "application/ports/in/commands/CreateTodoCommand.java");
        write(root, base + "application/services/CreateTodoService.java");
        write(root, base + "domain/aggregates/TodoList.java");
        write(root, base + "domain/valueobjects/TodoTitle.java");
        write(root, base + "application/ports/out/TodoListRepository.java");
        write(root, base + "infrastructure/adapters/out/InMemoryTodoListRepositoryAdapter.java");
    }

    public static void write(Path root, String relativePath) throws IOException {
        Path path = root.resolve(relativePath);
        Files.createDirectories(path.getParent());
        Files.writeString(path, "// test fixture\n");
    }
}
