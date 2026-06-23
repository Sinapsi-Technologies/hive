# Hexagonal Core

`hexagonal-core` contains the smallest framework-free contracts used by Hive Hexagonal Toolkit.

The module is intentionally boring: no Spring, no Jakarta, no persistence APIs, no validation runtime, and no technology-specific ports. It should be safe to use from domain and application code without pulling infrastructure concerns into the center of the system.

## Package

```text
io.sinapsi.hive.core
```

## Contracts

```text
command.Command
command.CommandFactory
domain.Identifier
domain.AggregateId
domain.AggregateRoot
event.DomainEvent
event.IntegrationEvent
mapper.Mapper
mapper.BiMapper
port.Port
port.InputPort
port.OutputPort
result.Result
result.Unit
usecase.UseCase
```

## Use Cases

`UseCase<I, O>` represents one application action.

```java
public interface CreateUserUseCase extends UseCase<CreateUserCommand, CreateUserResult> {
}
```

Input ports should normally expose one operation. A use case may be implemented directly, by a service class, or as a lambda when useful in tests.

## Commands

`Command` is a marker for immutable input models.

Commands may:

- hold primitive request data
- declare validation annotations in applications that use validation
- expose helper methods that convert primitives to domain value objects

Commands must not:

- validate themselves
- create aggregates
- contain business decisions
- depend on infrastructure

## Results

`Result` is a marker for use case output models.

Use `Unit.INSTANCE` when a use case has no meaningful return value.

## Ports

Core only defines port direction markers:

```text
Port
InputPort
OutputPort
```

Technical ports such as clocks, ID generators, transactions, event publishing, and file storage do not live in core. Use `hive-ports` for the optional shared versions, or define application-specific output ports near the use case that needs them.

## Domain Helpers

`Identifier<T>`, `AggregateId<T>`, and `AggregateRoot<ID>` are small optional helpers for domain models.

They are not a framework. If a project already has its own aggregate conventions, it can ignore them.

## Events

`DomainEvent` marks events produced inside a bounded context.

`IntegrationEvent` marks events intended to cross application or module boundaries.

The core does not prescribe an event bus, serialization format, transaction boundary, or delivery mechanism.

## Mappers

`Mapper<S, T>` and `BiMapper<A, B>` are simple transformation contracts.

Mapping is usually an application or adapter concern. Avoid putting mapper implementations in the domain model.

## Design Rules

- Core must remain framework-free.
- Use cases represent one action.
- Input ports should have one method.
- Commands are immutable input models.
- Commands may declare validation annotations, but must not validate themselves.
- Commands may expose helper methods converting primitives to domain value objects.
- Commands must not create aggregates.
- Business logic belongs in domain/use case code, not in commands.
- Output ports model capabilities, not technologies.
- Domain code must not depend on Spring, JPA, HTTP, persistence, messaging, or validation frameworks.
