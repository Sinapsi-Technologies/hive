package io.sinapsi.hive.core.result;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public record Failure<T>(ResultError error) implements Result<T> {

    public Failure {
        Objects.requireNonNull(error, "error must not be null");
    }

    public Failure(ErrorCode code, String message) {
        this(new ResultError(code, message));
    }

    public Failure(ErrorCode code, String message, List<ErrorDetail> details) {
        this(new ResultError(code, message, details));
    }

    public static <T> Failure<T> of(ErrorCode code, String message) {
        return new Failure<>(code, message);
    }

    public ErrorCode code() {
        return error.code();
    }

    public String message() {
        return error.message();
    }

    public List<ErrorDetail> details() {
        return error.details();
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return Result.failure(error);
    }

    @Override
    public <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        return Result.failure(error);
    }

    @Override
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super ResultError, ? extends R> onFailure
    ) {
        return onFailure.apply(error);
    }
}
