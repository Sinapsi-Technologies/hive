package io.sinapsi.hive.cli.c4.model;

import java.nio.file.Path;

/**
 * A single {@code .java} file reduced to what the scanner needs: its path, its package name, and
 * its (top-level) class name. No AST parsing — package and class are derived from the file's
 * location, which keeps scanning deterministic and dependency-free.
 */
public record JavaSourceFile(Path path, String packageName, String className) {
    public String qualifiedName() {
        return packageName.isBlank() ? className : packageName + "." + className;
    }
}
