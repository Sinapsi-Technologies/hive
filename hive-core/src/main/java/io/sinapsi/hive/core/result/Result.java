package io.sinapsi.hive.core.result;

import java.util.List;
import java.util.function.Function;

/** An operation outcome: either a success value or an application error. */
public sealed interface Result<T> permits Success, Failure {

    boolean isSuccess();

    <R> Result<R> map(Function<? super T, ? extends R> mapper);

    <R> Result<R> flatMap(Function<? super T, Result<R>> mapper);

    <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super ResultError, ? extends R> onFailure
    );

    static <T> Success<T> success(T value) {
        return Success.of(value);
    }

    static <T> Failure<T> failure(ErrorCode code, String message) {
        return Failure.of(code, message);
    }

    static <T> Failure<T> failure(ErrorCode code, String message, List<ErrorDetail> details) {
        return new Failure<>(new ResultError(code, message, details));
    }

    static <T> Failure<T> failure(ResultError error) {
        return new Failure<>(error);
    }
}
