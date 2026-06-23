package com.example.todo.domain.events;

import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import io.sinapsi.hive.core.event.DomainEvent;

public record TodoItemCompleted(TodoListId todoListId, TodoItemId todoItemId) implements DomainEvent {
}
