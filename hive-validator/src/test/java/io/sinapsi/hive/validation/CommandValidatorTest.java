package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.factory.AbstractCommandFactory;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommandValidatorTest {

    @Test
    void validatesCommandWhenRulePasses() {
        CommandValidator<CreateUserCommand> validator = CreateUserValidation.rules();

        assertDoesNotThrow(() -> validator.validate(new CreateUserCommand("Ada", "ada@example.com")));
    }

    @Test
    void throwsApplicationExceptionWhenRuleFails() {
        CommandValidator<CreateUserCommand> validator = CreateUserValidation.rules();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> validator.validate(new CreateUserCommand("", "ada@example.com"))
        );

        assertEquals("name is required", exception.getMessage());
    }

    @Test
    void composesValidatorsInOrder() {
        List<String> calls = new ArrayList<>();
        CommandValidator<CreateUserCommand> first = command -> calls.add("name:" + command.name());
        CommandValidator<CreateUserCommand> second = command -> calls.add("email:" + command.email());

        first.and(second).validate(new CreateUserCommand("Ada", "ada@example.com"));

        assertEquals(List.of("name:Ada", "email:ada@example.com"), calls);
    }

    @Test
    void compositionStopsAtFirstFailure() {
        List<String> calls = new ArrayList<>();
        CommandValidator<CreateUserCommand> first = command -> {
            calls.add("first");
            throw new IllegalStateException("invalid command");
        };
        CommandValidator<CreateUserCommand> second = command -> calls.add("second");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> first.and(second).validate(new CreateUserCommand("Ada", "ada@example.com"))
        );

        assertEquals("invalid command", exception.getMessage());
        assertEquals(List.of("first"), calls);
    }

    @Test
    void noopValidatorAcceptsAnyCommand() {
        CommandValidator<CreateUserCommand> validator = CommandValidator.noop();

        assertDoesNotThrow(() -> validator.validate(new CreateUserCommand("", "")));
    }

    @Test
    void validationProvidersUsesServiceLoaderProvider() {
        ValidationProvider provider = ValidationProviders.get();

        assertInstanceOf(TestValidationProvider.class, provider);
    }

    @Test
    void abstractCommandFactoryValidatesCreatedCommandThroughValidationProvidersGet() {
        CreateUserCommandFactory factory = new CreateUserCommandFactory();

        CreateUserCommand command = factory.create("Ada", "ada@example.com");

        assertEquals(new CreateUserCommand("Ada", "ada@example.com"), command);
    }

    @Test
    void abstractCommandFactoryRejectsInvalidCreatedCommandThroughValidationProvidersGet() {
        CreateUserCommandFactory factory = new CreateUserCommandFactory();

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> factory.create("", "ada@example.com")
        );

        assertEquals("name is required", exception.getMessage());
    }

    record CreateUserCommand(String name, String email) implements Command {
    }

    private static final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
        CreateUserCommand create(String name, String email) {
            return validate(new CreateUserCommand(name, email));
        }
    }

    static final class CreateUserValidation {
        private CreateUserValidation() {
        }

        static CommandValidator<CreateUserCommand> rules() {
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
}
