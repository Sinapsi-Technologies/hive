package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.c4.C4Generator;
import io.sinapsi.hive.cli.c4.config.C4Config;
import io.sinapsi.hive.cli.c4.config.C4ConfigLoader;
import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureRelation;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.c4.render.RenderFormat;
import io.sinapsi.hive.cli.c4.util.DesktopOpener;
import io.sinapsi.hive.cli.c4.util.TextNames;
import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.HiveConfigLoader;
import io.sinapsi.hive.cli.service.ProjectLocator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Callable;

/**
 * {@code hive c4 generate} — turn a Hive project's hexagonal package structure into C4 diagrams
 * (PlantUML), optionally rendered to SVG/PNG and shown on a static HTML architecture page.
 */
@Command(
        name = "generate",
        mixinStandardHelpOptions = true,
        description = "Generate C4 diagrams from the Hive package structure."
)
public final class C4GenerateCommand implements Callable<Integer> {
    @Option(names = "--module", paramLabel = "name", description = "Module / bounded-context name.")
    String module;

    @Option(names = "--level", paramLabel = "level",
            description = "Diagram level: context, container, component. Default: component.")
    String level = "component";

    @Option(names = "--output", paramLabel = "path", description = "Output directory. Default: docs/architecture.")
    String output;

    @Option(names = "--format", paramLabel = "format", description = "Diagram format. For now: puml.")
    String format = "puml";

    @Option(names = "--render", arity = "0..1", paramLabel = "format", fallbackValue = "",
            description = "Render generated .puml to svg or png. Default: svg.")
    String render;

    @Option(names = "--site", description = "Generate an index.html architecture page.")
    boolean site;

    @Option(names = "--open", description = "Open the generated page or diagram after generation.")
    boolean open;

    @Option(names = "--json", description = "Print the result as JSON.")
    boolean json;

    @Option(names = "--force", description = "Overwrite generated files when needed.")
    boolean force;

    /** Directory the project search starts from. Defaults to the process working directory;
     * overridable in tests, where the JVM caches the real working directory. */
    Path startDir = Path.of(".");

    @Override
    public Integer call() throws Exception {
        Optional<Path> located = new ProjectLocator().locate(startDir);
        if (located.isEmpty()) {
            return failNoProject();
        }
        Path root = located.get();

        DiagramLevel diagramLevel;
        boolean renderRequested = render != null;
        RenderFormat renderFormat = null;
        C4Config c4Config = new C4ConfigLoader().load(root);
        try {
            if (!"puml".equalsIgnoreCase(format)) {
                throw new IllegalArgumentException(
                        "Unsupported format: " + format + ". Only 'puml' is supported for now.");
            }
            diagramLevel = DiagramLevel.parse(level);
            if (renderRequested) {
                String requested = render.isBlank() ? c4Config.defaultRenderFormat() : render;
                renderFormat = RenderFormat.parse(requested);
            }
        } catch (IllegalArgumentException exception) {
            return fail(exception.getMessage());
        }

        HiveConfig config = new HiveConfigLoader().load(root);
        boolean siteRequested = site || c4Config.generateSite();
        String outputValue = output == null || output.isBlank() ? "docs/architecture" : output;
        Path outputDir = root.resolve(outputValue);

        C4Generator.Request request = new C4Generator.Request(
                root, config, c4Config, module, diagramLevel, outputDir,
                renderRequested, renderFormat, siteRequested, force);
        C4Generator.Result result = new C4Generator().generate(request);

        if (open && result.openTarget() != null) {
            DesktopOpener.open(result.openTarget()).ifPresent(result.warnings()::add);
        }

        if (json) {
            printJson(root, outputDir, diagramLevel, result);
        } else {
            printHuman(root, result);
        }
        return 0;
    }

    private void printHuman(Path root, C4Generator.Result result) {
        result.warnings().forEach(warning -> System.err.println("WARNING: " + warning));
        if (result.created().isEmpty()) {
            System.out.println("No C4 documentation was generated.");
            return;
        }
        System.out.println("Generated C4 architecture documentation:");
        System.out.println();
        for (Path path : result.created()) {
            System.out.println("- " + JsonOutput.relativePath(root, path));
        }
        if (result.openTarget() != null) {
            System.out.println();
            System.out.println("Open " + JsonOutput.relativePath(root, result.openTarget())
                    + " to explore the architecture.");
        }
    }

    private void printJson(Path root, Path outputDir, DiagramLevel level, C4Generator.Result result) {
        List<Map<String, Object>> elements = new ArrayList<>();
        List<Map<String, Object>> relations = new ArrayList<>();
        for (ArchitectureView view : result.views()) {
            for (ArchitectureElement element : view.elements()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("id", element.id());
                entry.put("name", element.name());
                entry.put("type", element.type().name());
                elements.add(entry);
            }
            for (ArchitectureRelation relation : view.relations()) {
                Map<String, Object> entry = new LinkedHashMap<>();
                entry.put("source", relation.sourceId());
                entry.put("target", relation.targetId());
                entry.put("description", relation.description());
                relations.add(entry);
            }
        }

        Map<String, Object> json = new LinkedHashMap<>();
        json.put("command", "c4 generate");
        json.put("level", level.cliValue());
        json.put("module", moduleField(result.views()));
        json.put("output", JsonOutput.relativePath(root, outputDir));
        json.put("created", JsonOutput.relativePaths(root, result.created()));
        json.put("skipped", JsonOutput.relativePaths(root, result.skipped()));
        json.put("warnings", result.warnings());
        json.put("elements", elements);
        json.put("relations", relations);
        System.out.println(JsonOutput.render(json));
    }

    private String moduleField(List<ArchitectureView> views) {
        if (module != null && !module.isBlank()) {
            return TextNames.slug(module);
        }
        if (views.size() == 1) {
            return views.get(0).slug();
        }
        return "all";
    }

    private Integer failNoProject() {
        return fail("No .hive-project found.");
    }

    private Integer fail(String message) {
        if (json) {
            Map<String, Object> output = new LinkedHashMap<>();
            output.put("command", "c4 generate");
            output.put("ok", false);
            output.put("error", message);
            System.out.println(JsonOutput.render(output));
        } else {
            System.err.println(message);
        }
        return 2;
    }
}
