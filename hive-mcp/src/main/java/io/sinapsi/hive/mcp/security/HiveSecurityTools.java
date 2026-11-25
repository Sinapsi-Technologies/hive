package io.sinapsi.hive.mcp.security;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import io.sinapsi.hive.mcp.project.HiveProjectContext;

import java.io.IOException;
import java.nio.file.Path;

@Component
public final class HiveSecurityTools {

    private final OsvScanner scanner;

    private final HiveProjectContext projectContext;

    public HiveSecurityTools(
            OsvScanner scanner,
            HiveProjectContext projectContext) {
        this.scanner = scanner;
        this.projectContext = projectContext;
    }

    @McpTool(
            name = "hive_scan_dependencies",
            description = "Scan the configured project and its Maven dependency manifests with OSV-Scanner. Use it after adding or upgrading dependencies; the report is JSON and the status is PASS, FAIL, DISABLED, or TIMEOUT.")
    public OsvScanResult scanDependencies() throws IOException, InterruptedException {
        return scanner.scan(projectContext.current());
    }
}
