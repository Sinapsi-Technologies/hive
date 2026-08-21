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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreateUseCaseCommandTest {
    @TempDir
    Path tempDir;

    @Test
    void jsonCreatedPathsExcludeNestedCommandArtifacts() throws Exception {
        new FileScaffolder().init(tempDir, HiveConfig.defaults(), false, false);

        CreateUseCaseCommand command = new CreateUseCaseCommand();
        CommandLine commandLine = new CommandLine(command);
        commandLine.setErr(new PrintWriter(new ByteArrayOutputStream()));
        commandLine.parseArgs(
                "CancelEvent",
                "--field", "organizerId:String",
                "--field", "eventId:String",
                "--factory",
                "--json"
        );

        PrintStream originalOut = System.out;
        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(stdout, true, StandardCharsets.UTF_8));
            assertEquals(0, command.call(tempDir));
        } finally {
            System.setOut(originalOut);
        }

        String json = stdout.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"src/main/java/com/example/app/application/ports/in/CancelEventUseCase.java\""), json);
        assertTrue(json.contains("\"src/main/java/com/example/app/application/services/CancelEventService.java\""), json);
        assertFalse(json.contains("CancelEventCommand.java"), json);
        assertFalse(json.contains("CancelEventCommandFactory.java"), json);
    }
}
