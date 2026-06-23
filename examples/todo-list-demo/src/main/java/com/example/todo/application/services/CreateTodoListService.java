package com.example.todo.application.services;

import com.example.todo.application.ports.in.CreateTodoListUseCase;
import com.example.todo.application.ports.in.commands.CreateTodoListCommand;
import com.example.todo.application.ports.out.TodoListRepositoryPort;
import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.valueobjects.TodoListId;

public final class CreateTodoListService implements CreateTodoListUseCase {
    private final TodoListRepositoryPort repository;

    public CreateTodoListService(TodoListRepositoryPort repository) {
        this.repository = repository;
    }

    @Override
    public CreateTodoListResult handle(CreateTodoListCommand input) {
        TodoList todoList = TodoList.create(TodoListId.newId(), input.name());
        repository.save(todoList);
        return new CreateTodoListResult(todoList.getId().value().toString(), todoList.name());
    }
}
