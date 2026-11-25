package io.sinapsi.hive.core.result;

/** Optional shared error identifiers; contexts can also define their own ErrorCode enums. */
public enum CommonErrorCode implements ErrorCode {
    VALIDATION_FAILED;

    @Override
    public String value() {
        return name();
    }
}
