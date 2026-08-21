package io.sinapsi.hive.cli.service.inbound;

import io.sinapsi.hive.cli.model.HiveConfig;
import io.sinapsi.hive.cli.service.FileScaffolder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InboundAdapterGeneratorTest {
    @TempDir
    Path tempDir;

    private final FileScaffolder scaffolder = new FileScaffolder();
    private final InboundAdapterGenerator generator = new InboundAdapterGenerator();

    @Test
    void createRestInboundAdapterUsesCommandFactoriesAndTransportRecords() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);
        scaffolder.createIdentifier(tempDir, config, null, "OrderId", "UUID", false);
        scaffolder.createUseCase(tempDir, config, null, "CreateOrder", false, true, List.of("customerId:String"));
        scaffolder.createUseCase(tempDir, config, null, "ConfirmOrder", false, true, List.of("orderId:OrderId"));

        List<Path> created = generator.generate(
                tempDir,
                config,
                new InboundGenerationRequest(
                        InboundAdapterType.REST,
                        null,
                        "Order",
                        List.of(
                                InboundOperation.parseRest("POST /orders -> CreateOrder"),
                                InboundOperation.parseRest("POST /orders/{id}/confirm -> ConfirmOrder")
                        ),
                        false
                )
        );

        Path adapterRoot = tempDir.resolve("src/main/java/com/example/app/infrastructure/adapters/in/rest");
        Path controller = adapterRoot.resolve("OrderController.java");
        Path createRequest = adapterRoot.resolve("CreateOrderRequest.java");
        Path confirmRequest = adapterRoot.resolve("ConfirmOrderRequest.java");
        String source = Files.readString(controller);
        assertEquals(3, created.size());
        assertTrue(source.contains("POST /orders"));
        assertTrue(source.contains("private final CreateOrderUseCase.CreateOrderCommand.Factory createOrderCommandFactory;"));
        assertTrue(source.contains("CreateOrderUseCase.CreateOrderCommand command = createOrderCommandFactory.create(input.customerId());"));
        assertTrue(source.contains("return createOrderUseCase.handle(command);"));
        assertTrue(Files.notExists(tempDir.resolve("src/main/java/com/example/app/application/ports/in/commands/CreateOrderCommand.java")));
        assertTrue(Files.notExists(tempDir.resolve("src/main/java/com/example/app/application/ports/in/commands/CreateOrderCommandFactory.java")));
        assertTrue(Files.readString(createRequest).contains("record CreateOrderRequest(String customerId)"));
        assertTrue(Files.readString(confirmRequest).contains("import com.example.app.domain.valueobjects.OrderId;"));
        assertTrue(Files.readString(confirmRequest).contains("record ConfirmOrderRequest(OrderId orderId)"));
        assertFilesCompile(List.of(
                tempDir.resolve("src/main/java/com/example/app/domain/valueobjects/OrderId.java"),
                tempDir.resolve("src/main/java/com/example/app/application/ports/in/CreateOrderUseCase.java"),
                tempDir.resolve("src/main/java/com/example/app/application/ports/in/ConfirmOrderUseCase.java"),
                controller,
                createRequest,
                confirmRequest
        ));
    }

    @Test
    void createMcpInboundAdapterKeepsMcpTypesOutOfGeneratedCode() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);
        scaffolder.createUseCase(tempDir, config, null, "CreateOrder", false, true, List.of("customerId:String"));

        generator.generate(
                tempDir,
                config,
                new InboundGenerationRequest(
                        InboundAdapterType.MCP,
                        null,
                        "Order",
                        List.of(InboundOperation.useCaseOnly("CreateOrder")),
                        false
                )
        );

        Path tool = tempDir.resolve("src/main/java/com/example/app/infrastructure/adapters/in/mcp/OrderTool.java");
        String source = Files.readString(tool);
        assertTrue(source.contains("public final class OrderTool"));
        assertTrue(source.contains("CreateOrderArguments input"));
        assertTrue(source.contains("CreateOrderUseCase.CreateOrderCommand command = createOrderCommandFactory.create(input.customerId());"));
        assertTrue(!source.contains("Mcp"));
    }

    @Test
    void createListenerInboundAdapterUsesNestedCommandFactory() throws Exception {
        assertInboundAdapterUsesNestedCommandFactory(
                InboundAdapterType.LISTENER,
                "OrderCreated",
                "OrderCreatedListener.java",
                "CreateOrderMessage input"
        );
    }

    @Test
    void createSchedulerInboundAdapterUsesNestedCommandFactory() throws Exception {
        assertInboundAdapterUsesNestedCommandFactory(
                InboundAdapterType.SCHEDULER,
                "OrderExpiration",
                "OrderExpirationScheduler.java",
                "CreateOrderTrigger input"
        );
    }

    @Test
    void createListenerFailsWhenUseCaseDoesNotExist() throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> generator.generate(
                        tempDir,
                        config,
                        new InboundGenerationRequest(
                                InboundAdapterType.LISTENER,
                                null,
                                "OrderCreated",
                                List.of(InboundOperation.useCaseOnly("ProcessOrder")),
                                false
                        )
                )
        );

        assertTrue(exception.getMessage().contains("UseCase not found: ProcessOrderUseCase"));
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

        int result = compiler.run(null, null, null, arguments.toArray(String[]::new));
        assertEquals(0, result);
    }

    private void assertInboundAdapterUsesNestedCommandFactory(
            InboundAdapterType type,
            String adapterName,
            String generatedFile,
            String inputSignature
    ) throws Exception {
        HiveConfig config = HiveConfig.defaults();
        scaffolder.init(tempDir, config, false, false);
        scaffolder.createUseCase(tempDir, config, null, "CreateOrder", false, true, List.of("customerId:String"));

        generator.generate(
                tempDir,
                config,
                new InboundGenerationRequest(
                        type,
                        null,
                        adapterName,
                        List.of(InboundOperation.useCaseOnly("CreateOrder")),
                        false
                )
        );

        Path adapter = tempDir.resolve("src/main/java/com/example/app/infrastructure/adapters/in")
                .resolve(type.directory())
                .resolve(generatedFile);
        String source = Files.readString(adapter);
        assertTrue(source.contains("private final CreateOrderUseCase.CreateOrderCommand.Factory createOrderCommandFactory;"));
        assertTrue(source.contains(inputSignature));
        assertTrue(source.contains("CreateOrderUseCase.CreateOrderCommand command = createOrderCommandFactory.create(input.customerId());"));
        assertTrue(source.contains("return createOrderUseCase.handle(command);"));
    }
}
