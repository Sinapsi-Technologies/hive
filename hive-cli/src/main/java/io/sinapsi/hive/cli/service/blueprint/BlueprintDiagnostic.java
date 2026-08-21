package io.sinapsi.hive.cli.service.blueprint;

import java.util.LinkedHashMap;
import java.util.Map;

public record BlueprintDiagnostic(
        String code,
        String path,
        String message,
        String kind,
        String name,
        String file,
        Integer line
) {
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("code", code);
        map.put("path", path);
        map.put("message", message);
        if (kind != null) {
            map.put("kind", kind);
        }
        if (name != null) {
            map.put("name", name);
        }
        if (file != null) {
            map.put("file", file);
        }
        if (line != null) {
            map.put("line", line);
        }
        return map;
    }
}
