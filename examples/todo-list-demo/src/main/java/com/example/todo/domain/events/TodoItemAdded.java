package com.example.todo.domain.events;

import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import com.example.todo.domain.valueobjects.TodoTitle;
import io.sinapsi.hive.core.event.DomainEvent;

public record TodoItemAdded(TodoListId todoListId, TodoItemId todoItemId, TodoTitle title)
        implements DomainEvent {
}
