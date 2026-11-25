package io.sinapsi.hive.mcp.monitor;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
public final class HiveMonitorController {
    private final OperationRegistry registry;

    public HiveMonitorController(OperationRegistry registry) {
        this.registry = registry;
    }

    @GetMapping("/hive/monitor")
    public String monitor() {
        return "redirect:/hive-monitor.html";
    }

    @GetMapping("/hive/monitor/api/operations")
    @ResponseBody
    public List<OperationRecord> operations() {
        return registry.recent();
    }
}
