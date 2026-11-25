package io.sinapsi.hive.mcp.dependencies;

import io.sinapsi.hive.mcp.security.OsvScanResult;
import io.sinapsi.hive.mcp.security.OsvScanner;
import io.sinapsi.hive.mcp.security.OsvScannerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public final class MavenAndOsvDependencyVerifier implements DependencyChangeVerifier {

    private final OsvScanner scanner;
    private final OsvScannerProperties properties;

    public MavenAndOsvDependencyVerifier(OsvScanner scanner) {
        this(scanner, new OsvScannerProperties());
    }

    @Autowired
    public MavenAndOsvDependencyVerifier(OsvScanner scanner, OsvScannerProperties properties) {
        this.scanner = scanner;
        this.properties = properties;
    }

    @Override
    public VerificationResult verify(Path projectRoot) throws IOException, InterruptedException {
        Process process = new ProcessBuilder(List.of("mvn", "-q", "test"))
                .directory(projectRoot.toFile())
                .redirectErrorStream(true)
                .start();
        boolean finished = process.waitFor(180, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return new VerificationResult("TIMEOUT", "NOT_RUN", false, "Maven test timed out");
        }
        if (process.exitValue() != 0) {
            return new VerificationResult("FAIL", "NOT_RUN", false, "Maven test failed");
        }

        OsvScanResult osv = scanner.scan(projectRoot);
        boolean securityPassed = osv.status() == OsvScanResult.Status.PASS
                || osv.status() == OsvScanResult.Status.DISABLED
                || (osv.status() == OsvScanResult.Status.UNAVAILABLE && !properties.isFailOnUnavailable());
        String verificationError = osv.status() == OsvScanResult.Status.UNAVAILABLE
                ? "OSV-Scanner unavailable; dependency security was not verified"
                : securityPassed ? "" : "OSV-Scanner reported a failure";
        return new VerificationResult(
                "PASS",
                osv.status().name(),
                securityPassed,
                verificationError);
    }
}
