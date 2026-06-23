package com.example.todo.domain.valueobjects;

import io.sinapsi.hive.core.domain.Identifier;

import java.util.UUID;

/** Identity of a {@code TodoItem} entity inside a list. */
public record TodoItemId(UUID value) implements Identifier<UUID> {
    public TodoItemId {
        if (value == null) {
            throw new IllegalArgumentException("Todo item id is required");
        }
    }

    public static TodoItemId newId() {
        return new TodoItemId(UUID.randomUUID());
    }

    public static TodoItemId of(String value) {
        return new TodoItemId(UUID.fromString(value));
    }
}
