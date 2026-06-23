# Hive Port

`hive-port` contains optional technical output port contracts for Hive applications.

The module is framework-free. It defines capabilities that application code can depend on without choosing an infrastructure technology.

Implementations live outside this module. Use `hive-adapter` for lightweight defaults, or create application-specific adapters in your own infrastructure layer.

## Dependency

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-port</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

## Ports

```text
ClockPort
IdGeneratorPort<T extends Identifier<?>>
PublishEventPort<E extends Event>
FileStoragePort
```

All ports extend `OutputPort` from `hive-core`.

## ClockPort

Use `ClockPort` when application logic needs the current time.

```java
import io.sinapsi.hive.ports.ClockPort;

import java.time.Instant;

public final class CreateInvoiceService {
    private final ClockPort clock;

    public CreateInvoiceService(ClockPort clock) {
        this.clock = clock;
    }

    public Instant createdAt() {
        return clock.now();
    }
}
```

## IdGeneratorPort

Use `IdGeneratorPort` when a use case needs to create domain identifiers without knowing how IDs are generated.

```java
import io.sinapsi.hive.core.domain.Identifier;
import io.sinapsi.hive.ports.IdGeneratorPort;

public record UserId(String value) implements Identifier<String> {
}

public final class CreateUserService {
    private final IdGeneratorPort<UserId> ids;

    public CreateUserService(IdGeneratorPort<UserId> ids) {
        this.ids = ids;
    }

    public UserId nextUserId() {
        return ids.generate();
    }
}
```

## PublishEventPort

Use `PublishEventPort` to publish events without coupling the application layer to a broker, framework, or in-process dispatcher.

```java
import io.sinapsi.hive.core.event.Event;
import io.sinapsi.hive.ports.PublishEventPort;

public record UserCreatedEvent(String userId) implements Event {
}

public final class CreateUserService {
    private final PublishEventPort<UserCreatedEvent> events;

    public CreateUserService(PublishEventPort<UserCreatedEvent> events) {
        this.events = events;
    }

    public void publishCreated(UserId userId) {
        events.publish(new UserCreatedEvent(userId.value()));
    }
}
```

## FileStoragePort

Use `FileStoragePort` for simple byte-based storage capabilities.

```java
import io.sinapsi.hive.ports.FileStoragePort;

public final class StoreAvatarService {
    private final FileStoragePort files;

    public StoreAvatarService(FileStoragePort files) {
        this.files = files;
    }

    public void saveAvatar(String userId, byte[] content) {
        files.save("avatars/" + userId, content);
    }
}
```

## Design Rules

- Ports model capabilities, not technologies.
- Application services depend on these interfaces, not adapter implementations.
- Adapters implement these ports in infrastructure modules.
- Keep domain logic independent from `hive-port` unless the capability is truly part of the use case boundary.
- Prefer use-case-specific output ports when a shared technical port would be too generic.
