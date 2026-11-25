package io.sinapsi.hive.commons.support;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

class HiveVersionsTest {

    @Test
    void exposesNonBlankDependencyVersions() {

        assertFalse(HiveVersions.hive().isBlank());
        assertFalse(HiveVersions.archUnit().isBlank());
    }
}
