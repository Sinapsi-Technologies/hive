package com.example.todo.infrastructure.adapters.in;

import com.example.todo.application.ports.in.AddTodoItemUseCase;
import com.example.todo.application.ports.in.CompleteTodoItemUseCase;
import com.example.todo.application.ports.in.CreateTodoListUseCase;
import com.example.todo.application.ports.in.commands.AddTodoItemCommandFactory;
import com.example.todo.application.ports.in.commands.CompleteTodoItemCommandFactory;
import com.example.todo.application.ports.in.commands.CreateTodoListCommandFactory;
import com.example.todo.application.services.AddTodoItemResult;
import com.example.todo.application.services.CompleteTodoItemResult;
import com.example.todo.application.services.CreateTodoListResult;
import com.example.todo.domain.exceptions.TodoItemNotFoundException;
import com.example.todo.domain.exceptions.TodoListNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbound adapter. It turns HTTP requests into validated commands via the command factories,
 * then delegates to the input ports (use cases). It never touches outbound adapters directly.
 */
@RestController
@RequestMapping("/todo-lists")
public final class TodoListController {
    private final CreateTodoListUseCase createTodoList;
    private final AddTodoItemUseCase addTodoItem;
    private final CompleteTodoItemUseCase completeTodoItem;

    private final CreateTodoListCommandFactory createTodoListCommands = new CreateTodoListCommandFactory();
    private final AddTodoItemCommandFactory addTodoItemCommands = new AddTodoItemCommandFactory();
    private final CompleteTodoItemCommandFactory completeTodoItemCommands = new CompleteTodoItemCommandFactory();

    public TodoListController(
            CreateTodoListUseCase createTodoList,
            AddTodoItemUseCase addTodoItem,
            CompleteTodoItemUseCase completeTodoItem
    ) {
        this.createTodoList = createTodoList;
        this.addTodoItem = addTodoItem;
        this.completeTodoItem = completeTodoItem;
    }

    @PostMapping
    CreateTodoListResult create(@RequestBody CreateTodoListRequest request) {
        return createTodoList.handle(createTodoListCommands.create(request.name()));
    }

    @PostMapping("/{todoListId}/items")
    AddTodoItemResult addItem(@PathVariable String todoListId, @RequestBody AddTodoItemRequest request) {
        return addTodoItem.handle(addTodoItemCommands.create(todoListId, request.title()));
    }

    @PostMapping("/{todoListId}/items/{todoItemId}/completion")
    CompleteTodoItemResult complete(@PathVariable String todoListId, @PathVariable String todoItemId) {
        return completeTodoItem.handle(completeTodoItemCommands.create(todoListId, todoItemId));
    }

    @ExceptionHandler({TodoListNotFoundException.class, TodoItemNotFoundException.class})
    @ResponseStatus(HttpStatus.NOT_FOUND)
    String handleNotFound(RuntimeException exception) {
        return exception.getMessage();
    }

    @ExceptionHandler({ConstraintViolationException.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    String handleBadRequest(RuntimeException exception) {
        return exception.getMessage();
    }

    record CreateTodoListRequest(String name) {
    }

    record AddTodoItemRequest(String title) {
    }
}
