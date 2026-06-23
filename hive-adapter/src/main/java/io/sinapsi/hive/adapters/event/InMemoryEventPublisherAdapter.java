package io.sinapsi.hive.adapters.event;

import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.ports.PublishEventPort;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class InMemoryEventPublisherAdapter<E extends Event>
    implements PublishEventPort<E> {

    private final List<E> publishedEvents = new CopyOnWriteArrayList<>();

    @Override
    public void publish(E event) {
        publishedEvents.add(event);
    }

    public List<E> publishedEvents() {
        return List.copyOf(publishedEvents);
    }

}