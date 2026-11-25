package io.sinapsi.hive.mcp.dependencies.infrastructure;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

@FunctionalInterface
public interface MavenDependencyProcessRunner {

    ProcessResult run(List<String> command, Path workingDirectory, Duration timeout)
            throws IOException, InterruptedException;

    record ProcessResult(int exitCode, String output, boolean timedOut) {}
}
