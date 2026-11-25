package io.sinapsi.hive.mcp.project;

import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public final class HiveProjectContextTools {
    private final HiveProjectContext context;

    public HiveProjectContextTools(HiveProjectContext context) {
        this.context = context;
    }

    @McpTool(name = "hive_select_project", description = "Select the project root for the current MCP server session. Use this once before analysis or generation when the server serves more than one local project. The path must be an existing directory and, when configured, must be inside hive.allowed-roots; subsequent Hive tools use this project automatically.")
    public Map<String, Object> selectProject(String projectRoot) {
        return Map.of("projectRoot", context.select(projectRoot).toString());
    }

    @McpTool(name = "hive_current_project", description = "Return the project root currently selected for this MCP session. Use it to verify context before reading or writing project files.")
    public Map<String, Object> currentProject() {
        if (context.currentOrNull() == null) {
            return Map.of(
                    "selected", false,
                    "projectRoot", "",
                    "message", "No project selected. Call hive_select_project with an existing directory.");
        }
        return Map.of("selected", true, "projectRoot", context.current().toString());
    }
}
