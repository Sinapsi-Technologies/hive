package io.sinapsi.hive.mcp.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OsvScannerTest {

    @Test
    void executesJsonRecursiveSourceScan(@TempDir Path project) throws Exception {
        OsvScannerProperties properties = new OsvScannerProperties();
        properties.setEnabled(true);
        properties.setExecutable("osv-scanner-test");
        properties.setTimeoutMillis(1000);
        OsvScanner scanner = new OsvScanner((command, root, timeout) -> {
            assertEquals(List.of("osv-scanner-test", "scan", "source", "--format", "json", "--recursive", root.toString()), command);
            assertEquals(Duration.ofMillis(1000), timeout);
            return new OsvProcessRunner.ProcessResult(0, """
                    {"results":[{"source":{"path":"pom.xml"},"packages":[{"package":{"name":"org.example:demo","version":"1.0.0","ecosystem":"Maven"},"vulnerabilities":[{"id":"CVE-2026-0001","database_specific":{"severity":"HIGH"},"affected":[{"ranges":[{"events":[{"introduced":"0"},{"fixed":"1.0.1"}]}]}]}]}]}]}
                    """, false);
        }, properties);

        OsvScanResult result = scanner.scan(project);
        assertEquals(OsvScanResult.Status.FAIL, result.status());
        assertEquals("CVE-2026-0001", result.findings().getFirst().vulnerabilityId());
        assertEquals("1.0.1", result.findings().getFirst().fixedVersion());
    }

    @Test
    void doesNotStartAProcessWhenDisabled(@TempDir Path project) throws Exception {
        OsvScannerProperties properties = new OsvScannerProperties();
        OsvScanner scanner = new OsvScanner((command, root, timeout) -> {
            throw new AssertionError("process must not be started");
        }, properties);

        assertEquals(OsvScanResult.Status.DISABLED, scanner.scan(project).status());
    }

    @Test
    void ignoresProgressTextBeforeJsonPayload(@TempDir Path project) throws Exception {
        OsvScannerProperties properties = new OsvScannerProperties();
        properties.setEnabled(true);
        OsvScanner scanner = new OsvScanner((command, root, timeout) ->
                new OsvProcessRunner.ProcessResult(0,
                        "Scanning " + root + "\n{\"results\":[]}", false), properties);

        assertEquals(OsvScanResult.Status.PASS, scanner.scan(project).status());
    }

    @Test
    void failsWhenJsonContainsVulnerabilitiesEvenWithZeroExitCode(@TempDir Path project) throws Exception {
        OsvScannerProperties properties = new OsvScannerProperties();
        properties.setEnabled(true);
        OsvScanner scanner = new OsvScanner((command, root, timeout) ->
                new OsvProcessRunner.ProcessResult(0, """
                        {"results":[{"source":{"path":"pom.xml"},"packages":[{"package":{"name":"org.example:demo","version":"1.0.0","ecosystem":"Maven"},"vulnerabilities":[{"id":"CVE-2026-0001"}]}]}]}
                        """, false), properties);

        OsvScanResult result = scanner.scan(project);

        assertEquals(OsvScanResult.Status.FAIL, result.status());
        assertEquals(1, result.findings().size());
    }

    @Test
    void reportsUnavailableWhenExecutableIsMissing(@TempDir Path project) throws Exception {
        OsvScannerProperties properties = new OsvScannerProperties();
        properties.setEnabled(true);
        properties.setExecutable("osv-scanner");
        OsvScanner scanner = new OsvScanner((command, root, timeout) ->
                new OsvProcessRunner.ProcessResult(127, "", false), properties);

        OsvScanResult result = scanner.scan(project);

        assertEquals(OsvScanResult.Status.UNAVAILABLE, result.status());
    }
}
