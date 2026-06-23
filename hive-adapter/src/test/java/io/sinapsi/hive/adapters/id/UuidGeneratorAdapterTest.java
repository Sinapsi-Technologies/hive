package io.sinapsi.hive.adapters.id;

import io.sinapsi.hive.core.domain.Identifier;
import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.ports.IdGeneratorPort;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UuidGeneratorAdapterTest {

    @Test
    void implementsIdGeneratorPortAndOutputPort() {
        UuidGeneratorAdapter<TestId> adapter = new UuidGeneratorAdapter<>(TestId::new);

        assertInstanceOf(IdGeneratorPort.class, adapter);
        assertInstanceOf(OutputPort.class, adapter);
    }

    @Test
    void generatesIdentifierFromUuidSupplier() {
        UUID uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UuidGeneratorAdapter<TestId> adapter = new UuidGeneratorAdapter<>(() -> uuid, TestId::new);

        TestId generated = adapter.generate();

        assertEquals(new TestId(uuid), generated);
    }

    @Test
    void callsIdentifierFactoryForEveryGeneration() {
        UUID first = UUID.fromString("11111111-1111-1111-1111-111111111111");
        UUID second = UUID.fromString("22222222-2222-2222-2222-222222222222");
        UuidGeneratorAdapter<TestId> adapter = new UuidGeneratorAdapter<>(
                new IncrementalUuidSupplier(first, second),
                TestId::new
        );

        assertEquals(new TestId(first), adapter.generate());
        assertEquals(new TestId(second), adapter.generate());
    }

    @Test
    void rejectsNullUuidSupplier() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new UuidGeneratorAdapter<TestId>(null, TestId::new)
        );

        assertEquals("uuidSupplier must not be null", exception.getMessage());
    }

    @Test
    void rejectsNullIdentifierFactory() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new UuidGeneratorAdapter<TestId>(() -> UUID.randomUUID(), null)
        );

        assertEquals("identifierFactory must not be null", exception.getMessage());
    }

    private record TestId(UUID value) implements Identifier<UUID> {
    }

    private static final class IncrementalUuidSupplier implements java.util.function.Supplier<UUID> {
        private final UUID first;
        private final UUID second;
        private int calls;

        private IncrementalUuidSupplier(UUID first, UUID second) {
            this.first = first;
            this.second = second;
        }

        @Override
        public UUID get() {
            calls++;
            return calls == 1 ? first : second;
        }
    }
}
