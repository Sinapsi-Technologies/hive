package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;

public interface ValidationProvider {

    <C extends Command> CommandValidator<C> commandValidator();

}