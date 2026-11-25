package io.sinapsi.hive.archunit.fixtures.violations.domainvalidation.domain;

import jakarta.validation.constraints.NotBlank;

public record ValidatedName(@NotBlank String value) {}
