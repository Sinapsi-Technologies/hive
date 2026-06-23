package com.example.todo.infrastructure.adapters.out;

import com.example.todo.application.ports.out.TodoListRepositoryPort;
import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.valueobjects.TodoListId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** Outbound adapter: a trivial in-memory store, good enough for tests and demos. */
public final class InMemoryTodoListRepositoryAdapter implements TodoListRepositoryPort {
    private final Map<TodoListId, TodoList> store = new ConcurrentHashMap<>();

    @Override
    public Optional<TodoList> findById(TodoListId id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public void save(TodoList todoList) {
        store.put(todoList.getId(), todoList);
    }
}
