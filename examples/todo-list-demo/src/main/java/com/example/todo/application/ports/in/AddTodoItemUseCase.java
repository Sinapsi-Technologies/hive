package com.example.todo.application.ports.in;

import com.example.todo.application.ports.in.commands.AddTodoItemCommand;
import com.example.todo.application.services.AddTodoItemResult;
import io.sinapsi.hive.core.usecase.UseCase;

public interface AddTodoItemUseCase extends UseCase<AddTodoItemCommand, AddTodoItemResult> {
}
