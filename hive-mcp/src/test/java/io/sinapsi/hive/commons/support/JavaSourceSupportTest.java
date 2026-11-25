package io.sinapsi.hive.commons.support;

import io.sinapsi.hive.commons.model.FieldSpec;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JavaSourceSupportTest {

    private final JavaSourceSupport source = new JavaSourceSupport();

    @Test
    void parsesFieldsAndMethods() {

        assertEquals(new FieldSpec("email", "String"), source.parseField("email:String"));
        assertEquals("load", source.parseMethod("Optional<User> load(UserId id)").name());
        assertEquals(1, source.parseMethod("Optional<User> load(UserId id)").parameters().size());
    }

    @Test
    void rendersImportsAndRejectsInvalidFields() {

        assertTrue(source.importsFor(List.of("UUID", "Optional<Order>")).contains("java.util.UUID"));
        assertThrows(IllegalArgumentException.class, () -> source.parseField("invalid"));
    }
}
