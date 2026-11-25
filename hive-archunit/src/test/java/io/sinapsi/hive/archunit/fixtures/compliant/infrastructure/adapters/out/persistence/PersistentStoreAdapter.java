package io.sinapsi.hive.archunit.fixtures.compliant.infrastructure.adapters.out.persistence;

import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StorePort;
import io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out.StoreData;

public final class PersistentStoreAdapter implements StorePort {
    @Override
    public void store(StoreData amount) {
        // grouped outbound adapter fixture
    }
}
