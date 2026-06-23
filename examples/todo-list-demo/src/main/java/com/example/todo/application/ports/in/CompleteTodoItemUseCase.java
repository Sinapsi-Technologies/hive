package com.example.todo.application.ports.in;

import com.example.todo.application.ports.in.commands.CompleteTodoItemCommand;
import com.example.todo.application.services.CompleteTodoItemResult;
import io.sinapsi.hive.core.usecase.UseCase;

public interface CompleteTodoItemUseCase extends UseCase<CompleteTodoItemCommand, CompleteTodoItemResult> {
}
