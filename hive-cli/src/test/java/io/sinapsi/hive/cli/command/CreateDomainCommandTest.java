package io.sinapsi.hive.cli.command;

import org.junit.jupiter.api.Test;
import picocli.CommandLine;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CreateDomainCommandTest {
    @Test
    void createCommandRegistersNewDomainPrimitiveSubcommands() {
        CommandLine commandLine = new CommandLine(new CreateCommand());

        assertEquals(Set.of(
                "usecase",
                "port",
                "adapter",
                "module",
                "archtest",
                "vo",
                "id",
                "entity",
                "aggregate",
                "enum",
                "event",
                "exception",
                "domainservice",
                "snapshot",
                "record",
                "class",
                "command"
        ), commandLine.getSubcommands().keySet());
    }
}
