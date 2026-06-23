package io.sinapsi.hive.core.event;

public interface EventHandler<E extends Event> {
    void handle(E event);
}
