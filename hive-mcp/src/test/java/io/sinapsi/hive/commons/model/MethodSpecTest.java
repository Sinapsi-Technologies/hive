package io.sinapsi.hive.commons.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class MethodSpecTest {

    @Test
    void storesSignatureAndParameters() {

        MethodSpec method = new MethodSpec("Optional<User>", "load", List.of(new FieldSpec("id", "UserId")));

        assertEquals("Optional<User>", method.returnType());
        assertEquals("load", method.name());
        assertEquals(List.of(new FieldSpec("id", "UserId")), method.parameters());
    }
}
