package io.sinapsi.hive.cli.c4;

import io.sinapsi.hive.cli.c4.config.C4Config;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.c4.render.PlantUmlRenderer;
import io.sinapsi.hive.cli.c4.render.RenderFormat;
import io.sinapsi.hive.cli.c4.render.RenderResult;
import io.sinapsi.hive.cli.c4.scan.ProjectArchitectureScanner;
import io.sinapsi.hive.cli.c4.writer.ArchitectureSiteWriter;
import io.sinapsi.hive.cli.c4.writer.PlantUmlC4Writer;
import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.output.JsonOutput;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Orchestrates {@code hive c4 generate}: scan the project, write {@code .puml} files, optionally
 * render them and build the HTML site — all honoring the deterministic, no-overwrite-without-force
 * rules the rest of the Hive CLI follows. Kept free of Picocli so it can be unit-tested directly.
 */
public final class C4Generator {
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final ProjectArchitectureScanner scanner = new ProjectArchitectureScanner();
    private final PlantUmlC4Writer diagramWriter = new PlantUmlC4Writer();
    private final ArchitectureSiteWriter siteWriter = new ArchitectureSiteWriter();
    private final PlantUmlRenderer rendererOverride;

    public C4Generator() {
        this.rendererOverride = null;
    }

    /** Visible for testing: inject a renderer (e.g. one pointed at a bogus binary to test fallback). */
    public C4Generator(PlantUmlRenderer renderer) {
        this.rendererOverride = renderer;
    }

    public record Request(
            Path projectRoot,
            HiveConfig config,
            C4Config c4Config,
            String module,
            DiagramLevel level,
            Path outputDir,
            boolean render,
            RenderFormat renderFormat,
            boolean site,
            boolean force
    ) {
    }

    public record Result(
            List<Path> created,
            List<Path> skipped,
            List<String> warnings,
            List<ArchitectureView> views,
            Path openTarget
    ) {
    }

    public Result generate(Request request) throws IOException {
        Path root = request.projectRoot();
        Path outputDir = request.outputDir();

        List<Path> created = new ArrayList<>();
        List<Path> skipped = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        List<ArchitectureView> views = scanner.scan(root, request.config(), request.module(), request.level());
        if (views.isEmpty()) {
            String reason = request.module() == null || request.module().isBlank()
                    ? "No Hive components found under base package " + request.config().basePackage() + "."
                    : "No Hive components found for module '" + request.module() + "'.";
            warnings.add(reason);
        }

        PlantUmlRenderer renderer = request.render() ? renderer(request.c4Config()) : null;
        List<ArchitectureSiteWriter.DiagramEntry> siteEntries = new ArrayList<>();

        for (ArchitectureView view : views) {
            String pumlName = diagramWriter.fileName(view);
            Path pumlPath = outputDir.resolve(pumlName);
            writeFile(pumlPath, diagramWriter.render(view), request.force(), root, created, skipped, warnings);

            String base = pumlName.substring(0, pumlName.length() - ".puml".length());
            String imageFileName = null;

            if (request.render() && Files.exists(pumlPath)) {
                Path imagePath = outputDir.resolve(base + "." + request.renderFormat().extension());
                if (Files.exists(imagePath) && !request.force()) {
                    skipped.add(imagePath);
                    warnings.add(existsWarning(root, imagePath));
                    imageFileName = imagePath.getFileName().toString();
                } else {
                    RenderResult result = renderer.render(pumlPath, request.renderFormat());
                    if (result.rendered()) {
                        created.add(result.output());
                        imageFileName = result.output().getFileName().toString();
                    } else {
                        warnings.add(result.skipReason());
                    }
                }
            } else if (request.site()) {
                imageFileName = findExistingImage(outputDir, base);
            }

            if (request.site()) {
                siteEntries.add(toSiteEntry(view, outputDir, pumlName, imageFileName));
            }
        }

        if (request.site()) {
            Path sitePath = outputDir.resolve(siteWriter.fileName());
            String basePackage = request.config().basePackage();
            String html = siteWriter.render(
                    basePackage,
                    basePackage,
                    LocalDateTime.now().format(TIMESTAMP),
                    siteEntries);
            writeFile(sitePath, html, request.force(), root, created, skipped, warnings);
        }

        return new Result(created, skipped, warnings, views, resolveOpenTarget(request, outputDir, created));
    }

    private ArchitectureSiteWriter.DiagramEntry toSiteEntry(
            ArchitectureView view, Path outputDir, String pumlName, String imageFileName) {
        String inlineSvg = null;
        if (imageFileName != null && imageFileName.endsWith(".svg")) {
            try {
                inlineSvg = ArchitectureSiteWriter.stripSvgProlog(Files.readString(outputDir.resolve(imageFileName)));
            } catch (IOException ignored) {
                inlineSvg = null;
            }
        }
        String displayName = view.name() + " Module — " + view.level().shortName() + " " + view.level().titleSuffix();
        return new ArchitectureSiteWriter.DiagramEntry(displayName, pumlName, imageFileName, inlineSvg);
    }

    private Path resolveOpenTarget(Request request, Path outputDir, List<Path> created) {
        if (request.site()) {
            Path sitePath = outputDir.resolve(siteWriter.fileName());
            if (Files.exists(sitePath)) {
                return sitePath;
            }
        }
        for (Path path : created) {
            String name = path.getFileName().toString();
            if (name.endsWith(".svg") || name.endsWith(".png")) {
                return path;
            }
        }
        for (Path path : created) {
            if (path.getFileName().toString().endsWith(".puml")) {
                return path;
            }
        }
        return null;
    }

    private String findExistingImage(Path outputDir, String base) {
        for (String ext : List.of("svg", "png")) {
            Path candidate = outputDir.resolve(base + "." + ext);
            if (Files.exists(candidate)) {
                return candidate.getFileName().toString();
            }
        }
        return null;
    }

    private boolean writeFile(
            Path path,
            String content,
            boolean force,
            Path root,
            List<Path> created,
            List<Path> skipped,
            List<String> warnings
    ) throws IOException {
        if (Files.exists(path) && !force) {
            skipped.add(path);
            warnings.add(existsWarning(root, path));
            return false;
        }
        Files.createDirectories(path.getParent());
        Files.writeString(path, content);
        created.add(path);
        return true;
    }

    private String existsWarning(Path root, Path path) {
        return "File already exists. Use --force to overwrite: " + JsonOutput.relativePath(root, path);
    }

    private PlantUmlRenderer renderer(C4Config config) {
        return rendererOverride != null ? rendererOverride : new PlantUmlRenderer(config);
    }
}
