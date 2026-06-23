package io.sinapsi.hive.ports;

import io.sinapsi.hive.core.port.OutputPort;

public interface FileStoragePort extends OutputPort {

    void save(String path, byte[] content);

    byte[] load(String path);

}