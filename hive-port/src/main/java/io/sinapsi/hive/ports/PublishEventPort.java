package io.sinapsi.hive.ports;

import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.core.port.OutputPort;

public interface PublishEventPort<E extends Event> extends OutputPort {
    void publish(E event);
}
