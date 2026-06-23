package io.sinapsi.hive.core.usecase;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.port.InputPort;

@FunctionalInterface
public interface UseCase<I extends Command, O> extends InputPort {
    O handle(I input);
}
