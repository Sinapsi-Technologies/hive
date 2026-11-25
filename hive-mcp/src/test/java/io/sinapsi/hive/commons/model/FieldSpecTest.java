package io.sinapsi.hive.commons.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FieldSpecTest {

    @Test
    void storesNameAndType() {

        FieldSpec field = new FieldSpec("email", "String");

        assertEquals("email", field.name());
        assertEquals("String", field.type());
    }
}
