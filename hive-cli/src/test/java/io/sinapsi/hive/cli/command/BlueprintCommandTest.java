package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.HiveCli;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlueprintCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void schemaJsonPrintsMachineReadableKinds() {
        CommandResult result = execute("blueprint", "schema", "--json");

        assertEquals(0, result.exitCode());
        assertTrue(result.stdout().contains("\"schemaVersion\": \"1\""));
        assertTrue(result.stdout().contains("\"aggregate\""));
        assertTrue(result.stdout().contains("\"required\""));
    }

    @Test
    void validateJsonReturnsNonZeroForInvalidBlueprint() throws Exception {
        Path blueprint = tempDir.resolve("order.yml");
        Files.writeString(blueprint, """
                version: 1

                types:
                  - kind: aggregate
                    name: Order
                """);

        CommandResult result = execute("blueprint", "validate", blueprint.toString(), "--json");

        assertEquals(1, result.exitCode());
        assertTrue(result.stdout().contains("\"valid\": false"));
        assertTrue(result.stdout().contains("\"BLUEPRINT_REQUIRED_FIELD_MISSING\""));
    }

    private CommandResult execute(String... args) {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        CommandLine commandLine = new CommandLine(new HiveCli());
        commandLine.setErr(new PrintWriter(new ByteArrayOutputStream()));
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            int exitCode = commandLine.execute(args);
            return new CommandResult(exitCode, stdout.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(originalOut);
        }
    }

    private record CommandResult(int exitCode, String stdout) {
    }
}
