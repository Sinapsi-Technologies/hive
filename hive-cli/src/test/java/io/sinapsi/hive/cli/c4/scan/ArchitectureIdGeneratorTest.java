package io.sinapsi.hive.cli.c4.scan;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ArchitectureIdGeneratorTest {
    @Test
    void lowerCamelCasesClassNames() {
        ArchitectureIdGenerator ids = new ArchitectureIdGenerator();
        assertEquals("todoController", ids.generate("TodoController"));
        assertEquals("createTodoUseCase", ids.generate("CreateTodoUseCase"));
        assertEquals("inMemoryTodoListRepositoryAdapter",
                ids.generate("InMemoryTodoListRepositoryAdapter"));
    }

    @Test
    void deduplicatesWithNumericSuffix() {
        ArchitectureIdGenerator ids = new ArchitectureIdGenerator();
        assertEquals("todoController", ids.generate("TodoController"));
        assertEquals("todoController2", ids.generate("TodoController"));
        assertEquals("todoController3", ids.generate("TodoController"));
    }

    @Test
    void stripsInvalidCharacters() {
        ArchitectureIdGenerator ids = new ArchitectureIdGenerator();
        assertEquals("myType", ids.generate("My-Type"));
    }
}
