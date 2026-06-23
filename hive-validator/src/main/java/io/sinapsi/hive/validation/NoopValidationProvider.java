package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;

public enum NoopValidationProvider implements ValidationProvider {

    INSTANCE;

    @Override
    public <C extends Command> CommandValidator<C> commandValidator() {
        return CommandValidator.noop();
    }

}
