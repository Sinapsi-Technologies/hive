package io.sinapsi.hive.archunit.fixtures.violations.domaindep.domain;

import io.sinapsi.hive.archunit.fixtures.violations.domaindep.application.OrderWorkflow;

// Intentional violation: domain code must not depend on application.
public final class OrderEntity {
    private final OrderWorkflow workflow = new OrderWorkflow();

    public OrderWorkflow workflow() {
        return workflow;
    }
}
