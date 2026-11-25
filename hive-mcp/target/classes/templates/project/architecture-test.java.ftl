package ${packageName};

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import io.sinapsi.hive.archunit.HexagonalRules;

@AnalyzeClasses(packages = "${basePackage}")
class ArchitectureTest {
    @ArchTest
    static final ArchRule hiveBaseRules = HexagonalRules.allBaseRules("${basePackage}");
}
