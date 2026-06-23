package io.sinapsi.hive.factory;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.validation.CommandValidator;
import io.sinapsi.hive.validation.ValidationProviders;

public abstract class AbstractCommandFactory<C extends Command> {

    private final CommandValidator<C> validator;

    protected AbstractCommandFactory() {
        this.validator = ValidationProviders.get().commandValidator();
    }

    protected final C validate(C command) {
        validator.validate(command);
        return command;
    }

}
