package com.example.todo.application.services;

import com.example.todo.application.ports.in.CompleteTodoItemUseCase;
import com.example.todo.application.ports.in.commands.CompleteTodoItemCommand;
import com.example.todo.application.ports.out.TodoListRepositoryPort;
import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.exceptions.TodoListNotFoundException;
import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.ports.ClockPort;
import io.sinapsi.hive.ports.PublishEventPort;

public final class CompleteTodoItemService implements CompleteTodoItemUseCase {
    private final TodoListRepositoryPort repository;
    private final ClockPort clock;
    private final PublishEventPort<DomainEvent> events;

    public CompleteTodoItemService(
            TodoListRepositoryPort repository,
            ClockPort clock,
            PublishEventPort<DomainEvent> events
    ) {
        this.repository = repository;
        this.clock = clock;
        this.events = events;
    }

    @Override
    public CompleteTodoItemResult handle(CompleteTodoItemCommand input) {
        TodoListId todoListId = TodoListId.of(input.todoListId());
        TodoItemId todoItemId = TodoItemId.of(input.todoItemId());
        TodoList todoList = repository.findById(todoListId)
                .orElseThrow(() -> new TodoListNotFoundException(todoListId));

        todoList.completeItem(todoItemId, clock.now());
        repository.save(todoList);
        todoList.pullDomainEvents().forEach(events::publish);

        return new CompleteTodoItemResult(todoListId.value().toString(), todoItemId.value().toString());
    }
}
