package io.sinapsi.hive.mcp.dependencies;

import java.io.IOException;
import java.nio.file.Path;

@FunctionalInterface
public interface DependencyChangeVerifier {

    VerificationResult verify(Path projectRoot) throws IOException, InterruptedException;

    record VerificationResult(String build, String security, boolean passed, String error) {

        public static VerificationResult skipped() {
            return new VerificationResult("NOT_RUN", "NOT_RUN", true, "");
        }
    }
}
