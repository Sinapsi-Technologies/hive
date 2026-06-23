package com.example.todo.domain.entities;

import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoStatus;
import com.example.todo.domain.valueobjects.TodoTitle;

import java.time.Instant;
import java.util.Objects;

/**
 * Entity living inside the {@code TodoList} aggregate. It has identity and a mutable lifecycle
 * (OPEN -&gt; DONE); state transitions are driven by the aggregate, never from outside.
 */
public final class TodoItem {
    private final TodoItemId id;
    private final TodoTitle title;
    private final Instant createdAt;
    private TodoStatus status;
    private Instant completedAt;

    public TodoItem(TodoItemId id, TodoTitle title, Instant createdAt) {
        this.id = Objects.requireNonNull(id, "id is required");
        this.title = Objects.requireNonNull(title, "title is required");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt is required");
        this.status = TodoStatus.OPEN;
    }

    public void markCompleted(Instant when) {
        if (status == TodoStatus.DONE) {
            throw new IllegalStateException("Todo item " + id.value() + " is already completed");
        }
        this.status = TodoStatus.DONE;
        this.completedAt = Objects.requireNonNull(when, "completion time is required");
    }

    public TodoItemId id() {
        return id;
    }

    public TodoTitle title() {
        return title;
    }

    public TodoStatus status() {
        return status;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public Instant completedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TodoItem todoItem && id.equals(todoItem.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
