package io.sinapsi.hive.archunit.fixtures.violations.infrastructuredep.infrastructure.adapters.in;

import io.sinapsi.hive.archunit.fixtures.violations.infrastructuredep.domain.Amount;

public final class BadController {
    public Amount handle() {
        return new Amount(1);
    }
}
