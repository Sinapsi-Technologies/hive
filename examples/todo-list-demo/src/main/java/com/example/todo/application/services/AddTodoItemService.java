package com.example.todo.application.services;

import com.example.todo.application.ports.in.AddTodoItemUseCase;
import com.example.todo.application.ports.in.commands.AddTodoItemCommand;
import com.example.todo.application.ports.out.TodoListRepositoryPort;
import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.exceptions.TodoListNotFoundException;
import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import com.example.todo.domain.valueobjects.TodoTitle;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.ports.ClockPort;
import io.sinapsi.hive.ports.PublishEventPort;

public final class AddTodoItemService implements AddTodoItemUseCase {
    private final TodoListRepositoryPort repository;
    private final ClockPort clock;
    private final PublishEventPort<DomainEvent> events;

    public AddTodoItemService(
            TodoListRepositoryPort repository,
            ClockPort clock,
            PublishEventPort<DomainEvent> events
    ) {
        this.repository = repository;
        this.clock = clock;
        this.events = events;
    }

    @Override
    public AddTodoItemResult handle(AddTodoItemCommand input) {
        TodoListId todoListId = TodoListId.of(input.todoListId());
        TodoList todoList = repository.findById(todoListId)
                .orElseThrow(() -> new TodoListNotFoundException(todoListId));

        TodoItemId itemId = todoList.addItem(TodoTitle.of(input.title()), clock.now());
        repository.save(todoList);
        todoList.pullDomainEvents().forEach(events::publish);

        return new AddTodoItemResult(todoListId.value().toString(), itemId.value().toString(), input.title());
    }
}
