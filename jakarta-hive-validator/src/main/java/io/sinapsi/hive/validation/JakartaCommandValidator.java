package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

public final class JakartaCommandValidator<T extends Command> implements CommandValidator<T> {

    private static final Validator VALIDATOR;

    static {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();

        VALIDATOR = factory.getValidator();
    }

    private JakartaCommandValidator() {}

    public static <T extends Command> JakartaCommandValidator<T> create() {
        return new JakartaCommandValidator<>();
    }

    @Override
    public void validate(T command) {

        Set<ConstraintViolation<T>> violations = VALIDATOR.validate(command);

        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

    }

}
