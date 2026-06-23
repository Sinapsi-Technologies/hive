package io.sinapsi.hive.archunit.fixtures.compliant.application.services;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.RegisterUseCase;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands.RegisterCommand;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StorePort;
import io.sinapsi.hive.archunit.fixtures.compliant.domain.Amount;

public final class RegisterService implements RegisterUseCase {
    private final StorePort store;

    public RegisterService(StorePort store) {
        this.store = store;
    }

    @Override
    public void register(RegisterCommand command) {
        store.store(new Amount(0));
    }
}
