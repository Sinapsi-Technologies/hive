package io.sinapsi.hive.cli.output;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonOutputTest {

    @Test
    void rendersStringsBooleansAndArrays() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("command", "create port");
        fields.put("ok", true);
        fields.put("created", List.of("a/B.java", "c/D.java"));

        String json = JsonOutput.render(fields);

        assertTrue(json.contains("\"command\": \"create port\""), json);
        assertTrue(json.contains("\"ok\": true"), json);
        assertTrue(json.contains("\"a/B.java\""), json);
        assertTrue(json.contains("\"c/D.java\""), json);
    }

    @Test
    void escapesSpecialCharacters() {
        Map<String, Object> fields = new LinkedHashMap<>();
        fields.put("value", "a\"b\\c\n");

        String json = JsonOutput.render(fields);

        assertTrue(json.contains("a\\\"b\\\\c\\n"), json);
    }

    @Test
    void relativizesPathsAgainstRootWithForwardSlashes() {
        Path root = Path.of("/tmp/project");

        List<String> relative = JsonOutput.relativePaths(
                root,
                List.of(Path.of("/tmp/project/src/main/java/A.java"))
        );

        assertEquals(List.of("src/main/java/A.java"), relative);
    }
}
