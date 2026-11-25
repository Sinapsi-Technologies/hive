package io.sinapsi.hive.archunit.fixtures.violations.portservicedep.application.ports.in;

import io.sinapsi.hive.archunit.fixtures.violations.portservicedep.application.services.UserOutput;

public interface FindUserUseCase {
    UserOutput find();
}
