package io.sinapsi.hive.cli.service.blueprint;

public final class BlueprintParseException extends IllegalArgumentException {
    private final Integer line;

    public BlueprintParseException(String message, Integer line) {
        super(message);
        this.line = line;
    }

    public Integer line() {
        return line;
    }
}
