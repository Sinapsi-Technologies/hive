# Hive Validator

`hive-validator` defines the framework-free validation contract for Hive commands and the base factory used to validate commands while they are created.

The intended flow is:

1. A request enters the application.
2. A command factory creates the command.
3. The factory validates the command.
4. The use case receives only a valid command.

Commands stay immutable and do not validate themselves.

## Dependency

```xml
<dependency>
  <groupId>io.sinapsi.hive</groupId>
  <artifactId>hive-validator</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

The module depends on `hive-core` and has no framework dependency.

## Core Types

```text
io.sinapsi.hive.validation.CommandValidator
io.sinapsi.hive.validation.ValidationProvider
io.sinapsi.hive.validation.ValidationProviders
io.sinapsi.hive.factory.AbstractCommandFactory
```

## Command Validator

```java
@FunctionalInterface
public interface CommandValidator<T extends Command> {
    void validate(T command);

    default CommandValidator<T> and(CommandValidator<T> other) {
        return command -> {
            validate(command);
            other.validate(command);
        };
    }

    static <T extends Command> CommandValidator<T> noop() {
        return command -> {};
    }
}
```

## Recommended Usage: Command Factory

Create immutable commands:

```java
import io.sinapsi.hive.core.command.Command;

public record CreateUserCommand(String name, String email) implements Command {
}
```

Define validation rules:

```java
import io.sinapsi.hive.validation.CommandValidator;

public final class CreateUserValidation {
    public static CommandValidator<CreateUserCommand> rules() {
        return nameIsRequired().and(emailIsRequired());
    }

    private static CommandValidator<CreateUserCommand> nameIsRequired() {
        return command -> {
            if (command.name() == null || command.name().isBlank()) {
                throw new IllegalArgumentException("name is required");
            }
        };
    }

    private static CommandValidator<CreateUserCommand> emailIsRequired() {
        return command -> {
            if (command.email() == null || command.email().isBlank()) {
                throw new IllegalArgumentException("email is required");
            }
        };
    }
}
```

Expose them through a `ValidationProvider`:

```java
import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.validation.CommandValidator;
import io.sinapsi.hive.validation.ValidationProvider;

public final class ApplicationValidationProvider implements ValidationProvider {
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Command> CommandValidator<C> commandValidator() {
        return (CommandValidator<C>) CreateUserValidation.rules();
    }
}
```

Expose the provider through Java `ServiceLoader` by adding this file:

```text
src/main/resources/META-INF/services/io.sinapsi.hive.validation.ValidationProvider
```

with this content:

```text
com.example.ApplicationValidationProvider
```

Use an `AbstractCommandFactory`. The factory reads the discovered provider through `ValidationProviders.get()`:

```java
import io.sinapsi.hive.factory.AbstractCommandFactory;

public final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
    public CreateUserCommand create(String name, String email) {
        return validate(new CreateUserCommand(name, email));
    }
}
```

Now command creation and validation happen together:

```java
var factory = new CreateUserCommandFactory();

CreateUserCommand command = factory.create("Ada", "ada@example.com");
```

If validation fails, the factory throws the exception raised by the validator.

## Validation Provider Lookup

`AbstractCommandFactory` uses the provider discovered by `ServiceLoader`:

```java
ValidationProviders.get().commandValidator();
```

If no provider is available on the classpath, Hive uses a no-op validator. This keeps the factory usable in tests and small applications before validation is configured.

Expose only one provider per application runtime. Multiple providers fail fast to avoid ambiguous validation behavior.

## Composition

Use `and(...)` to compose small validators:

```java
CommandValidator<CreateUserCommand> validator =
        CreateUserValidation.rules();
```

Composition is ordered. If the first validator throws, the second validator is not executed.

Use `noop()` as a neutral starting point:

```java
CommandValidator<CreateUserCommand> validator =
        CommandValidator.<CreateUserCommand>noop()
                .and(CreateUserValidation.rules());
```

## Design Rules

- Commands remain immutable input models.
- Commands must not validate themselves.
- Validation happens during command creation or before use case execution.
- Use cases should receive valid command objects.
- Validators should be small and specific.
- Framework-specific validation belongs in adapter modules, such as `jakarta-hive-validator`.
