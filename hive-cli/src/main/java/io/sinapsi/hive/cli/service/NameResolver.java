package io.sinapsi.hive.cli.service;

import java.nio.file.Path;

public final class NameResolver {
    public String packagePath(String packageName) {
        return packageName.replace('.', '/');
    }

    public String modulePackageName(String moduleName) {
        return toLowerCamel(moduleName.replace('-', '_'));
    }

    public Path packageDirectory(Path sourceRoot, String packageName) {
        return sourceRoot.resolve(packagePath(packageName));
    }

    public String requireJavaTypeName(String name) {
        if (name == null || name.isBlank() || !Character.isJavaIdentifierStart(name.charAt(0))) {
            throw new IllegalArgumentException("Invalid Java type name: " + name);
        }
        for (int i = 1; i < name.length(); i++) {
            if (!Character.isJavaIdentifierPart(name.charAt(i))) {
                throw new IllegalArgumentException("Invalid Java type name: " + name);
            }
        }
        return name;
    }

    private String toLowerCamel(String value) {
        StringBuilder resolved = new StringBuilder();
        boolean uppercaseNext = false;
        for (char c : value.toCharArray()) {
            if (c == '_' || c == '-') {
                uppercaseNext = true;
                continue;
            }
            if (resolved.isEmpty()) {
                resolved.append(Character.toLowerCase(c));
            } else if (uppercaseNext) {
                resolved.append(Character.toUpperCase(c));
                uppercaseNext = false;
            } else {
                resolved.append(c);
            }
        }
        return resolved.toString();
    }
}
