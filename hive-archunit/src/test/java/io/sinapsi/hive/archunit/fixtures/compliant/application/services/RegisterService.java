package io.sinapsi.hive.archunit.fixtures.compliant.application.services;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.RegisterUseCase;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.views.RegisterView;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands.RegisterCommand;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StorePort;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StoreData;
import io.sinapsi.hive.archunit.fixtures.compliant.domain.Amount;

public final class RegisterService implements RegisterUseCase {
    private final StorePort store;

    public RegisterService(StorePort store) {
        this.store = store;
    }

    @Override
    public RegisterView register(RegisterCommand command) {
        Amount amount = new Amount(0);
        store.store(new StoreData((int) amount.cents()));
        return RegisterView.of("registered");
    }
}
