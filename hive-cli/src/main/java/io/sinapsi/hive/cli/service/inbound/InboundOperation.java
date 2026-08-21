package io.sinapsi.hive.cli.service.inbound;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public record InboundOperation(String transport, String useCaseName) {
    private static final Pattern REST_OPERATION = Pattern.compile("\\s*(\\S+)\\s+(\\S+)\\s*->\\s*(\\S+)\\s*");

    public static InboundOperation useCaseOnly(String useCaseName) {
        return new InboundOperation(null, useCaseName);
    }

    public static InboundOperation parseRest(String value) {
        Matcher matcher = REST_OPERATION.matcher(value);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(
                    "Invalid --operation. Expected syntax: \"METHOD /path -> UseCase\"");
        }
        return new InboundOperation(matcher.group(1) + " " + matcher.group(2), matcher.group(3));
    }
}
