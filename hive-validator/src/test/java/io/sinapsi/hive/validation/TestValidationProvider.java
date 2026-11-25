package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;

public final class TestValidationProvider implements ValidationProvider {

    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command> CommandValidator<C> commandValidator() {
        return command -> {
            if (command instanceof CommandValidatorTest.BrokenCommand) {
                throw new IllegalStateException("Provider unavailable");
            }
            if (command instanceof CommandValidatorTest.CreateUserCommand createUserCommand) {
                CommandValidatorTest.CreateUserValidation.rules()
                        .validate(createUserCommand);
            }
        };
    }
}
