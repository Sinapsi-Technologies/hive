package io.sinapsi.hive.commons.support;

import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NameResolverTest {

    private final NameResolver resolver = new NameResolver();

    @Test
    void resolvesPackagesAndModules() {

        assertEquals("com/acme/domain", resolver.packagePath("com.acme.domain"));
        assertEquals("customerProfile", resolver.modulePackageName("customer-profile"));
        assertEquals(Path.of("src/main/java/com/acme"), resolver.packageDirectory(Path.of("src/main/java"), "com.acme"));
    }

    @Test
    void rejectsInvalidJavaNames() {

        assertThrows(IllegalArgumentException.class, () -> resolver.requireJavaTypeName("bad-name"));
        assertThrows(IllegalArgumentException.class, () -> resolver.requirePackageSegment("package", "class"));
    }
}
