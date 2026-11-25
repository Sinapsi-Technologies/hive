package io.sinapsi.hive.commons.generators.infrastructure.inbound;

import io.sinapsi.hive.commons.model.FieldSpec;

import java.nio.file.Path;
import java.util.List;

public record ResolvedUseCase(
        String requestedName,
        String baseName,
        String useCaseType,
        String useCasePackage,
        Path useCasePath,
        String commandType,
        String commandPackage,
        Path commandPath,
        String factoryType,
        Path factoryPath,
        List<FieldSpec> commandFields,
        List<String> commandImports,
        boolean nestedCommand
) {
    public boolean hasCommandFactory() {
        return factoryPath != null;
    }

    public String commandReference() {
        if (commandType == null) {
            return null;
        }
        return nestedCommand ? useCaseType + "." + commandType : commandType;
    }

    public String factoryReference() {
        if (!hasCommandFactory()) {
            return null;
        }
        if (nestedCommand && "CommandFactory".equals(factoryType)) {
            return useCaseType + "." + factoryType;
        }
        return "Factory".equals(factoryType) ? commandReference() + ".Factory" : factoryType;
    }

    public boolean requiresFactoryInstance() {
        return hasCommandFactory() && !(nestedCommand && "CommandFactory".equals(factoryType));
    }

    public String factoryFieldName() {
        return Character.toLowerCase(baseName.charAt(0)) + baseName.substring(1) + "CommandFactory";
    }
}
