package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;

public final class JakartaValidationProvider implements ValidationProvider {

    @Override
    public <C extends Command> CommandValidator<C> commandValidator() {
        return JakartaCommandValidator.create();
    }
}
