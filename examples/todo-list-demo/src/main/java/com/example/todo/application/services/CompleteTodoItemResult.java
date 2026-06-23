package com.example.todo.application.services;

import io.sinapsi.hive.core.result.Result;

public record CompleteTodoItemResult(String todoListId, String todoItemId) implements Result {
}
