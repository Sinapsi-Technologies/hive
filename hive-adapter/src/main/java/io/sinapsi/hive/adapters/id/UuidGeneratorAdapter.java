package io.sinapsi.hive.adapters.id;

import io.sinapsi.hive.core.domain.Identifier;
import io.sinapsi.hive.ports.IdGeneratorPort;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;

public final class UuidGeneratorAdapter<T extends Identifier<UUID>> implements IdGeneratorPort<T> {

    private final Supplier<UUID> uuidSupplier;
    private final Function<UUID, T> identifierFactory;

    public UuidGeneratorAdapter(Function<UUID, T> identifierFactory) {
        this(UUID::randomUUID, identifierFactory);
    }

    public UuidGeneratorAdapter(Supplier<UUID> uuidSupplier, Function<UUID, T> identifierFactory) {
        this.uuidSupplier = Objects.requireNonNull(uuidSupplier, "uuidSupplier must not be null");
        this.identifierFactory = Objects.requireNonNull(identifierFactory, "identifierFactory must not be null");
    }

    @Override
    public T generate() {
        return identifierFactory.apply(uuidSupplier.get());
    }
}
