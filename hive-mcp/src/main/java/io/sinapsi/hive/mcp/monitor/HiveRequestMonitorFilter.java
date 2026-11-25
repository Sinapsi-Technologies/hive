package io.sinapsi.hive.mcp.monitor;

import io.sinapsi.hive.mcp.project.HiveProjectContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public final class HiveRequestMonitorFilter extends OncePerRequestFilter {
    private static final Pattern TOOL_NAME = Pattern.compile("\\\"name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"");
    private final OperationRegistry registry;
    private final HiveProjectContext projectContext;

    public HiveRequestMonitorFilter(OperationRegistry registry, HiveProjectContext projectContext) {
        this.registry = registry;
        this.projectContext = projectContext;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // The dashboard polls this endpoint every second. Recording its own
        // GET requests hides the actual MCP tool calls in the activity stream.
        if (request.getRequestURI().startsWith("/hive/monitor")) {
            filterChain.doFilter(request, response);
            return;
        }

        // MCP tool invocations are JSON-RPC POSTs. GETs are transport/SSE
        // handshakes and do not identify a tool, so they are not operations.
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        ContentCachingRequestWrapper wrapped = new ContentCachingRequestWrapper(request, 1_048_576);
        String operationId = registry.start("MCP JSON-RPC", 
                projectContext.currentOrNull() == null ? "(project not selected)" : projectContext.current().toString());
        long started = System.nanoTime();
        try {
            filterChain.doFilter(wrapped, response);
            String tool = extractTool(wrapped);
            registry.rename(operationId, tool.isBlank() ? "MCP JSON-RPC" : tool);
            registry.complete(operationId, response.getStatus() >= 400 ? "FAILED" : "COMPLETED",
                    elapsed(started), response.getStatus() >= 400 ? "HTTP " + response.getStatus() : "");
        } catch (ServletException | IOException | RuntimeException exception) {
            registry.complete(operationId, "FAILED", elapsed(started), exception.getMessage());
            throw exception;
        }
    }

    private long elapsed(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }

    private String extractTool(ContentCachingRequestWrapper request) {
        String payload = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        Matcher matcher = TOOL_NAME.matcher(payload);
        return matcher.find() ? matcher.group(1) : "";
    }
}
