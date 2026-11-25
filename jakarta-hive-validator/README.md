# Jakarta Hive Validator

`jakarta-hive-validator` connects Jakarta Bean Validation to Hive command validation.

You do not instantiate `JakartaValidationProvider` yourself.
Hive discovers it through Java SPI when `ValidationProviders.get()` is called.

The Jakarta implementations live in `io.sinapsi.hive.infrastructure.validation`.
The framework-free contracts remain in `io.sinapsi.hive.validation`.
Application factories depend on those contracts; commands may declare Jakarta
constraints, while provider wiring belongs to infrastructure.

## Dependency

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>jakarta-hive-validator</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The module depends on `hive-validator` and `jakarta.validation-api`.
At runtime you also need a Jakarta Validation implementation, for example:

```xml
<dependency>
  <groupId>org.hibernate.validator</groupId>
  <artifactId>hibernate-validator</artifactId>
  <version>8.0.1.Final</version>
</dependency>

<dependency>
  <groupId>org.glassfish</groupId>
  <artifactId>jakarta.el</artifactId>
  <version>4.0.2</version>
</dependency>
```

## How Provider Discovery Works

`jakarta-hive-validator` ships this SPI descriptor:

```text
META-INF/services/io.sinapsi.hive.validation.ValidationProvider
```

Its content points to:

```text
io.sinapsi.hive.infrastructure.validation.JakartaValidationProvider
```

When application code uses an `AbstractCommandFactory`, Hive calls:

```java
ValidationProviders.get().commandValidator();
```

`ValidationProviders` uses `ServiceLoader` to instantiate the available `ValidationProvider`.
If `jakarta-hive-validator` is on the classpath, the discovered provider is `JakartaValidationProvider`.

## Command Example

Commands stay immutable and do not validate themselves.
They only declare validation constraints:

```java
import io.sinapsi.hive.core.command.Command;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserCommand(
        @NotBlank String name,
        @Email String email
) implements Command {
}
```

## Factory Example

Create commands through an `AbstractCommandFactory`.
The factory validates through `ValidationProviders.get()` internally:

```java
import io.sinapsi.hive.factory.AbstractCommandFactory;
import io.sinapsi.hive.core.result.Result;

public final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
    public Result<CreateUserCommand> create(String name, String email) {
        return validate(new CreateUserCommand(name, email));
    }
}
```

Usage:

```java
var factory = new CreateUserCommandFactory();

Result<CreateUserCommand> command = factory.create("Ada", "ada@example.com");
```

Invalid commands return a structured `Failure` from the factory:

```java
factory.create("", "not-an-email").fold(
        command -> command,
        error -> {
            error.details().forEach(detail ->
                    System.out.println(detail.field() + ": " + detail.message()));
            return null;
        }
);
```

`JakartaCommandValidator.validate(...)` translates all Jakarta violations into Hive's
`CommandValidationException`. The factory converts that exception to
`Result.failure(CommonErrorCode.VALIDATION_FAILED, message, details)`.
Field paths and messages are retained, ordered by path and message, without including
rejected values or Jakarta types. `map`, `flatMap`, and `fold` preserve every detail.
Unexpected provider/configuration failures continue to propagate as exceptions.

## Optional Provider Check

Application code usually does not need this, but tests can assert that SPI discovery is active:

```java
import io.sinapsi.hive.infrastructure.validation.JakartaValidationProvider;
import io.sinapsi.hive.validation.ValidationProviders;

assert ValidationProviders.get() instanceof JakartaValidationProvider;
```

## Direct Validator Usage

Direct usage is available for tests or advanced composition, but the factory flow is preferred:

```java
var validator = JakartaCommandValidator.<CreateUserCommand>create();

validator.validate(new CreateUserCommand("Ada", "ada@example.com"));
```

## Composition

`JakartaCommandValidator<T>` implements `CommandValidator<T>`, so it can be composed with application-specific rules:

```java
import io.sinapsi.hive.validation.CommandValidator;
import io.sinapsi.hive.infrastructure.validation.JakartaCommandValidator;

CommandValidator<CreateUserCommand> validator =
        JakartaCommandValidator.<CreateUserCommand>create()
                .and(command -> {
                    if (command.email().endsWith("@blocked.test")) {
                        throw new IllegalArgumentException("email domain is blocked");
                    }
                });
```

Validation is ordered. If Jakarta constraints fail, the next validator is not executed.

## Design Rules

- Put constraints on commands, not validation logic.
- Commands must not validate themselves.
- Validation should happen while commands are created.
- Use cases should receive valid commands.
- Domain logic should not depend on Jakarta Validation.
- `jakarta-hive-validator` is an adapter for Hive's `CommandValidator` contract.
