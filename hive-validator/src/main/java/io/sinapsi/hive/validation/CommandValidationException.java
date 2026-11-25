package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.result.ErrorDetail;
import java.util.List;

/** Expected command violations, independent of the validation provider. */
public final class CommandValidationException extends RuntimeException {
    private final List<ErrorDetail> errors;

    public CommandValidationException(List<ErrorDetail> errors) {
        super("Command validation failed");
        this.errors = List.copyOf(errors);
        if (this.errors.isEmpty()) {
            throw new IllegalArgumentException("validation errors must not be empty");
        }
    }

    public List<ErrorDetail> errors() {
        return errors;
    }
}
