package io.sinapsi.hive.cli.service.inbound;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FieldSpec;
import io.sinapsi.hive.cli.service.NameResolver;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class UseCaseResolver {
    private static final Pattern RECORD_PARAMETERS = Pattern.compile(
            "(?:public\\s+)?record\\s+\\w+\\s*\\((.*?)\\)\\s*implements\\s+Command",
            Pattern.DOTALL
    );
    private static final Pattern COMMAND_FIELD = Pattern.compile(
            "private\\s+final\\s+(.+?)\\s+(\\w+)\\s*;"
    );

    private final NameResolver names = new NameResolver();

    public ResolvedUseCase resolve(Path projectRoot, HiveConfig config, String moduleName, String useCaseName)
            throws IOException {
        String baseName = stripSuffix(names.requireJavaTypeName(useCaseName), "UseCase");
        String useCaseType = baseName + "UseCase";
        String contextRoot = contextRootPackage(config, moduleName);
        String useCasePackage = contextRoot + ".application.ports.in";
        Path contextDir = names.packageDirectory(config.javaSourceRoot(projectRoot), contextRoot);
        Path useCasePath = contextDir.resolve("application/ports/in").resolve(useCaseType + ".java");
        if (!Files.exists(useCasePath)) {
            throw new IllegalArgumentException("UseCase not found: " + useCaseType + " at " + useCasePath);
        }

        String useCaseSource = Files.readString(useCasePath);
        String commandType = baseName + "Command";
        if (hasCommandType(useCaseSource, commandType)) {
            return new ResolvedUseCase(
                    useCaseName,
                    baseName,
                    useCaseType,
                    useCasePackage,
                    useCasePath,
                    commandType,
                    useCasePackage,
                    useCasePath,
                    "Factory",
                    hasNestedCommandFactory(useCaseSource) ? useCasePath : null,
                    commandFields(useCaseSource),
                    commandImports(useCaseSource),
                    true
            );
        }

        String commandPackage = useCasePackage + ".commands";
        Path commandPath = contextDir.resolve("application/ports/in/commands").resolve(commandType + ".java");
        if (!Files.exists(commandPath)) {
            return new ResolvedUseCase(
                    useCaseName,
                    baseName,
                    useCaseType,
                    useCasePackage,
                    useCasePath,
                    null,
                    commandPackage,
                    null,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    false
            );
        }

        String commandSource = Files.readString(commandPath);
        boolean nestedFactory = hasNestedCommandFactory(commandSource);
        String factoryType = nestedFactory ? "Factory" : baseName + "CommandFactory";
        Path externalFactoryPath = contextDir.resolve("application/ports/in/commands").resolve(factoryType + ".java");
        return new ResolvedUseCase(
                useCaseName,
                baseName,
                useCaseType,
                useCasePackage,
                useCasePath,
                commandType,
                commandPackage,
                commandPath,
                factoryType,
                nestedFactory ? commandPath : Files.exists(externalFactoryPath) ? externalFactoryPath : null,
                commandFields(commandSource),
                commandImports(commandSource),
                false
        );
    }

    private boolean hasCommandType(String source, String commandType) {
        Pattern command = Pattern.compile("\\b(?:record|class)\\s+" + Pattern.quote(commandType) + "\\b");
        return command.matcher(source).find();
    }

    private boolean hasNestedCommandFactory(String source) {
        return Pattern.compile("\\bpublic\\s+static\\s+final\\s+class\\s+Factory\\b").matcher(source).find();
    }

    private List<FieldSpec> commandFields(String source) {
        Matcher matcher = RECORD_PARAMETERS.matcher(source);
        if (!matcher.find() || matcher.group(1).isBlank()) {
            return commandClassFields(source);
        }
        List<FieldSpec> fields = new ArrayList<>();
        for (String rawParameter : matcher.group(1).split(",")) {
            String parameter = rawParameter.strip();
            int split = parameter.lastIndexOf(' ');
            if (split < 1 || split == parameter.length() - 1) {
                return List.of();
            }
            fields.add(new FieldSpec(parameter.substring(split + 1).strip(), parameter.substring(0, split).strip()));
        }
        return fields;
    }

    private List<FieldSpec> commandClassFields(String source) {
        List<FieldSpec> fields = new ArrayList<>();
        Matcher matcher = COMMAND_FIELD.matcher(source);
        while (matcher.find()) {
            fields.add(new FieldSpec(matcher.group(2).strip(), matcher.group(1).strip()));
        }
        return fields;
    }

    private List<String> commandImports(String source) {
        return source.lines()
                .map(String::strip)
                .filter(line -> line.startsWith("import "))
                .filter(line -> !line.equals("import io.sinapsi.hive.core.command.Command;"))
                .filter(line -> !line.equals("import io.sinapsi.hive.core.result.Result;"))
                .filter(line -> !line.equals("import io.sinapsi.hive.core.usecase.UseCase;"))
                .filter(line -> !line.equals("import io.sinapsi.hive.factory.AbstractCommandFactory;"))
                .toList();
    }

    private String contextRootPackage(HiveConfig config, String moduleName) {
        if (moduleName == null || moduleName.isBlank()) {
            return config.basePackage();
        }
        return config.basePackage() + ".modules." + names.modulePackageName(moduleName);
    }

    private String stripSuffix(String value, String suffix) {
        return value.endsWith(suffix) ? value.substring(0, value.length() - suffix.length()) : value;
    }
}
