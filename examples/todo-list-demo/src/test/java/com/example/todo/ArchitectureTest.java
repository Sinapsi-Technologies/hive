package com.example.todo;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.sinapsi.hive.archunit.HexagonalRules;

@AnalyzeClasses(packages = "com.example.todo")
class ArchitectureTest {
    @ArchTest
    static final ArchRule hiveBaseRules = HexagonalRules.allBaseRules("com.example.todo");
}
