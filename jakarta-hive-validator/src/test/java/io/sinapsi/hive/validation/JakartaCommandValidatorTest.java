package io.sinapsi.hive.validation;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.factory.AbstractCommandFactory;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JakartaCommandValidatorTest {

    @Test
    void acceptsCommandThatSatisfiesJakartaConstraints() {
        JakartaCommandValidator<CreateUserCommand> validator = JakartaCommandValidator.create();

        assertDoesNotThrow(() -> validator.validate(new CreateUserCommand("Ada", "ada@example.com")));
    }

    @Test
    void rejectsCommandWithSingleConstraintViolation() {
        JakartaCommandValidator<CreateUserCommand> validator = JakartaCommandValidator.create();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(new CreateUserCommand("", "ada@example.com"))
        );

        assertEquals(Set.of("name"), violatedProperties(exception));
    }

    @Test
    void rejectsCommandWithMultipleConstraintViolations() {
        JakartaCommandValidator<CreateUserCommand> validator = JakartaCommandValidator.create();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(new CreateUserCommand("", "not-an-email"))
        );

        assertEquals(Set.of("name", "email"), violatedProperties(exception));
    }

    @Test
    void supportsDifferentCommandTypes() {
        JakartaCommandValidator<CreateProjectCommand> validator = JakartaCommandValidator.create();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> validator.validate(new CreateProjectCommand("", "x"))
        );

        assertEquals(Set.of("name", "code"), violatedProperties(exception));
    }

    @Test
    void canBeComposedWithApplicationValidator() {
        CommandValidator<CreateUserCommand> jakartaValidation = JakartaCommandValidator.create();
        CommandValidator<CreateUserCommand> blockedDomainValidation = command -> {
            if (command.email().endsWith("@blocked.test")) {
                throw new IllegalArgumentException("email domain is blocked");
            }
        };

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> jakartaValidation
                        .and(blockedDomainValidation)
                        .validate(new CreateUserCommand("Ada", "ada@blocked.test"))
        );

        assertEquals("email domain is blocked", exception.getMessage());
    }

    @Test
    void validationProvidersDiscoversJakartaProviderWithServiceLoader() {
        ValidationProvider provider = ValidationProviders.get();

        assertInstanceOf(JakartaValidationProvider.class, provider);
    }

    @Test
    void abstractCommandFactoryValidatesCommandThroughValidationProvidersGet() {
        CreateUserCommandFactory factory = new CreateUserCommandFactory();

        CreateUserCommand command = factory.create("Ada", "ada@example.com");

        assertEquals(new CreateUserCommand("Ada", "ada@example.com"), command);
    }

    @Test
    void abstractCommandFactoryRejectsInvalidCommandThroughValidationProvidersGet() {
        CreateUserCommandFactory factory = new CreateUserCommandFactory();

        ConstraintViolationException exception = assertThrows(
                ConstraintViolationException.class,
                () -> factory.create("", "not-an-email")
        );

        assertEquals(Set.of("name", "email"), violatedProperties(exception));
    }

    private Set<String> violatedProperties(ConstraintViolationException exception) {
        return exception.getConstraintViolations().stream()
                .map(violation -> violation.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    private record CreateUserCommand(
            @NotBlank String name,
            @Email String email
    ) implements Command {
    }

    private static final class CreateUserCommandFactory extends AbstractCommandFactory<CreateUserCommand> {
        CreateUserCommand create(String name, String email) {
            return validate(new CreateUserCommand(name, email));
        }
    }

    private record CreateProjectCommand(
            @NotBlank String name,
            @Size(min = 3) String code
    ) implements Command {
    }
}
