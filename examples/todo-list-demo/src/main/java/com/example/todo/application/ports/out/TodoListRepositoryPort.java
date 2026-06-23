package com.example.todo.application.ports.out;

import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.valueobjects.TodoListId;
import io.sinapsi.hive.core.port.OutputPort;

import java.util.Optional;

/** Outbound port: persistence capability for the {@code TodoList} aggregate. */
public interface TodoListRepositoryPort extends OutputPort {
    Optional<TodoList> findById(TodoListId id);

    void save(TodoList todoList);
}
