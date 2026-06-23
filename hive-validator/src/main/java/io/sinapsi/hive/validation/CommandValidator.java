package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;

@FunctionalInterface
public interface CommandValidator<T extends Command> {

    void validate(T command);

    default CommandValidator<T> and(CommandValidator<T> other) {
        return command -> {
            validate(command);
            other.validate(command);
        };
    }

    static <T extends Command> CommandValidator<T> noop() {
        return command -> {};
    }

}
