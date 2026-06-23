package io.sinapsi.hive.archunit.fixtures.compliant.infrastructure.adapters.in;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.RegisterUseCase;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands.RegisterCommand;

public final class RegisterController {
    private final RegisterUseCase useCase;

    public RegisterController(RegisterUseCase useCase) {
        this.useCase = useCase;
    }

    public void handle(String name) {
        useCase.register(new RegisterCommand(name));
    }
}
