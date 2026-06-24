package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.c4.C4TestProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import picocli.CommandLine;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class C4GenerateCommandTest {
    @TempDir
    Path tempDir;

    private record Run(int exitCode, String out, String err) {
    }

    private Run run(String... args) {
        C4GenerateCommand command = new C4GenerateCommand();
        command.startDir = tempDir;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        PrintStream originalErr = System.err;
        try {
            System.setOut(new PrintStream(out, true, StandardCharsets.UTF_8));
            System.setErr(new PrintStream(err, true, StandardCharsets.UTF_8));
            int code = new CommandLine(command).execute(args);
            return new Run(code, out.toString(StandardCharsets.UTF_8), err.toString(StandardCharsets.UTF_8));
        } finally {
            System.setOut(originalOut);
            System.setErr(originalErr);
        }
    }

    @Test
    void generatesPumlAndPrintsHumanOutput() throws IOException {
        C4TestProject.todoModule(tempDir);

        Run run = run("--module", "todo");

        assertEquals(0, run.exitCode());
        assertTrue(run.out().contains("Generated C4 architecture documentation:"), run.out());
        assertTrue(run.out().contains("- docs/architecture/c3-todo.puml"), run.out());
        assertTrue(Files.exists(tempDir.resolve("docs/architecture/c3-todo.puml")));
    }

    @Test
    void emitsMachineReadableJson() throws IOException {
        C4TestProject.todoModule(tempDir);

        Run run = run("--module", "todo", "--json");

        assertEquals(0, run.exitCode());
        String json = run.out();
        assertTrue(json.contains("\"command\": \"c4 generate\""), json);
        assertTrue(json.contains("\"level\": \"component\""), json);
        assertTrue(json.contains("\"module\": \"todo\""), json);
        assertTrue(json.contains("\"created\""), json);
        assertTrue(json.contains("docs/architecture/c3-todo.puml"), json);
        assertTrue(json.contains("\"type\": \"INBOUND_ADAPTER\""), json);
        assertTrue(json.contains("\"source\": \"todoController\""), json);
        assertTrue(json.contains("\"description\": \"calls\""), json);
    }

    @Test
    void failsWhenNoHiveProject() {
        Run run = run("--module", "todo", "--json");

        assertEquals(2, run.exitCode());
        assertTrue(run.out().contains("\"ok\": false"), run.out());
        assertTrue(run.out().contains("No .hive-project found"), run.out());
    }

    @Test
    void rejectsUnsupportedFormat() throws IOException {
        C4TestProject.todoModule(tempDir);

        Run run = run("--module", "todo", "--format", "mermaid");

        assertEquals(2, run.exitCode());
        assertTrue(run.err().contains("Unsupported format"), run.err());
    }

    @Test
    void reportsSkippedFilesInJsonWithoutForce() throws IOException {
        C4TestProject.todoModule(tempDir);
        run("--module", "todo");

        Run second = run("--module", "todo", "--json");

        assertEquals(0, second.exitCode());
        assertTrue(second.out().contains("\"skipped\""), second.out());
        assertTrue(second.out().contains("c3-todo.puml"), second.out());
        assertTrue(second.out().contains("already exists"), second.out());
    }

    @Test
    void forceOverwritesExistingFiles() throws IOException {
        C4TestProject.todoModule(tempDir);
        run("--module", "todo");

        Run forced = run("--module", "todo", "--force", "--json");

        assertEquals(0, forced.exitCode());
        assertTrue(forced.out().contains("docs/architecture/c3-todo.puml"), forced.out());
        // skipped array should be empty when forcing.
        assertTrue(forced.out().contains("\"skipped\": []"), forced.out());
    }
}
