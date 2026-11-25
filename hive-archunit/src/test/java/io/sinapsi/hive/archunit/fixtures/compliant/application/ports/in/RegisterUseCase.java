package io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands.RegisterCommand;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.views.RegisterView;

public interface RegisterUseCase {
    RegisterView register(RegisterCommand command);
}
