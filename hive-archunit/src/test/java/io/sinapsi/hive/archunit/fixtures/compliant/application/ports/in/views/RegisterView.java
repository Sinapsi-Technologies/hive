package io.sinapsi.hive.archunit.fixtures.compliant.application.ports.in.views;

public record RegisterView(String name) {
    public static RegisterView of(String name) {
        return new RegisterView(name);
    }
}
