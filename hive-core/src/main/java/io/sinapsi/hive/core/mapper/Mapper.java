package io.sinapsi.hive.core.mapper;

@FunctionalInterface
public interface Mapper<S, T> {
    T map(S source);
}
