package io.sinapsi.hive.archunit.fixtures.compliant.infrastructure.adapters.out;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StorePort;
import io.sinapsi.hive.archunit.fixtures.compliant.domain.Amount;

public final class StoreAdapter implements StorePort {
    @Override
    public void store(Amount amount) {
        // in-memory no-op fixture
    }
}
