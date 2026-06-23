package com.example.todo;

import com.example.todo.application.ports.in.commands.AddTodoItemCommand;
import com.example.todo.application.ports.in.commands.CompleteTodoItemCommand;
import com.example.todo.application.ports.in.commands.CreateTodoListCommand;
import com.example.todo.application.services.AddTodoItemResult;
import com.example.todo.application.services.AddTodoItemService;
import com.example.todo.application.services.CompleteTodoItemService;
import com.example.todo.application.services.CreateTodoListResult;
import com.example.todo.application.services.CreateTodoListService;
import com.example.todo.domain.aggregates.TodoList;
import com.example.todo.domain.entities.TodoItem;
import com.example.todo.domain.events.TodoItemAdded;
import com.example.todo.domain.events.TodoItemCompleted;
import com.example.todo.domain.exceptions.TodoListNotFoundException;
import com.example.todo.domain.valueobjects.TodoItemId;
import com.example.todo.domain.valueobjects.TodoListId;
import com.example.todo.domain.valueobjects.TodoStatus;
import com.example.todo.infrastructure.adapters.out.InMemoryTodoListRepositoryAdapter;
import io.sinapsi.hive.adapters.clock.SystemClockAdapter;
import io.sinapsi.hive.adapters.event.InMemoryEventPublisherAdapter;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.ports.ClockPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TodoListFlowTest {
    private final InMemoryTodoListRepositoryAdapter repository = new InMemoryTodoListRepositoryAdapter();
    private final InMemoryEventPublisherAdapter<DomainEvent> events = new InMemoryEventPublisherAdapter<>();
    private final ClockPort clock = new SystemClockAdapter();

    private final CreateTodoListService createTodoList = new CreateTodoListService(repository);
    private final AddTodoItemService addTodoItem = new AddTodoItemService(repository, clock, events);
    private final CompleteTodoItemService completeTodoItem = new CompleteTodoItemService(repository, clock, events);

    @Test
    void createsAddsAndCompletesAnItemRaisingDomainEvents() {
        CreateTodoListResult list = createTodoList.handle(new CreateTodoListCommand("Groceries"));

        AddTodoItemResult added = addTodoItem.handle(new AddTodoItemCommand(list.todoListId(), "Buy milk"));

        TodoList afterAdd = repository.findById(TodoListId.of(list.todoListId())).orElseThrow();
        assertEquals(1, afterAdd.items().size());
        assertEquals("Buy milk", afterAdd.items().get(0).title().value());
        assertEquals(TodoStatus.OPEN, afterAdd.items().get(0).status());

        completeTodoItem.handle(new CompleteTodoItemCommand(list.todoListId(), added.todoItemId()));

        TodoItem completed = repository.findById(TodoListId.of(list.todoListId())).orElseThrow().items().get(0);
        assertEquals(TodoStatus.DONE, completed.status());
        assertNotNull(completed.completedAt());

        List<DomainEvent> published = events.publishedEvents();
        assertEquals(2, published.size());
        assertInstanceOf(TodoItemAdded.class, published.get(0));
        assertInstanceOf(TodoItemCompleted.class, published.get(1));
    }

    @Test
    void completingAnItemOnAMissingListFails() {
        CompleteTodoItemCommand command = new CompleteTodoItemCommand(
                TodoListId.newId().value().toString(),
                TodoItemId.newId().value().toString()
        );

        assertThrows(TodoListNotFoundException.class, () -> completeTodoItem.handle(command));
    }
}
