package com.example.todo.application.services;

import io.sinapsi.hive.core.result.Result;

public record AddTodoItemResult(String todoListId, String todoItemId, String title) implements Result {
}
