package com.example.todo.domain.exceptions;

import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;

public final class TodoItemNotFoundException extends RuntimeException {
    public TodoItemNotFoundException(TodoListId todoListId, TodoItemId todoItemId) {
        super("Todo item " + todoItemId.value() + " not found in list " + todoListId.value());
    }
}
