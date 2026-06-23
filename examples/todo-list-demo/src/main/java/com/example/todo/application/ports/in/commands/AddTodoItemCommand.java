package com.example.todo.application.ports.in.commands;

import io.sinapsi.hive.core.command.Command;
import jakarta.validation.constraints.NotBlank;

public record AddTodoItemCommand(@NotBlank String todoListId, @NotBlank String title) implements Command {
}
