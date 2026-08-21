package io.sinapsi.hive.archunit.fixtures.compliant.infrastructure.adapters.out.persistence;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StorePort;
import io.sinapsi.hive.archunit.fixtures.compliant.domain.Amount;

public final class PersistentStoreAdapter implements StorePort {
    @Override
    public void store(Amount amount) {
        // grouped outbound adapter fixture
    }
}
