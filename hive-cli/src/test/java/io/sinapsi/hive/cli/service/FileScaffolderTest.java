package io.sinapsi.hive.cli.service;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FileScaffolder.ClassSpec;
import io.sinapsi.hive.cli.service.FileScaffolder.ValueObjectSpec;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.util.ArrayList;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FileScaffolderTest {
    @TempDir
    Path tempDir;

    private final FileScaffolder scaffolder = new FileScaffolder();

    @Test
    void initCreatesMarkerConfigAndSourceRoots() throws Exception {
        scaffolder.init(tempDir, HiveConfig.defaults(), false, false);

        assertTrue(Files.exists(tempDir.resolve(".hive-project")));
        assertTrue(Files.exists(tempDir.resolve("hive.yml")));
        assertTrue(Files.isDirectory(tempDir.resolve("src/main/java")));
        assertTrue(Files.isDirectory(tempDir.resolve("src/test/java")));
        assertTrue(Files.readString(tempDir.resolve("hive.yml")).contains("layout: single"));
        assertPomContains("hive-core");
    }

    @Test
    void createUseCaseGeneratesExpectedFiles() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, false);

        Path packageRoot = tempDir.resolve("src/main/java/com/example/app/application");
        assertTrue(Files.exists(packageRoot.resolve("ports/in/commands/CreateUserCommand.java")));
        assertTrue(Files.exists(packageRoot.resolve("ports/in/CreateUserUseCase.java")));
        assertTrue(Files.exists(packageRoot.resolve("services/CreateUserService.java")));
        assertTrue(Files.readString(packageRoot.resolve("services/CreateUserService.java")).contains("TODO"));
        assertFilesCompile(List.of(
                packageRoot.resolve("ports/in/commands/CreateUserCommand.java"),
                packageRoot.resolve("ports/in/CreateUserUseCase.java"),
                packageRoot.resolve("services/CreateUserService.java")
        ));
    }

    @Test
    void createUseCaseWithFactoryGeneratesExpectedFactory() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, true);

        Path packageRoot = tempDir.resolve("src/main/java/com/example/app/application");
        assertTrue(Files.exists(packageRoot.resolve("ports/in/commands/CreateUserCommandFactory.java")));
        assertPomContains("hive-validator");
        assertFilesCompile(List.of(
                packageRoot.resolve("ports/in/commands/CreateUserCommand.java"),
                packageRoot.resolve("ports/in/commands/CreateUserCommandFactory.java"),
                packageRoot.resolve("ports/in/CreateUserUseCase.java"),
                packageRoot.resolve("services/CreateUserService.java")
        ));
    }

    @Test
    void createModuleGeneratesExpectedFolders() throws Exception {
        HiveConfig config = new HiveConfig("com.example.app", "modular", "src/main/java", "src/test/java");
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createModule(tempDir, config, "customer");

        Path moduleRoot = tempDir.resolve("src/main/java/com/example/app/modules/customer");
        List<String> folders = List.of(
                "commons",
                "configurations",
                "application/ports/in/commands",
                "application/ports/out",
                "application/services",
                "domain/valueobjects",
                "domain/aggregates",
                "domain/events",
                "domain/entities",
                "domain/services",
                "domain/snapshots",
                "domain/exceptions",
                "infrastructure/configs",
                "infrastructure/adapters/in",
                "infrastructure/adapters/out"
        );
        for (String folder : folders) {
            assertTrue(Files.isDirectory(moduleRoot.resolve(folder)), "Missing " + folder);
        }
    }

    @Test
    void createArchTestGeneratesExpectedArchitectureTest() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createArchTest(tempDir, config, false);

        Path architectureTest = tempDir.resolve("src/test/java/com/example/app/ArchitectureTest.java");
        assertTrue(Files.exists(architectureTest));
        assertTrue(Files.readString(architectureTest).contains("HexagonalRules.allBaseRules(\"com.example.app\")"));
        assertPomContains("hive-archunit");
        assertPomContains("archunit-junit5");
        assertFilesCompile(List.of(architectureTest));
    }

    @Test
    void createPortGeneratesCompilableOutputPort() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUser", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        assertTrue(Files.exists(port));
        assertTrue(Files.readString(port).contains("extends OutputPort"));
        assertTrue(Files.readString(port).contains("TODO"));
        assertFilesCompile(List.of(port));
    }

    @Test
    void createPortDoesNotDuplicatePortSuffix() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUserPort", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        assertTrue(Files.exists(port));
    }

    @Test
    void createAdapterGeneratesCompilableAdapterImplementingPort() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createPort(tempDir, config, null, "SaveUser", false);
        scaffolder.createAdapter(tempDir, config, null, "InMemorySaveUser", "SaveUser", false);

        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveUserPort.java");
        Path adapter = tempDir.resolve(
                "src/main/java/com/example/app/infrastructure/adapters/out/InMemorySaveUserAdapter.java");
        assertTrue(Files.exists(adapter));
        assertTrue(Files.readString(adapter).contains("implements SaveUserPort"));
        assertTrue(Files.readString(adapter).contains("TODO"));
        assertFilesCompile(List.of(port, adapter));
    }

    @Test
    void createUseCaseDoesNotDuplicateMavenDependencies() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createUseCase(tempDir, config, null, "CreateUser", false, true);
        scaffolder.createUseCase(tempDir, config, null, "CreateUser", true, true);

        String pom = Files.readString(tempDir.resolve("pom.xml"));
        assertEquals(1, occurrences(pom, "<artifactId>hive-core</artifactId>"));
        assertEquals(1, occurrences(pom, "<artifactId>hive-validator</artifactId>"));
    }

    @Test
    void createValueObjectGeneratesRecordWithDeterministicConstraints() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createValueObject(
                tempDir,
                config,
                null,
                new ValueObjectSpec("Email", "String", true, true, null, null, 5, 128, ".+@.+"),
                false
        );

        Path valueObject = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/Email.java");
        String source = Files.readString(valueObject);
        assertTrue(source.contains("public record Email(String value)"));
        assertTrue(source.contains("value must not be blank"));
        assertTrue(source.contains("value.length() < 5"));
        assertTrue(source.contains("value.matches(\".+@.+\")"));
        assertFilesCompile(List.of(valueObject));
    }

    @Test
    void createIdentifierUsesAggregateIdContract() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "CustomerId", "UUID", false);

        Path identifier = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/CustomerId.java");
        String source = Files.readString(identifier);
        assertTrue(source.contains("implements AggregateId<UUID>"));
        assertTrue(source.contains("import java.util.UUID;"));
        assertFilesCompile(List.of(identifier));
    }

    @Test
    void createEntityGeneratesControlledStateWithoutSetters() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "OrderLineId", "UUID", false);
        scaffolder.createValueObject(
                tempDir,
                config,
                null,
                new ValueObjectSpec("Quantity", "Integer", true, false, "1", null, null, null, null),
                false
        );
        scaffolder.createEntity(
                tempDir,
                config,
                null,
                "OrderLine",
                "OrderLineId",
                List.of("quantity:Quantity"),
                false
        );

        Path id = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderLineId.java");
        Path quantity = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/Quantity.java");
        Path entity = tempDir.resolve("src/main/java/com/example/app/domain/entities/OrderLine.java");
        String source = Files.readString(entity);
        assertTrue(source.contains("private final OrderLineId id;"));
        assertTrue(source.contains("private final Quantity quantity;"));
        assertTrue(source.contains("public Quantity quantity()"));
        assertFalse(source.contains(" set"));
        assertFilesCompile(List.of(id, quantity, entity));
    }

    @Test
    void createAggregateUsesAggregateRootAndDomainEventBuffer() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "OrderId", "UUID", false);
        scaffolder.createEnum(tempDir, config, null, "OrderStatus", List.of("DRAFT", "CONFIRMED"), false);
        scaffolder.createAggregate(
                tempDir,
                config,
                null,
                "Order",
                "OrderId",
                List.of("status:OrderStatus"),
                false
        );

        Path id = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java");
        Path status = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderStatus.java");
        Path aggregate = tempDir.resolve("src/main/java/com/example/app/domain/aggregates/Order.java");
        String source = Files.readString(aggregate);
        assertTrue(source.contains("implements AggregateRoot<OrderId>"));
        assertTrue(source.contains("List<DomainEvent> domainEvents"));
        assertTrue(source.contains("TODO: add domain behavior here"));
        assertFilesCompile(List.of(id, status, aggregate));
    }

    @Test
    void createEventAndExceptionGenerateFrameworkFreeDomainTypes() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "OrderId", "UUID", false);
        scaffolder.createEvent(tempDir, config, null, "OrderCreated", List.of("orderId:OrderId", "occurredAt:Instant"), false);
        scaffolder.createException(tempDir, config, null, "OrderAlreadyConfirmed", false);

        Path id = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java");
        Path event = tempDir.resolve("src/main/java/com/example/app/domain/events/OrderCreated.java");
        Path exception = tempDir.resolve("src/main/java/com/example/app/domain/exceptions/OrderAlreadyConfirmedException.java");
        String eventSource = Files.readString(event);
        assertTrue(eventSource.contains("public record OrderCreated("));
        assertTrue(eventSource.contains("OrderId orderId"));
        assertTrue(eventSource.contains("Instant occurredAt"));
        assertTrue(eventSource.contains("implements DomainEvent"));
        assertTrue(Files.readString(exception).contains("extends RuntimeException"));
        assertFilesCompile(List.of(id, event, exception));
    }

    @Test
    void createDomainServiceAndSnapshotGenerateCompilableDomainTypes() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "OrderId", "UUID", false);
        scaffolder.createEnum(tempDir, config, null, "OrderStatus", List.of("DRAFT"), false);
        scaffolder.createDomainService(tempDir, config, null, "Pricing", false);
        scaffolder.createSnapshot(tempDir, config, null, "OrderSnapshot", List.of("id:OrderId", "status:OrderStatus"), false);

        Path id = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java");
        Path status = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderStatus.java");
        Path service = tempDir.resolve("src/main/java/com/example/app/domain/services/PricingService.java");
        Path snapshot = tempDir.resolve("src/main/java/com/example/app/domain/snapshots/OrderSnapshot.java");
        assertTrue(Files.readString(service).contains("TODO: add domain service behavior here"));
        assertTrue(Files.readString(snapshot).contains("public record OrderSnapshot("));
        assertFilesCompile(List.of(id, status, service, snapshot));
    }

    @Test
    void createMultiFieldValueObjectAndGeneralJavaTypesSupportGenerics() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createValueObject(
                tempDir,
                config,
                null,
                new ValueObjectSpec("Money", "String", false, false, null, null, null, null, null,
                        List.of("amount:BigDecimal", "currency:String"), true),
                false
        );
        scaffolder.createRecord(tempDir, config, null, "CustomerResponse",
                List.of("id:UUID", "tags:List<String>"), false);
        scaffolder.createPlainClass(tempDir, config, null,
                new ClassSpec("CustomerDto", List.of("name:String", "email:String"), true, true, true, true),
                false);

        Path money = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/Money.java");
        Path record = tempDir.resolve("src/main/java/com/example/app/commons/CustomerResponse.java");
        Path dto = tempDir.resolve("src/main/java/com/example/app/commons/CustomerDto.java");
        assertTrue(Files.readString(money).contains("public static Money of(BigDecimal amount, String currency)"));
        assertTrue(Files.readString(record).contains("import java.util.List;"));
        assertTrue(Files.readString(dto).contains("public void setEmail(String email)"));
        assertFilesCompile(List.of(money, record, dto));
    }

    @Test
    void createUseCaseCommandPortAndAdapterAcceptDeterministicSignatures() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        scaffolder.createIdentifier(tempDir, config, null, "OrderId", "UUID", false);
        scaffolder.createAggregate(tempDir, config, null, "Order", "OrderId", List.of(), false);
        scaffolder.createUseCase(tempDir, config, null, "CreateOrder", false, true,
                List.of("orderId:OrderId", "tags:List<String>"));
        scaffolder.createCommand(tempDir, config, null, "CancelOrder", List.of("orderId:OrderId"), false);
        scaffolder.createPort(tempDir, config, null, "LoadOrder",
                List.of("Optional<Order> load(OrderId id)"), false);
        scaffolder.createPort(tempDir, config, null, "SaveOrder",
                List.of("void save(Order order)"), false);
        scaffolder.createAdapter(tempDir, config, null, "OrderPersistence",
                List.of("LoadOrder", "SaveOrder"), false);

        Path orderId = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java");
        Path order = tempDir.resolve("src/main/java/com/example/app/domain/aggregates/Order.java");
        Path useCaseCommand = tempDir.resolve("src/main/java/com/example/app/application/ports/in/commands/CreateOrderCommand.java");
        Path command = tempDir.resolve("src/main/java/com/example/app/application/ports/in/commands/CancelOrderCommand.java");
        Path loadPort = tempDir.resolve("src/main/java/com/example/app/application/ports/out/LoadOrderPort.java");
        Path savePort = tempDir.resolve("src/main/java/com/example/app/application/ports/out/SaveOrderPort.java");
        Path adapter = tempDir.resolve("src/main/java/com/example/app/infrastructure/adapters/out/OrderPersistenceAdapter.java");
        assertTrue(Files.readString(useCaseCommand).contains("List<String> tags"));
        assertTrue(Files.readString(command).contains("implements Command"));
        assertTrue(Files.readString(loadPort).contains("Optional<Order> load(OrderId id);"));
        assertTrue(Files.readString(adapter).contains("implements LoadOrderPort, SaveOrderPort"));
        assertTrue(Files.readString(adapter).contains("public Optional<Order> load(OrderId id)"));
        assertTrue(Files.readString(adapter).contains("public void save(Order order)"));
        assertFilesCompile(List.of(orderId, order, useCaseCommand, command, loadPort, savePort, adapter));
    }

    @Test
    void blueprintGenerationReusesDeterministicGenerators() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);
        Path model = tempDir.resolve(".hive/model/order.yml");
        Files.createDirectories(model.getParent());
        Files.writeString(model, """
                version: 1

                types:
                  - kind: id
                    name: OrderId
                    type: UUID
                  - kind: aggregate
                    name: Order
                    id: OrderId
                  - kind: outputPort
                    name: LoadOrder
                    methods:
                      - returnType: Optional<Order>
                        name: load
                        parameters:
                          - name: id
                            type: OrderId
                  - kind: adapter
                    name: OrderPersistence
                    ports:
                      - LoadOrder
                """);

        List<Path> created = new BlueprintGenerator().generateAll(tempDir, config, false);

        Path orderId = tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java");
        Path order = tempDir.resolve("src/main/java/com/example/app/domain/aggregates/Order.java");
        Path port = tempDir.resolve("src/main/java/com/example/app/application/ports/out/LoadOrderPort.java");
        Path adapter = tempDir.resolve("src/main/java/com/example/app/infrastructure/adapters/out/OrderPersistenceAdapter.java");
        assertEquals(4, created.size());
        assertTrue(Files.readString(port).contains("Optional<Order> load(OrderId id);"));
        assertTrue(Files.readString(adapter).contains("public Optional<Order> load(OrderId id)"));
        assertFilesCompile(List.of(orderId, order, port, adapter));
    }

    private void assertFilesCompile(List<Path> sourceFiles) throws Exception {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        Path output = tempDir.resolve("compiled");
        Files.createDirectories(output);
        List<String> arguments = new ArrayList<>();
        arguments.add("--release");
        arguments.add("21");
        arguments.add("-classpath");
        arguments.add(System.getProperty("java.class.path"));
        arguments.add("-d");
        arguments.add(output.toString());
        sourceFiles.stream()
                .map(Path::toString)
                .forEach(arguments::add);

        int result = compiler.run(
                null,
                null,
                null,
                arguments.toArray(String[]::new)
        );

        assertEquals(0, result);
    }

    private void assertPomContains(String artifactId) throws Exception {
        assertTrue(
                Files.readString(tempDir.resolve("pom.xml")).contains("<artifactId>" + artifactId + "</artifactId>"),
                "Missing Maven dependency " + artifactId
        );
    }

    private int occurrences(String text, String value) {
        int count = 0;
        int index = text.indexOf(value);
        while (index >= 0) {
            count++;
            index = text.indexOf(value, index + value.length());
        }
        return count;
    }
}
