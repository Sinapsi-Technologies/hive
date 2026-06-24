package io.sinapsi.hive.cli.c4.scan;

import io.sinapsi.hive.cli.c4.model.ArchitectureElement;
import io.sinapsi.hive.cli.c4.model.ArchitectureElementType;
import io.sinapsi.hive.cli.c4.model.ArchitectureView;
import io.sinapsi.hive.cli.c4.model.DiagramLevel;
import io.sinapsi.hive.cli.c4.model.JavaSourceFile;
import io.sinapsi.hive.cli.c4.util.TextNames;
import io.sinapsi.hive.cli.model.HiveConfig;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Walks the Java source tree of a Hive project and builds one {@link ArchitectureView} per
 * module. Supports both the single layout (all code under the base package) and the modular
 * layout ({@code <base>.modules.<name>}). Package, class name, and naming heuristics are enough —
 * no AST analysis.
 */
public final class ProjectArchitectureScanner {
    private final PackageClassifier classifier = new PackageClassifier();
    private final RelationHeuristics relations = new RelationHeuristics();

    /**
     * @param moduleFilter optional module name; in modular layout it selects one module, in single
     *                     layout it overrides the diagram name.
     */
    public List<ArchitectureView> scan(
            Path projectRoot,
            HiveConfig config,
            String moduleFilter,
            DiagramLevel level
    ) throws IOException {
        Path sourceRoot = config.javaSourceRoot(projectRoot);
        if (!Files.isDirectory(sourceRoot)) {
            return List.of();
        }
        List<JavaSourceFile> files = collect(sourceRoot, config.basePackage());
        if (files.isEmpty()) {
            return List.of();
        }

        boolean modular = files.stream().anyMatch(file -> moduleOf(file).isPresent());
        List<ArchitectureView> views = new ArrayList<>();
        if (modular) {
            Map<String, List<JavaSourceFile>> byModule = new LinkedHashMap<>();
            for (JavaSourceFile file : files) {
                moduleOf(file).ifPresent(module ->
                        byModule.computeIfAbsent(module, key -> new ArrayList<>()).add(file));
            }
            String filterSlug = moduleFilter == null || moduleFilter.isBlank()
                    ? null
                    : TextNames.slug(moduleFilter);
            for (Map.Entry<String, List<JavaSourceFile>> entry : byModule.entrySet()) {
                if (filterSlug != null && !TextNames.slug(entry.getKey()).equals(filterSlug)) {
                    continue;
                }
                views.add(buildView(entry.getKey(), entry.getValue(), level));
            }
        } else {
            String name = moduleFilter == null || moduleFilter.isBlank()
                    ? lastSegment(config.basePackage())
                    : moduleFilter;
            views.add(buildView(name, files, level));
        }

        views.sort(Comparator.comparing(ArchitectureView::slug));
        return views;
    }

    private ArchitectureView buildView(String moduleName, List<JavaSourceFile> files, DiagramLevel level) {
        List<JavaSourceFile> visible = new ArrayList<>();
        for (JavaSourceFile file : files) {
            if (typeOf(file).componentVisible()) {
                visible.add(file);
            }
        }
        visible.sort(Comparator
                .comparingInt((JavaSourceFile f) -> typeOf(f).displayOrder())
                .thenComparing(JavaSourceFile::className));

        ArchitectureIdGenerator ids = new ArchitectureIdGenerator();
        List<ArchitectureElement> elements = new ArrayList<>();
        for (JavaSourceFile file : visible) {
            ArchitectureElementType type = typeOf(file);
            String id = ids.generate(file.className());
            elements.add(new ArchitectureElement(id, file.className(), file.className(), type));
        }

        return new ArchitectureView(
                TextNames.title(moduleName),
                TextNames.slug(moduleName),
                level,
                elements,
                relations.build(elements)
        );
    }

    private ArchitectureElementType typeOf(JavaSourceFile file) {
        return classifier.classify(file.packageName(), file.className());
    }

    private List<JavaSourceFile> collect(Path sourceRoot, String basePackage) throws IOException {
        List<JavaSourceFile> files = new ArrayList<>();
        try (Stream<Path> walk = Files.walk(sourceRoot)) {
            walk.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().endsWith(".java"))
                    .filter(path -> {
                        String fileName = path.getFileName().toString();
                        return !fileName.equals("package-info.java") && !fileName.equals("module-info.java");
                    })
                    .sorted()
                    .forEach(path -> toSourceFile(sourceRoot, path, basePackage).ifPresent(files::add));
        }
        return files;
    }

    private Optional<JavaSourceFile> toSourceFile(Path sourceRoot, Path file, String basePackage) {
        Path relative = sourceRoot.relativize(file);
        Path parent = relative.getParent();
        String packageName = parent == null
                ? ""
                : parent.toString().replace(file.getFileSystem().getSeparator(), ".");
        if (!basePackage.isBlank()
                && !packageName.equals(basePackage)
                && !packageName.startsWith(basePackage + ".")) {
            return Optional.empty();
        }
        String fileName = file.getFileName().toString();
        String className = fileName.substring(0, fileName.length() - ".java".length());
        return Optional.of(new JavaSourceFile(file, packageName, className));
    }

    private Optional<String> moduleOf(JavaSourceFile file) {
        String[] parts = file.packageName().split("\\.");
        for (int i = 0; i < parts.length - 1; i++) {
            if (parts[i].equals("modules")) {
                return Optional.of(parts[i + 1]);
            }
        }
        return Optional.empty();
    }

    private String lastSegment(String basePackage) {
        if (basePackage == null || basePackage.isBlank()) {
            return "app";
        }
        int dot = basePackage.lastIndexOf('.');
        return dot < 0 ? basePackage : basePackage.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /** Convenience for tests/clients that prefer unchecked exceptions. */
    public List<ArchitectureView> scanUnchecked(
            Path projectRoot,
            HiveConfig config,
            String moduleFilter,
            DiagramLevel level
    ) {
        try {
            return scan(projectRoot, config, moduleFilter, level);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
