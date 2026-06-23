package com.example.todo.application.ports.in;

import com.example.todo.application.ports.in.commands.CreateTodoListCommand;
import com.example.todo.application.services.CreateTodoListResult;
import io.sinapsi.hive.core.usecase.UseCase;

public interface CreateTodoListUseCase extends UseCase<CreateTodoListCommand, CreateTodoListResult> {
}
