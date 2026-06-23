package io.sinapsi.hive.ports;

import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.core.domain.Identifier;

public interface IdGeneratorPort<T extends Identifier<?>> extends OutputPort {
    T generate();
}
