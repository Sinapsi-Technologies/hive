package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class CreateDomainCommandSupport {
    private CreateDomainCommandSupport() {
    }

    static ProjectContext locateProject() throws Exception {
        Path root = new ProjectLocator().locate(Path.of("."))
                .orElseThrow(() -> new IllegalStateException("No .hive-project found"));
        return new ProjectContext(root, new HiveConfigLoader().load(root));
    }

    static String moduleName(List<String> names) {
        return names.size() == 2 ? names.getFirst() : null;
    }

    static String primitiveName(List<String> names) {
        return names.size() == 2 ? names.get(1) : names.getFirst();
    }

    static void printResult(
            boolean json,
            Path root,
            String command,
            String name,
            List<Path> created,
            String humanMessage
    ) {
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", command);
            output.put("name", name);
            output.put("created", JsonOutput.relativePaths(root, created));
            System.out.println(JsonOutput.render(output));
        } else {
            System.out.println(humanMessage);
        }
    }

    record ProjectContext(Path root, HiveConfig config) {
    }
}
