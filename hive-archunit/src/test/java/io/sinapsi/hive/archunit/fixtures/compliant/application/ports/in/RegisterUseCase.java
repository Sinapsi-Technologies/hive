package io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands.RegisterCommand;

public interface RegisterUseCase {
    void register(RegisterCommand command);
}
