package io.sinapsi.hive.archunit.fixtures.compliant.application.ports.out;

import io.sinapsi.hive.archunit.fixtures.compliant.domain.Amount;

public interface StorePort {
    void store(Amount amount);
}
