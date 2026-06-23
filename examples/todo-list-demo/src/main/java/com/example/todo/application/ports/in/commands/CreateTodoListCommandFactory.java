package com.example.todo.application.ports.in.commands;

import io.sinapsi.hive.factory.AbstractCommandFactory;

public final class CreateTodoListCommandFactory extends AbstractCommandFactory<CreateTodoListCommand> {
    public CreateTodoListCommand create(String name) {
        return validate(new CreateTodoListCommand(name));
    }
}
