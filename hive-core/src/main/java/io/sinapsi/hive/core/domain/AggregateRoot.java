package io.sinapsi.hive.core.domain;

import io.sinapsi.hive.core.event.DomainEvent;
import java.util.List;

public interface AggregateRoot<ID extends AggregateId<?>> {

    ID getId();
    List<DomainEvent> pullDomainEvents();
}
