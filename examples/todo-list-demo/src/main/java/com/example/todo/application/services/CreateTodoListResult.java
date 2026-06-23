package com.example.todo.application.services;

import io.sinapsi.hive.core.result.Result;

public record CreateTodoListResult(String todoListId, String name) implements Result {
}
