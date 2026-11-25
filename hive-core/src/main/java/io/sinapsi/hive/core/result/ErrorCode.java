package io.sinapsi.hive.core.result;

/** Application error identifiers are intentionally open for each bounded context. */
public interface ErrorCode {

    String value();
}
