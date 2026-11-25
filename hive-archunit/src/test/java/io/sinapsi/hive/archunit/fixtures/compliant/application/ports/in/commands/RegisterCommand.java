package io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.commands;

import jakarta.validation.constraints.NotBlank;

public record RegisterCommand(@NotBlank String name) {
}
