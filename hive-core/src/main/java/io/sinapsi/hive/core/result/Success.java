package io.sinapsi.hive.core.result;

import java.util.Objects;
import java.util.function.Function;

public record Success<T>(T value) implements Result<T> {

    public Success {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static <T> Success<T> of(T value) {
        return new Success<>(value);
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public <R> Result<R> map(Function<? super T, ? extends R> mapper) {
        return Result.success(mapper.apply(value));
    }

    @Override
    public <R> Result<R> flatMap(Function<? super T, Result<R>> mapper) {
        return Objects.requireNonNull(mapper.apply(value), "flatMap result must not be null");
    }

    @Override
    public <R> R fold(
            Function<? super T, ? extends R> onSuccess,
            Function<? super ResultError, ? extends R> onFailure
    ) {
        return onSuccess.apply(value);
    }
}
