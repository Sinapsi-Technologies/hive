package io.sinapsi.hive.infrastructure.validation;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.validation.CommandValidator;
import jakarta.validation.ConstraintViolation;
import io.sinapsi.hive.core.result.ErrorDetail;
import io.sinapsi.hive.validation.CommandValidationException;
import java.util.Comparator;
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
            var errors = violations.stream()
                    .map(violation -> new ErrorDetail(
                            violation.getPropertyPath().toString(), violation.getMessage()))
                    .sorted(Comparator.comparing(ErrorDetail::field).thenComparing(ErrorDetail::message))
                    .toList();
            throw new CommandValidationException(errors);
        }

    }

}
