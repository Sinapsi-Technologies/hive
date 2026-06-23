package io.sinapsi.hive.core.mapper;

public interface BiMapper<A, B> {
    B toTarget(A source);

    A toSource(B target);
}
