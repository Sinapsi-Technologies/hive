package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FileScaffolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateAdapterCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void jsonCreatedPathIncludesGroupSubpackage() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        FileScaffolder scaffolder = new FileScaffolder();
        scaffolder.init(tempDir, config, false, false);
        scaffolder.createPort(tempDir, config, null, "Payment", false);

        CreateAdapterCommand command = new CreateAdapterCommand();
        CommandLine commandLine = new CommandLine(command);
        commandLine.setErr(new PrintWriter(new ByteArrayOutputStream()));
        commandLine.parseArgs("Payment", "--group", "payment", "--port", "PaymentPort", "--json");

        PrintStream originalOut = System.out;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            assertEquals(0, command.call(tempDir));
        } finally {
            System.setOut(originalOut);
        }

        String json = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"command\": \"create adapter\""), json);
        assertTrue(json.contains(
                "\"src/main/java/com/example/app/infrastructure/adapters/out/payment/PaymentAdapter.java\""), json);
    }

    @Test
    void invalidGroupIsRejectedThroughCommandOption() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        FileScaffolder scaffolder = new FileScaffolder();
        scaffolder.init(tempDir, config, false, false);
        scaffolder.createPort(tempDir, config, null, "Payment", false);

        CreateAdapterCommand command = new CreateAdapterCommand();
        CommandLine commandLine = new CommandLine(command);
        commandLine.setErr(new PrintWriter(new ByteArrayOutputStream()));
        commandLine.parseArgs("Payment", "--group", "../payment", "--port", "PaymentPort");

        assertThrows(IllegalArgumentException.class, () -> command.call(tempDir));
    }
}
