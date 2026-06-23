package com.example.todo.application.ports.in.commands;

import io.sinapsi.hive.factory.AbstractCommandFactory;

public final class AddTodoItemCommandFactory extends AbstractCommandFactory<AddTodoItemCommand> {
    public AddTodoItemCommand create(String todoListId, String title) {
        return validate(new AddTodoItemCommand(todoListId, title));
    }
}
