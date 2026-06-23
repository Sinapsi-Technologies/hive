package com.example.todo.domain.aggregates;

import com.example.todo.domain.entities.TodoItem;
import com.example.todo.domain.events.TodoItemAdded;
import com.example.todo.domain.events.TodoItemCompleted;
import com.example.todo.domain.exceptions.TodoItemNotFoundException;
import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import com.example.todo.domain.valueobjects.TodoTitle;
import io.sinapsi.hive.core.domain.AggregateRoot;
import io.sinapsi.hive.core.event.DomainEvent;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Aggregate root. It is the only entry point for changing its items, enforces the invariants,
 * and records domain events for the application layer to publish after persistence.
 */
public final class TodoList implements AggregateRoot<TodoListId> {
    private final TodoListId id;
    private final String name;
    private final List<TodoItem> items = new ArrayList<>();
    private final List<DomainEvent> domainEvents = new ArrayList<>();

    private TodoList(TodoListId id, String name) {
        this.id = Objects.requireNonNull(id, "id is required");
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Todo list name is required");
        }
        this.name = name.trim();
    }

    public static TodoList create(TodoListId id, String name) {
        return new TodoList(id, name);
    }

    /** Adds a new open item and records a {@link TodoItemAdded} event. */
    public TodoItemId addItem(TodoTitle title, Instant now) {
        TodoItemId itemId = TodoItemId.newId();
        items.add(new TodoItem(itemId, title, now));
        domainEvents.add(new TodoItemAdded(id, itemId, title));
        return itemId;
    }

    /** Completes an existing item and records a {@link TodoItemCompleted} event. */
    public void completeItem(TodoItemId itemId, Instant now) {
        TodoItem item = items.stream()
                .filter(candidate -> candidate.id().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new TodoItemNotFoundException(id, itemId));
        item.markCompleted(now);
        domainEvents.add(new TodoItemCompleted(id, itemId));
    }

    public String name() {
        return name;
    }

    public List<TodoItem> items() {
        return List.copyOf(items);
    }

    @Override
    public TodoListId getId() {
        return id;
    }

    @Override
    public List<DomainEvent> pullDomainEvents() {
        List<DomainEvent> pulled = List.copyOf(domainEvents);
        domainEvents.clear();
        return pulled;
    }
}
