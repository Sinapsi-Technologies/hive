package com.example.todo.application.ports.in.commands;

import io.sinapsi.hive.core.command.Command;
import jakarta.validation.constraints.NotBlank;

public record CompleteTodoItemCommand(@NotBlank String todoListId, @NotBlank String todoItemId)
        implements Command {
}
