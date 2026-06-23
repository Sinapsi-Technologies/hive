package io.sinapsi.hive.archunit.fixtures.compliant.domain;

public final class Amount {
    private final long cents;

    public Amount(long cents) {
        this.cents = cents;
    }

    public long cents() {
        return cents;
    }
}
