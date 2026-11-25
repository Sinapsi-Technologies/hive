package io.sinapsi.hive.commons;

import io.sinapsi.hive.commons.model.ValueObjectSpec;
import io.sinapsi.hive.commons.generators.application.ApplicationGenerator;
import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.InfrastructureGenerator;
import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.project.HiveConfigLoader;
import io.sinapsi.hive.commons.support.HiveVersions;
import io.sinapsi.hive.commons.support.JavaSourceSupport;
import io.sinapsi.hive.commons.support.NameResolver;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundAdapterType;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundGenerationRequest;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundOperation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CommonsUnitTest {
    @Test
    void configHasStableDefaults() {
        HiveConfig config = HiveConfig.defaults();
        assertEquals("com.example.app", config.basePackage());
        assertEquals(Path.of("/tmp/project/src/main/java"), config.javaSourceRoot(Path.of("/tmp/project")));
    }

    @Test
    void configLoaderReadsProjectValues(@TempDir Path project) throws Exception {
        Files.writeString(project.resolve("hive.yml"), "basePackage: com.acme.orders\nlayout: modular\n");
        HiveConfig config = new HiveConfigLoader().load(project);
        assertEquals("com.acme.orders", config.basePackage());
        assertEquals("modular", config.layout());
    }

    @Test
    void resolvesJavaAndModuleNames() {
        NameResolver names = new NameResolver();
        assertEquals("customerProfile", names.modulePackageName("customer-profile"));
        assertEquals("com/acme/domain", names.packagePath("com.acme.domain"));
        assertThrows(IllegalArgumentException.class, () -> names.requireJavaTypeName("bad-name"));
    }

    @Test
    void parsesJavaFieldsMethodsAndImports() {
        JavaSourceSupport source = new JavaSourceSupport();
        assertEquals(new io.sinapsi.hive.commons.model.FieldSpec("email", "String"), source.parseField("email:String"));
        assertEquals("load", source.parseMethod("Optional<User> load(UserId id)").name());
        assertTrue(source.importsFor(List.of("UUID", "Optional<Order>")).contains("java.util.UUID"));
    }

    @Test
    void exposesBuildVersions() {
        assertFalse(HiveVersions.hive().isBlank());
        assertFalse(HiveVersions.archUnit().isBlank());
    }

    @Test
    void contextGeneratorsDelegateToScaffolder(@TempDir Path project) throws Exception {
        HiveConfig config = HiveConfig.defaults();
        assertEquals(1, new DomainGenerator().valueObject(project, config, null,
                new ValueObjectSpec("Email", "String", false, false, null, null, null, null, null, List.of(), false), false).size());
        assertEquals(2, new ApplicationGenerator().useCase(project, config, null, "CreateUser", false, false, List.of()).size());
        new ApplicationGenerator().port(project, config, null, "SaveUser", List.of(), false);
        assertEquals(1, new InfrastructureGenerator().adapter(project, config, null, "Persistence", List.of("SaveUserPort"), null, false).size());
    }

    @Test
    void inboundValueTypesAreExplicit() {
        assertEquals("Controller", InboundAdapterType.REST.adapterSuffix());
        assertEquals("CreateUser", InboundOperation.useCaseOnly("CreateUser").useCaseName());
        assertEquals(InboundAdapterType.MCP, new InboundGenerationRequest(InboundAdapterType.MCP, null, "Tools", List.of(), false).type());
    }
}
