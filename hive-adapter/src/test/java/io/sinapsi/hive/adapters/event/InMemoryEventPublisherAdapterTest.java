package io.sinapsi.hive.adapters.event;

import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.ports.PublishEventPort;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InMemoryEventPublisherAdapterTest {

    @Test
    void implementsPublishEventPortAndOutputPort() {
        InMemoryEventPublisherAdapter<TestEvent> adapter = new InMemoryEventPublisherAdapter<>();

        assertInstanceOf(PublishEventPort.class, adapter);
        assertInstanceOf(OutputPort.class, adapter);
    }

    @Test
    void storesPublishedEventsInOrder() {
        InMemoryEventPublisherAdapter<TestEvent> adapter = new InMemoryEventPublisherAdapter<>();
        TestEvent first = new TestEvent("user-created");
        TestEvent second = new TestEvent("email-sent");

        adapter.publish(first);
        adapter.publish(second);

        assertEquals(List.of(first, second), adapter.publishedEvents());
    }

    @Test
    void exposesImmutableSnapshotOfPublishedEvents() {
        InMemoryEventPublisherAdapter<TestEvent> adapter = new InMemoryEventPublisherAdapter<>();
        adapter.publish(new TestEvent("user-created"));

        List<TestEvent> snapshot = adapter.publishedEvents();

        assertThrows(UnsupportedOperationException.class, () -> snapshot.add(new TestEvent("changed")));
        assertEquals(List.of(new TestEvent("user-created")), adapter.publishedEvents());
    }

    @Test
    void snapshotDoesNotChangeWhenNewEventsArePublished() {
        InMemoryEventPublisherAdapter<TestEvent> adapter = new InMemoryEventPublisherAdapter<>();
        adapter.publish(new TestEvent("first"));
        List<TestEvent> snapshot = adapter.publishedEvents();

        adapter.publish(new TestEvent("second"));

        assertEquals(List.of(new TestEvent("first")), snapshot);
        assertEquals(List.of(new TestEvent("first"), new TestEvent("second")), adapter.publishedEvents());
    }

    private record TestEvent(String name) implements Event {
    }
}
