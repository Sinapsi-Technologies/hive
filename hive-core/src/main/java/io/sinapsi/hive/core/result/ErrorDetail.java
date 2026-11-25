package io.sinapsi.hive.core.result;

import java.util.Objects;

/** A field or object-level error. An empty field identifies the object itself. */
public record ErrorDetail(String field, String message) {
    public ErrorDetail {
        Objects.requireNonNull(field, "field must not be null");
        Objects.requireNonNull(message, "message must not be null");
    }
}
