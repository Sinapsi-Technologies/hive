package com.example.todo.configurations;

import com.example.todo.application.ports.in.AddTodoItemUseCase;
import com.example.todo.application.ports.in.CompleteTodoItemUseCase;
import com.example.todo.application.ports.in.CreateTodoListUseCase;
import com.example.todo.application.ports.out.TodoListRepositoryPort;
import com.example.todo.application.services.AddTodoItemService;
import com.example.todo.application.services.CompleteTodoItemService;
import com.example.todo.application.services.CreateTodoListService;
import com.example.todo.infrastructure.adapters.out.InMemoryTodoListRepositoryAdapter;
import io.sinapsi.hive.adapters.clock.SystemClockAdapter;
import io.sinapsi.hive.adapters.event.InMemoryEventPublisherAdapter;
import io.sinapsi.hive.core.event.DomainEvent;
import io.sinapsi.hive.ports.ClockPort;
import io.sinapsi.hive.ports.PublishEventPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Composition root. It wires the application services to their outbound adapters — the only place
 * that knows about both sides. The technical ports (clock, event publishing) reuse the default
 * Hive adapters from {@code hive-adapter}.
 */
@Configuration
public class TodoConfiguration {

    @Bean
    ClockPort clockPort() {
        return new SystemClockAdapter();
    }

    @Bean
    PublishEventPort<DomainEvent> eventPublisher() {
        return new InMemoryEventPublisherAdapter<>();
    }

    @Bean
    TodoListRepositoryPort todoListRepository() {
        return new InMemoryTodoListRepositoryAdapter();
    }

    @Bean
    CreateTodoListUseCase createTodoListUseCase(TodoListRepositoryPort repository) {
        return new CreateTodoListService(repository);
    }

    @Bean
    AddTodoItemUseCase addTodoItemUseCase(
            TodoListRepositoryPort repository,
            ClockPort clock,
            PublishEventPort<DomainEvent> events
    ) {
        return new AddTodoItemService(repository, clock, events);
    }

    @Bean
    CompleteTodoItemUseCase completeTodoItemUseCase(
            TodoListRepositoryPort repository,
            ClockPort clock,
            PublishEventPort<DomainEvent> events
    ) {
        return new CompleteTodoItemService(repository, clock, events);
    }
}
