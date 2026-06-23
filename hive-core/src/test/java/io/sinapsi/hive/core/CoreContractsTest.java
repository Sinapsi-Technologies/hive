package io.sinapsi.hive.core;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.core.event.IntegrationEvent;
import io.sinapsi.hive.core.mapper.BiMapper;
import io.sinapsi.hive.core.mapper.Mapper;
import io.sinapsi.hive.core.port.InputPort;
import io.sinapsi.hive.core.port.OutputPort;
import io.sinapsi.hive.core.port.Port;
import io.sinapsi.hive.core.result.Result;
import io.sinapsi.hive.core.result.Unit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;

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
        Result result = Unit.INSTANCE;
        DomainEvent domainEvent = new TestDomainEvent();
        IntegrationEvent integrationEvent = new TestIntegrationEvent();
        Port port = new TestOutputPort();

        assertInstanceOf(Command.class, command);
        assertSame(Unit.INSTANCE, result);
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

}
