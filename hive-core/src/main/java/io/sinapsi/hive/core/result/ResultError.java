package io.sinapsi.hive.core.result;

import java.util.List;
import java.util.Objects;

/** Framework-free error information shared by all failure transformations. */
public record ResultError(ErrorCode code, String message, List<ErrorDetail> details) {
    public ResultError {
        Objects.requireNonNull(code, "code must not be null");
        Objects.requireNonNull(message, "message must not be null");
        details = List.copyOf(details);
    }

    public ResultError(ErrorCode code, String message) {
        this(code, message, List.of());
    }
}
