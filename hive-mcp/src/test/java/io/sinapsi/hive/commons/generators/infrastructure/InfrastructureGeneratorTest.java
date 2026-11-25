package io.sinapsi.hive.commons.generators.infrastructure;

import io.sinapsi.hive.commons.model.HiveConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class InfrastructureGeneratorTest {

    @Test
    void generatesOutboundAdapter(@TempDir Path project) throws Exception {

        InfrastructureGenerator generator = new InfrastructureGenerator();

        generator.adapter(project, HiveConfig.defaults(), null, "Persistence", List.of("SaveUser"), null, false);

        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/infrastructure/adapters/out/PersistenceAdapter.java")));
    }

    @Test
    void preservesPortSuffixWhenAdapterReferencesFullPortName(@TempDir Path project) throws Exception {
        Path port = project.resolve("src/main/java/com/example/app/application/ports/out/TodoListRepositoryPort.java");
        Files.createDirectories(port.getParent());
        Files.writeString(port, """
                package com.example.app.application.ports.out;
                public interface TodoListRepositoryPort {
                    void save(String value);
                }
                """);

        new InfrastructureGenerator().adapter(project, HiveConfig.defaults(), null,
                "InMemoryTodoListRepository", List.of("TodoListRepositoryPort"), null, false);

        String source = Files.readString(project.resolve(
                "src/main/java/com/example/app/infrastructure/adapters/out/InMemoryTodoListRepositoryAdapter.java"));

        assertTrue(source.contains("TodoListRepositoryPort;"));
        assertTrue(source.contains("implements TodoListRepositoryPort"));
        assertTrue(!source.contains("TodoListRepositoryPortPort"));
        assertTrue(source.contains("void save(String value)"));
    }

    @Test
    void doesNotDuplicateAdapterSuffix(@TempDir Path project) throws Exception {
        new InfrastructureGenerator().adapter(project, HiveConfig.defaults(), null,
                "InMemoryTodoListRepositoryAdapter", List.of("TodoListRepositoryPort"), null, false);

        assertTrue(Files.exists(project.resolve(
                "src/main/java/com/example/app/infrastructure/adapters/out/InMemoryTodoListRepositoryAdapter.java")));
        assertTrue(!Files.exists(project.resolve(
                "src/main/java/com/example/app/infrastructure/adapters/out/InMemoryTodoListRepositoryAdapterAdapter.java")));
    }

    @Test
    void placesGroupedAdapterUnderItsPackageDirectory(@TempDir Path project) throws Exception {
        new InfrastructureGenerator().adapter(project, HiveConfig.defaults(), "todo",
                "InMemoryTodoListRepository", List.of("TodoListRepositoryPort"), "persistence", false);

        Path generated = project.resolve(
                "src/main/java/com/example/app/modules/todo/infrastructure/adapters/out/persistence/InMemoryTodoListRepositoryAdapter.java");
        assertTrue(Files.exists(generated));
        assertTrue(Files.readString(generated).startsWith(
                "package com.example.app.modules.todo.infrastructure.adapters.out.persistence;"));
    }

    @Test
    void importsParameterTypesButNeverParameterNames(@TempDir Path project) throws Exception {
        Path aggregate = project.resolve(
                "src/main/java/com/example/app/modules/todo/domain/aggregates/TodoList.java");
        Files.createDirectories(aggregate.getParent());
        Files.writeString(aggregate, "package com.example.app.modules.todo.domain.aggregates; public class TodoList {}\n");

        Path port = project.resolve(
                "src/main/java/com/example/app/modules/todo/application/ports/out/TodoListStorePort.java");
        Files.createDirectories(port.getParent());
        Files.writeString(port, """
                package com.example.app.modules.todo.application.ports.out;
                import com.example.app.modules.todo.domain.aggregates.TodoList;
                public interface TodoListStorePort {
                    void save(TodoList todoList);
                }
                """);

        new InfrastructureGenerator().adapter(project, HiveConfig.defaults(), "todo",
                "InMemoryTodoListRepository", List.of("TodoListStorePort"), null, false);

        String source = Files.readString(project.resolve(
                "src/main/java/com/example/app/modules/todo/infrastructure/adapters/out/InMemoryTodoListRepositoryAdapter.java"));
        assertTrue(source.contains("import com.example.app.modules.todo.domain.aggregates.TodoList;"));
        assertTrue(!source.contains("import com.example.app.modules.todo.domain.aggregates.todoList;"));
    }
}
