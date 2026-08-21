package io.sinapsi.hive.cli.command;

import io.sinapsi.hive.cli.output.JsonOutput;
import io.sinapsi.hive.cli.service.blueprint.BlueprintDiagnostic;
import io.sinapsi.hive.cli.service.blueprint.BlueprintValidationResult;
import io.sinapsi.hive.cli.service.blueprint.BlueprintValidator;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

@Command(name = "validate", mixinStandardHelpOptions = true, description = "Validate a Hive blueprint without generating source files.")
public final class BlueprintValidateCommand implements Callable<Integer> {
    @Parameters(index = "0", paramLabel = "FILE", description = "Blueprint YAML file.")
    Path file;

    @Option(names = "--json", description = "Print validation result as JSON.")
    boolean json;

    @Override
    public Integer call() {
        BlueprintValidationResult result = new BlueprintValidator().validate(file);
        if (json) {
            System.out.println(JsonOutput.render(output(result)));
        } else {
            printHuman(result);
        }
        return result.valid() ? 0 : 1;
    }

    private Map<String, Object> output(BlueprintValidationResult result) {
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("command", "blueprint validate");
        output.put("valid", result.valid());
        output.put("file", result.file().toString());
        output.put("errors", result.errors().stream().map(BlueprintDiagnostic::toMap).toList());
        return output;
    }

    private void printHuman(BlueprintValidationResult result) {
        if (result.valid()) {
            System.out.println("Valid blueprint: " + result.file());
            return;
        }
        System.out.println("Invalid blueprint: " + result.file());
        System.out.println();
        for (BlueprintDiagnostic error : result.errors()) {
            String path = error.path() == null ? "<blueprint>" : error.path();
            System.out.println(path);
            System.out.println("  " + error.message());
        }
    }
}
