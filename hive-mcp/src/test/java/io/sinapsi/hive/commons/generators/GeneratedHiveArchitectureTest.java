package io.sinapsi.hive.commons.generators;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import io.sinapsi.hive.archunit.HexagonalRules;
import io.sinapsi.hive.commons.generators.application.ApplicationGenerator;
import io.sinapsi.hive.commons.generators.domain.DomainGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.InfrastructureGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundAdapterGenerator;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundAdapterType;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundGenerationRequest;
import io.sinapsi.hive.commons.generators.infrastructure.inbound.InboundOperation;
import io.sinapsi.hive.commons.generators.project.ProjectGenerator;
import io.sinapsi.hive.commons.model.ClassSpec;
import io.sinapsi.hive.commons.model.HiveConfig;
import io.sinapsi.hive.commons.model.ValueObjectSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class GeneratedHiveArchitectureTest {

    @Test
    void generatedHiveProjectCompilesAndSatisfiesHexagonalRules(@TempDir Path temporaryProject) throws Exception {
        Path project = Optional.ofNullable(System.getProperty("hive.generated.output"))
                .map(Path::of)
                .orElse(temporaryProject);
        HiveConfig config = HiveConfig.defaults();

        new ProjectGenerator().init(project, config, false, false);

        DomainGenerator domain = new DomainGenerator();
        domain.valueObject(project, config, null,
                new ValueObjectSpec("Email", "String", true, true, null, null, null, null, null, List.of(), false), false);
        domain.identifier(project, config, null, "UserId", "UUID", false);
        domain.entity(project, config, null, "User", "UserId", List.of("name:String"), false);
        domain.aggregate(project, config, null, "UserAggregate", "UserId", List.of("name:String"), false);
        domain.enumeration(project, config, null, "UserStatus", List.of("active", "disabled"), false);
        domain.event(project, config, null, "UserCreated", List.of("userId:String"), false);
        domain.exception(project, config, null, "UserNotFound", "User not found", false);
        domain.snapshot(project, config, null, "UserSnapshot", List.of("name:String"), false);

        ApplicationGenerator application = new ApplicationGenerator();
        application.useCase(project, config, null, "CreateUser", false, true, List.of("email:String"));
        application.command(project, config, null, "CreateUser", List.of("email:String"), false);
        application.port(project, config, null, "UserStore", List.of(), false);
        application.record(project, config, null, "UserView", List.of("name:String", "roles:List<String>"), false);
        application.record(project, config, "orders", "OrderView", List.of(), false);
        application.port(project, config, null, "ListUsers", List.of("List<UserView> load()"), false);
        application.plainClass(project, config, null,
                new ClassSpec("ApplicationMarker", List.of(), false, false, false, false), false);

        new InfrastructureGenerator().adapter(project, config, null, "UserStore", List.of("UserStore"), null, false);
        new InboundAdapterGenerator().generate(project, config, new InboundGenerationRequest(
                InboundAdapterType.REST,
                null,
                "User",
                List.of(InboundOperation.useCaseOnly("CreateUser")),
                false
        ));

        Path classes = project.resolve("target/generated-classes");
        compileGeneratedSources(project.resolve(config.javaSourceRoot()), classes);

        JavaClasses generated = new ClassFileImporter().importPath(classes);
        HexagonalRules.allBaseRules(config.basePackage()).check(generated);

        try (var loader = new URLClassLoader(new java.net.URL[]{classes.toUri().toURL()}, getClass().getClassLoader())) {
            Class<?> useCase = loader.loadClass("com.example.app.application.ports.in.CreateUserUseCase");
            var called = new java.util.concurrent.atomic.AtomicBoolean();
            Object service = java.lang.reflect.Proxy.newProxyInstance(loader, new Class<?>[]{useCase},
                    (proxy, method, arguments) -> {
                        called.set(true);
                        return io.sinapsi.hive.core.result.Result.success(io.sinapsi.hive.core.result.Unit.INSTANCE);
                    });
            Class<?> adapter = loader.loadClass("com.example.app.infrastructure.adapters.in.rest.UserController");
            Object controller = adapter.getConstructor(useCase).newInstance(service);
            Class<?> request = loader.loadClass("com.example.app.infrastructure.adapters.in.rest.CreateUserRequest");
            Object input = request.getConstructor(String.class).newInstance("ada@example.com");
            var response = (io.sinapsi.hive.core.result.Result<?>) adapter.getMethod("createUser", request).invoke(controller, input);
            assertTrue(response.isSuccess());
            assertTrue(called.get(), "Generated adapter must compose the command factory with the use case");

            Class<?> view = loader.loadClass("com.example.app.application.ports.in.views.UserView");
            Object user = view.getMethod("of", String.class, List.class).invoke(null, "Mario", List.of("admin"));
            assertEquals("Mario", view.getMethod("name").invoke(user));
            assertEquals(List.of("admin"), view.getMethod("roles").invoke(user));
            Class<?> emptyView = loader.loadClass("com.example.app.modules.orders.application.ports.in.views.OrderView");
            assertTrue(emptyView.isInstance(emptyView.getMethod("of").invoke(null)));
        }

        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/domain/valueobjects/Email.java")));
        assertTrue(Files.exists(project.resolve("src/main/java/com/example/app/infrastructure/adapters/in/rest/UserController.java")));
    }

    private void compileGeneratedSources(Path sourceRoot, Path output) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "A JDK is required to compile generated sources");
        Files.createDirectories(output);

        List<Path> sources;
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            sources = files.filter(path -> path.toString().endsWith(".java")).toList();
        }

        try (StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null)) {
            var units = fileManager.getJavaFileObjectsFromPaths(sources);
            var options = List.of("-d", output.toString(), "-classpath", System.getProperty("java.class.path"));
            Boolean success = compiler.getTask(null, fileManager, null, options, null, units).call();
            assertTrue(Boolean.TRUE.equals(success), "Generated Java sources must compile");
        }

    }

}
