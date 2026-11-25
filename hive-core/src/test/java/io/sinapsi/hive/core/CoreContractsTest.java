package io.sinapsi.hive.core;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.core.event.IntegrationEvent;
import io.sinapsi.hive.core.mapper.BiMapper;
import io.sinapsi.hive.core.mapper.Mapper;
import io.sinapsi.hive.core.port.InputPort;
import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.core.port.Port;
import io.sinapsi.hive.core.result.ErrorCode;
import io.sinapsi.hive.core.result.ErrorDetail;
import io.sinapsi.hive.core.result.ResultError;
import java.util.ArrayList;
import java.util.List;
import io.sinapsi.hive.core.result.Failure;
import io.sinapsi.hive.core.result.Result;
import io.sinapsi.hive.core.result.Success;
import io.sinapsi.hive.core.result.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoreContractsTest {

    @Test
    void portMarkersDefineDirectionOnly() {
        InputPort inputPort = new TestInputPort();
        OutputPort outputPort = new TestOutputPort();

        assertInstanceOf(Port.class, inputPort);
        assertInstanceOf(Port.class, outputPort);
    }

    @Test
    void markerInterfacesStayFrameworkFree() {
        Command command = new TestCommand();
        Success<Unit> result = Result.success(Unit.INSTANCE);
        DomainEvent domainEvent = new TestDomainEvent();
        IntegrationEvent integrationEvent = new TestIntegrationEvent();
        Port port = new TestOutputPort();

        assertInstanceOf(Command.class, command);
        assertEquals(Unit.INSTANCE, result.value());
        assertInstanceOf(DomainEvent.class, domainEvent);
        assertInstanceOf(IntegrationEvent.class, integrationEvent);
        assertInstanceOf(OutputPort.class, port);
    }

    @Test
    void mappersExposeSimpleTransformContracts() {
        Mapper<String, Integer> length = String::length;
        BiMapper<String, Integer> mapper = new BiMapper<>() {
            @Override
            public Integer toTarget(String source) {
                return source.length();
            }

            @Override
            public String toSource(Integer target) {
                return "*".repeat(target);
            }
        };

        assertEquals(4, length.map("hive"));
        assertEquals(4, mapper.toTarget("hive"));
        assertEquals("***", mapper.toSource(3));
    }

    @Test
    void resultsExposeSuccessAndFailureFactories() {
        Success<String> success = Result.success("created");
        Failure<String> failure = Result.failure(new TestErrorCode("USER_NOT_FOUND"), "User not found");

        assertEquals("created", success.value());
        assertEquals("USER_NOT_FOUND", failure.code().value());
        assertEquals("User not found", failure.message());
    }

    @Test
    void successCanBeMappedAndFlatMapped() {
        Result<Integer> result = Result.<String>success("hive")
                .map(String::length)
                .flatMap(length -> Result.success(length * 2));

        assertTrue(result.isSuccess());
        assertEquals(8, ((Success<Integer>) result).value());
    }

    @Test
    void failureSkipsTransformationsAndIsFolded() {
        Result<Integer> result = Result.<String>failure(new TestErrorCode("NOT_FOUND"), "Missing")
                .map(String::toUpperCase)
                .flatMap(value -> Result.success(value.length()));

        assertFalse(result.isSuccess());
        assertEquals("NOT_FOUND:Missing", result.fold(
                value -> "success:" + value,
                error -> error.code().value() + ":" + error.message()
        ));
    }

    @Test
    void successCanBeFoldedToAnOutput() {
        Result<String> result = Result.success("hive");

        assertEquals("HIVE", result.fold(String::toUpperCase, error -> {
            throw new AssertionError("A success must not invoke the failure branch");
        }));
    }

    @Test
    void failureNeverInvokesSuccessTransformations() {
        Result<String> failure = Result.failure(new TestErrorCode("NOT_FOUND"), "Missing");

        Result<Integer> transformed = failure.<String>map(value -> {
            throw new AssertionError("A failure must not invoke map");
        }).flatMap(value -> {
            throw new AssertionError("A failure must not invoke flatMap");
        });

        assertEquals("NOT_FOUND:Missing", transformed.fold(value -> {
            throw new AssertionError("A failure must not invoke the success branch");
        }, error -> error.code().value() + ":" + error.message()));
    }

    @Test
    void failurePreservesAllDetailsAcrossTransformationsAndFold() {
        var details = new ArrayList<>(List.of(
                new ErrorDetail("email", "Invalid email"),
                new ErrorDetail("name", "Name required")));
        var error = new ResultError(new TestErrorCode("VALIDATION_FAILED"), "Invalid data", details);
        details.clear();
        Result<Integer> result = Result.<String>failure(error)
                .map(String::length)
                .flatMap(value -> Result.success(value * 2));

        var folded = result.fold(value -> { throw new AssertionError("Unexpected success"); }, failure -> failure);
        assertSame(error, folded);
        assertEquals(2, folded.details().size());
        assertThrows(UnsupportedOperationException.class, () -> folded.details().clear());
        assertEquals(List.of(), Result.failure(new TestErrorCode("NOT_FOUND"), "Missing").details());
    }

    private record TestCommand() implements Command {
    }

    private record TestDomainEvent() implements DomainEvent {
    }

    private record TestIntegrationEvent() implements IntegrationEvent {
    }

    private static final class TestInputPort implements InputPort {
    }

    private static final class TestOutputPort implements OutputPort {
    }

    private record TestErrorCode(String value) implements ErrorCode {
    }

}
