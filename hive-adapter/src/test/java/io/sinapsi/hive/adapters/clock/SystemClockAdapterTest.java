package io.sinapsi.hive.adapters.clock;

import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.ports.ClockPort;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SystemClockAdapterTest {

    @Test
    void implementsClockPortAndOutputPort() {
        SystemClockAdapter adapter = new SystemClockAdapter();

        assertInstanceOf(ClockPort.class, adapter);
        assertInstanceOf(OutputPort.class, adapter);
    }

    @Test
    void usesSystemUtcClockByDefault() {
        SystemClockAdapter adapter = new SystemClockAdapter();

        assertNotNull(adapter.now());
    }

    @Test
    void returnsInstantFromConfiguredClock() {
        Instant fixedInstant = Instant.parse("2026-05-14T10:15:30Z");
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        SystemClockAdapter adapter = new SystemClockAdapter(fixedClock);

        assertEquals(fixedInstant, adapter.now());
    }

    @Test
    void rejectsNullClock() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SystemClockAdapter(null)
        );

        assertEquals("clock must not be null", exception.getMessage());
    }
}
