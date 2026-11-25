package io.sinapsi.hive.infrastructure.validation;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.validation.CommandValidator;
import io.sinapsi.hive.validation.ValidationProvider;

public final class JakartaValidationProvider implements ValidationProvider {

    @Override
    public <C extends Command> CommandValidator<C> commandValidator() {
        return JakartaCommandValidator.create();
    }
}
