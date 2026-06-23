package com.example.todo.domain.exceptions;

import com.example.todo.domain.valueobjects.TodoListId;

public final class TodoListNotFoundException extends RuntimeException {
    public TodoListNotFoundException(TodoListId id) {
        super("Todo list not found: " + id.value());
    }
}
