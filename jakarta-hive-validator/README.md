# Jakarta Hive Validator

`jakarta-hive-validator` connects Jakarta Bean Validation to Hive command validation.

You do not instantiate `JakartaValidationProvider` yourself.
Hive discovers it through Java SPI when `ValidationProviders.get()` is called.

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
io.sinapsi.hive.validation.JakartaValidationProvider
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

public final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
    public CreateUserCommand create(String name, String email) {
        return validate(new CreateUserCommand(name, email));
    }
}
```

Usage:

```java
var factory = new CreateUserCommandFactory();

CreateUserCommand command = factory.create("Ada", "ada@example.com");
```

Invalid commands throw `ConstraintViolationException`:

```java
import jakarta.validation.ConstraintViolationException;

try {
    factory.create("", "not-an-email");
} catch (ConstraintViolationException exception) {
    exception.getConstraintViolations().forEach(violation ->
            System.out.println(violation.getPropertyPath() + ": " + violation.getMessage())
    );
}
```

Example output:

```text
name: must not be blank
email: must be a well-formed email address
```

## Optional Provider Check

Application code usually does not need this, but tests can assert that SPI discovery is active:

```java
import io.sinapsi.hive.validation.JakartaValidationProvider;
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
import io.sinapsi.hive.validation.JakartaCommandValidator;

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
