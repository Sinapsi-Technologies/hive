package com.example.todo.domain.valueobjects;

import io.sinapsi.hive.core.domain.AggregateId;

import java.util.UUID;

/** Identity of the {@code TodoList} aggregate root. */
public record TodoListId(UUID value) implements AggregateId<UUID> {
    public TodoListId {
        if (value == null) {
            throw new IllegalArgumentException("Todo list id is required");
        }
    }

    public static TodoListId newId() {
        return new TodoListId(UUID.randomUUID());
    }

    public static TodoListId of(String value) {
        return new TodoListId(UUID.fromString(value));
    }
}
