package io.sinapsi.hive.ports;

import io.sinapsi.hive.core.port.OutputPort;
import java.time.Instant;

public interface ClockPort extends OutputPort {
    Instant now();
}
