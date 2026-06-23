package io.sinapsi.hive.ports;

import io.sinapsi.hive.core.domain.Identifier;
import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.core.port.OutputPort;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class HivePortsTest {

    @Test
    void clockPortIsAnOutputPortAndReturnsAnInstant() {
        Instant fixedInstant = Instant.parse("2026-05-14T10:15:30Z");
        ClockPort clock = () -> fixedInstant;

        assertInstanceOf(OutputPort.class, clock);
        assertEquals(fixedInstant, clock.now());
    }

    @Test
    void idGeneratorPortIsAnOutputPortAndReturnsAnIdentifier() {
        IdGeneratorPort<TestId> generator = () -> new TestId("id-1");

        assertInstanceOf(OutputPort.class, generator);
        assertEquals(new TestId("id-1"), generator.generate());
    }

    @Test
    void publishEventPortIsAnOutputPortAndPublishesEvents() {
        List<UserCreatedEvent> published = new ArrayList<>();
        PublishEventPort<UserCreatedEvent> publisher = published::add;
        UserCreatedEvent event = new UserCreatedEvent("user-1");

        publisher.publish(event);

        assertInstanceOf(OutputPort.class, publisher);
        assertEquals(List.of(event), published);
    }

    @Test
    void fileStoragePortIsAnOutputPortAndStoresBytesByPath() {
        FileStoragePort storage = new InMemoryFileStoragePort();
        byte[] content = "hello".getBytes();

        storage.save("docs/readme.txt", content);

        assertInstanceOf(OutputPort.class, storage);
        assertArrayEquals(content, storage.load("docs/readme.txt"));
    }

    private record TestId(String value) implements Identifier<String> {
    }

    private record UserCreatedEvent(String userId) implements Event {
    }

    private static final class InMemoryFileStoragePort implements FileStoragePort {
        private final Map<String, byte[]> files = new HashMap<>();

        @Override
        public void save(String path, byte[] content) {
            files.put(path, content.clone());
        }

        @Override
        public byte[] load(String path) {
            return files.get(path).clone();
        }
    }
}
