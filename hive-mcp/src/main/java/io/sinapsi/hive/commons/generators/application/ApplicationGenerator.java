package io.sinapsi.hive.commons.generators.application;

import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.model.ClassSpec;
import io.sinapsi.hive.commons.model.FieldSpec;
import io.sinapsi.hive.commons.support.JavaSourceSupport;
import io.sinapsi.hive.commons.model.MethodSpec;
import io.sinapsi.hive.commons.project.MavenPomUpdater;
import io.sinapsi.hive.commons.support.NameResolver;
import io.sinapsi.hive.commons.template.TemplateRenderer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public final class ApplicationGenerator {
    private final NameResolver names = new NameResolver();
    private final JavaSourceSupport java = new JavaSourceSupport();
    private final TemplateRenderer templates = new TemplateRenderer();
    private final MavenPomUpdater pomUpdater = new MavenPomUpdater();

    public List<Path> useCase(Path root, HiveConfig config, String module, String name, boolean force, boolean factory, List<String> fields) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        String inputPackage = contextRoot + ".application.ports.in";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(root), contextRoot);
        Path port = contextDir.resolve("application/ports/in").resolve(typeName + "UseCase.java");
        Path service = contextDir.resolve("application/services").resolve(typeName + "Service.java");
        String commandType = typeName + "Command";
        List<FieldSpec> commandFields = java.parseFields(fields);
        if (!Files.exists(port) || force) {
            write(port, templates.render("application/use-case.java.ftl", Map.of(
                    "packageName", inputPackage,
                    "className", typeName,
                    "commandType", commandType,
                    "commandFields", commandFields,
                    "commandImportsBlock", importsForTypes(root, contextRoot, commandFields.stream().map(FieldSpec::type).toList()),
                    "factory", factory,
                    "commandParameters", commandParameters(commandFields),
                    "commandArguments", commandArguments(commandFields)
            )), force);
        }
        if (factory) {
            pomUpdater.ensureValidatorDependency(root, config);
        }
        if (!Files.exists(service) || force) {
            write(service, templates.render("application/service.java.ftl", Map.of(
                    "packageName", contextRoot + ".application.services",
                    "inputPortPackage", inputPackage,
                    "useCaseType", typeName + "UseCase",
                    "className", typeName + "Service",
                "returnType", "Result<Unit>",
                    "commandType", commandType,
                    "body", "// TODO: implement the use case logic\n        return Result.success(Unit.INSTANCE);"
            )), force);
        }
        pomUpdater.ensureBasePom(root, config);
        return List.of(port, service);
    }

    public List<Path> port(Path root, HiveConfig config, String module, String name, List<String> methods, boolean force) throws IOException {
        String typeName = portTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot).resolve("application/ports/out").resolve(typeName + ".java");
        List<MethodSpec> methodSpecs = java.parseMethods(methods);
        String methodLines = String.join("\n", methodSpecs.stream().map(this::renderMethod).toList());
        String source = templates.render("application/port.java.ftl", Map.of(
                "packageName", contextRoot + ".application.ports.out",
                "className", typeName,
                "importsBlock", importsForMethods(root, contextRoot, methodSpecs),
                "methodLines", methodLines));
        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> command(Path root, HiveConfig config, String module, String name, List<String> fields, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        String contextRoot = contextRootPackage(config, module);
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot)
                .resolve("application/ports/in")
                .resolve(typeName + "UseCase.java");
        useCase(root, config, module, typeName, force, false, fields);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    public List<Path> record(Path root, HiveConfig config, String module, String name, List<String> fields, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        return simpleRecord(root, config, module, typeName, fields,
                typeName.endsWith("View") ? "application.ports.in.views" : "commons", force);
    }

    public List<Path> plainClass(Path root, HiveConfig config, String module, ClassSpec spec, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(spec.name());
        List<FieldSpec> fields = java.parseFields(spec.fieldValues());
        String contextRoot = contextRootPackage(config, module);
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot).resolve("commons").resolve(typeName + ".java");
        String body = "public class " + typeName + " {\n" + fields.stream().map(f -> "    private " + f.type() + " " + f.name() + ";\n").reduce("", String::concat) + "}\n";
        String source = templates.render("application/class.java.ftl", Map.of("packageName", contextRoot + ".commons", "importsBlock", importsForTypes(root, contextRoot, fields.stream().map(FieldSpec::type).toList()), "sourceBody", body));
        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    private List<Path> simpleRecord(Path root, HiveConfig config, String module, String name, List<String> values, String relativePackage, boolean force) throws IOException {
        String typeName = names.requireJavaTypeName(name);
        List<FieldSpec> fields = java.parseFields(values);
        String contextRoot = contextRootPackage(config, module);
        Path target = names.packageDirectory(config.javaSourceRoot(root), contextRoot).resolve(relativePackage.replace('.', '/')).resolve(typeName + ".java");
        String source = templates.render(typeName.endsWith("View") ? "application/view.java.ftl" : "application/record.java.ftl",
                Map.of("packageName", contextRoot + "." + relativePackage,
                        "importsBlock", importsForTypes(root, contextRoot, fields.stream().map(FieldSpec::type).toList()),
                        "className", typeName, "fields", fields,
                        "factoryParameters", commandParameters(fields),
                        "factoryArguments", commandArguments(fields)));
        write(target, source, force);
        pomUpdater.ensureBasePom(root, config);
        return List.of(target);
    }

    private String renderMethod(MethodSpec method) {
        String parameters = method.parameters().stream().map(f -> f.type() + " " + f.name()).reduce((a, b) -> a + ", " + b).orElse("");
        return "    " + method.returnType() + " " + method.name() + "(" + parameters + ");";
    }

    private String portTypeName(String name) {
        String typeName = names.requireJavaTypeName(name);
        return typeName.endsWith("Port") ? typeName : typeName + "Port";
    }

    private String commandParameters(List<FieldSpec> fields) {
        return fields.stream()
                .map(field -> field.type() + " " + field.name())
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String commandArguments(List<FieldSpec> fields) {
        return fields.stream()
                .map(FieldSpec::name)
                .reduce((left, right) -> left + ", " + right)
                .orElse("");
    }

    private String contextRootPackage(HiveConfig config, String module) {
        return module == null || module.isBlank() ? config.basePackage() : config.basePackage() + ".modules." + names.modulePackageName(module);
    }

    private String importsForMethods(Path root, String contextRoot, List<MethodSpec> methods) {
        List<String> types = methods.stream()
                .flatMap(method -> Stream.concat(
                        java.referencedTypes(method.returnType()).stream(),
                        method.parameters().stream()
                                .flatMap(parameter -> java.referencedTypes(parameter.type()).stream())))
                .toList();
        return importsForTypes(root, contextRoot, types);
    }

    private String importsForTypes(Path root, String contextRoot, List<String> types) {
        StringBuilder imports = new StringBuilder(java.importsFor(types));
        for (String type : types.stream().flatMap(value -> java.referencedTypes(value).stream()).distinct().toList()) {
            if (type.contains(".") || java.importsFor(List.of(type)).contains("import ")
                    || List.of("void", "boolean", "byte", "short", "int", "long", "float", "double", "char").contains(type)) {
                continue;
            }
            for (String relative : List.of("domain/valueobjects", "domain/entities", "domain/aggregates", "application/ports/in/views")) {
                Path candidate = root.resolve("src/main/java").resolve(contextRoot.replace('.', '/')).resolve(relative).resolve(type + ".java");
                if (Files.exists(candidate)) {
                    imports.append("import ").append(contextRoot).append('.').append(relative.replace('/', '.'))
                            .append('.').append(type).append(";\n");
                    break;
                }
            }
        }
        return imports.toString();
    }

    private void write(Path target, String source, boolean force) throws IOException {
        if (Files.exists(target) && !force) {
            throw new IOException("Refusing to overwrite existing file: " + target);
        }
        Files.createDirectories(target.getParent());
        Files.writeString(target, source);
    }
}
