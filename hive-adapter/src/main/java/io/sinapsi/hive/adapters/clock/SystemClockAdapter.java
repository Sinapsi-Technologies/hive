package io.sinapsi.hive.adapters.clock;

import io.sinapsi.hive.ports.ClockPort;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class SystemClockAdapter implements ClockPort {

    private final Clock clock;

    public SystemClockAdapter() {
        this(Clock.systemUTC());
    }

    public SystemClockAdapter(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock must not be null");
    }

    @Override
    public Instant now() {
        return clock.instant();
    }
}
