package io.sinapsi.hive.factory;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.result.CommonErrorCode;
import io.sinapsi.hive.core.result.Result;
import io.sinapsi.hive.validation.CommandValidationException;
import io.sinapsi.hive.validation.CommandValidator;
import io.sinapsi.hive.validation.ValidationProviders;

public abstract class AbstractCommandFactory<C extends Command> {

    private final CommandValidator<C> validator;

    protected AbstractCommandFactory() {
        this.validator = ValidationProviders.get().commandValidator();
    }

    protected final Result<C> validate(C command) {
        try {
            validator.validate(command);
            return Result.success(command);
        } catch (CommandValidationException exception) {
            return Result.failure(CommonErrorCode.VALIDATION_FAILED,
                    exception.getMessage(), exception.errors());
        }
    }

}
