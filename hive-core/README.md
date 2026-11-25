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
public interface CreateUserUseCase extends UseCase<CreateUserCommand, Result<CreateUserView>> {
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

`Result<T>` is a sealed operation outcome implemented only by `Success<T>` and `Failure<T>`.
Output models are plain records or classes; they do not implement `Result`.
Hive names application output records `*View` and places them in
`application.ports.in.views`, with a static `of(...)` factory. Input contracts must
not depend on service implementations.

```java
// application.ports.in.views
public record UserView(String id, String name) {
    public static UserView of(String id, String name) {
        return new UserView(id, name);
    }
}

public interface FindUserUseCase extends UseCase<FindUserCommand, Result<UserView>> {}
```

Return `Result.success(output)` for a successful operation or
`Result.failure(errorCode, message)` for an expected application error.
`ErrorCode` is open so each bounded context can define its own error catalog.
Adapters translate errors to transport-specific responses, such as HTTP status codes.

Failures contain an immutable `ResultError` with an `ErrorCode`, message, and
`List<ErrorDetail>`. `ErrorDetail(field, message)` describes field or object-level
errors; an empty field path identifies the whole object.
`Result.failure(code, message)` uses an empty detail list; the overload
`Result.failure(code, message, details)` retains multiple details.

Both outcomes support `isSuccess`, `map`, `flatMap`, and `fold`.
Transformations apply only to success values; failures preserve their complete error.
`fold(onSuccess, onFailure)` passes a single `ResultError` to the failure function:

```java
String description = result.fold(
        value -> "Success: " + value,
        error -> error.code().value() + ": " + error.message()
);
```

`CommonErrorCode.VALIDATION_FAILED` is an optional shared validation code.
Other business codes remain application-specific.
Success values must be non-null. Use `Result.success(Unit.INSTANCE)` when a use case
has no meaningful return value.

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
