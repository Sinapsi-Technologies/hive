# Hive Adapter

`hive-adapter` provides lightweight default implementations for the technical ports in `hive-port`.

Use this module when you want practical adapters for tests, demos, local development, or small applications without bringing in Spring, Jakarta, messaging, database, or cloud SDK dependencies.

Application code should depend on the port interfaces. Composition code can wire these adapters.

## Dependency

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-adapter</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Adapters

```text
ClockPort         -> SystemClockAdapter
IdGeneratorPort   -> UuidGeneratorAdapter
PublishEventPort  -> InMemoryEventPublisherAdapter
FileStoragePort   -> LocalFileStorageAdapter
```

## SystemClockAdapter

`SystemClockAdapter` implements `ClockPort`.
By default it uses `Clock.systemUTC()`.

```java
import io.sinapsi.hive.adapters.clock.SystemClockAdapter;
import io.sinapsi.hive.ports.ClockPort;

ClockPort clock = new SystemClockAdapter();

var now = clock.now();
```

For deterministic tests, pass a fixed Java `Clock`:

```java
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

ClockPort clock = new SystemClockAdapter(
        Clock.fixed(Instant.parse("2026-05-14T10:15:30Z"), ZoneOffset.UTC)
);
```

## UuidGeneratorAdapter

`UuidGeneratorAdapter` implements `IdGeneratorPort<T>` for identifiers backed by `UUID`.
It receives a factory function so your domain keeps its own ID type.

```java
import io.sinapsi.hive.adapters.id.UuidGeneratorAdapter;
import io.sinapsi.hive.core.domain.Identifier;
import io.sinapsi.hive.ports.IdGeneratorPort;

import java.util.UUID;

public record UserId(UUID value) implements Identifier<UUID> {
}

IdGeneratorPort<UserId> ids = new UuidGeneratorAdapter<>(UserId::new);

UserId id = ids.generate();
```

Tests can inject a deterministic UUID supplier:

```java
var ids = new UuidGeneratorAdapter<>(
        () -> UUID.fromString("11111111-1111-1111-1111-111111111111"),
        UserId::new
);
```

## InMemoryEventPublisherAdapter

`InMemoryEventPublisherAdapter` implements `PublishEventPort<E extends Event>`.
It stores published events in memory and exposes immutable snapshots.

```java
import io.sinapsi.hive.adapters.event.InMemoryEventPublisherAdapter;
import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.ports.PublishEventPort;

public record UserCreatedEvent(String userId) implements Event {
}

InMemoryEventPublisherAdapter<UserCreatedEvent> adapter =
        new InMemoryEventPublisherAdapter<>();

PublishEventPort<UserCreatedEvent> events = adapter;

events.publish(new UserCreatedEvent("user-1"));

var published = adapter.publishedEvents();
```

This adapter is not a message broker. Use it for simple in-process flows and tests.

## LocalFileStorageAdapter

`LocalFileStorageAdapter` implements `FileStoragePort`.
It stores byte arrays under a configured root directory.

```java
import io.sinapsi.hive.adapters.file.LocalFileStorageAdapter;
import io.sinapsi.hive.ports.FileStoragePort;

import java.nio.file.Path;

FileStoragePort files = new LocalFileStorageAdapter(Path.of("storage"));

files.save("avatars/user-1.png", content);

byte[] loaded = files.load("avatars/user-1.png");
```

The adapter creates parent directories when saving.
Paths are normalized and must remain inside the configured root.
For example, `../secret.txt` is rejected.

## When To Use

- Use `hive-port` in application services.
- Use `hive-adapter` in tests, examples, and simple composition roots.
- Replace these adapters with infrastructure-specific ones when you need real messaging, object storage, database transactions, or framework integration.

## Design Rules

- Adapters implement ports.
- Adapters stay outside domain and use case logic.
- These implementations are intentionally small and dependency-light.
- They are defaults, not a full infrastructure platform.
