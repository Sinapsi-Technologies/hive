package io.sinapsi.hive.core;

import io.sinapsi.hive.core.command.Command;
import io.sinapsi.hive.core.usecase.UseCase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UseCaseTest {

    @Test
    void useCaseCanAcceptCommandInput() {
        UseCase<TestCommand, Integer> length = command -> command.value().length();

        assertEquals(4, length.handle(new TestCommand("hive")));
    }

    private record TestCommand(String value) implements Command {}

}
