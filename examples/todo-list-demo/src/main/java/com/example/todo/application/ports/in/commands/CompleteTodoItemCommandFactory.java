package com.example.todo.application.ports.in.commands;

import io.sinapsi.hive.factory.AbstractCommandFactory;

public final class CompleteTodoItemCommandFactory extends AbstractCommandFactory<CompleteTodoItemCommand> {
    public CompleteTodoItemCommand create(String todoListId, String todoItemId) {
        return validate(new CompleteTodoItemCommand(todoListId, todoItemId));
    }
}
