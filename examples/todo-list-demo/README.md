# Todo List Demo

A small but complete hexagonal slice built with the Hive toolkit — scaffolded with the `hive` CLI
and filled in following the Hive conventions. It shows a full ports-&-adapters stack around a real
aggregate.

## What it demonstrates
 
| Building block | Where |
| --- | --- |
| **Aggregate root** (records & publishes domain events) | `domain/aggregates/TodoList` |
| **Entity** (identity + mutable lifecycle) | `domain/entities/TodoItem` |
| **Value objects** | `domain/valueobjects/{TodoListId, TodoItemId, TodoTitle, TodoStatus}` |
| **Domain events** | `domain/events/{TodoItemAdded, TodoItemCompleted}` |
| **Use cases + commands** (validated) | `application/ports/in` + `…/commands` |
| **Command factories** (validation pipeline) | `application/ports/in/commands/*Factory` |
| **Outbound port + adapter** | `application/ports/out/TodoListRepositoryPort` + `infrastructure/adapters/out/InMemoryTodoListRepositoryAdapter` |
| **Technical ports reused from Hive** | `ClockPort` + `PublishEventPort` wired to `hive-adapter` defaults |
| **Inbound adapter** (controller) | `infrastructure/adapters/in/TodoListController` |
| **Composition root** | `configurations/TodoConfiguration` |

The domain stays framework-free; validation annotations live on the commands; the controller turns
HTTP requests into validated commands via the factories and delegates to the input ports. The
`ArchitectureTest` enforces all of this with the Hive ArchUnit rules.

## Flow

1. A request hits `TodoListController`.
2. The matching command factory builds and validates the command.
3. The use-case service loads the aggregate, mutates it, saves it, and publishes the domain events
   it recorded.

## Endpoints

```text
POST /todo-lists                                          create a list           {"name": "..."}
POST /todo-lists/{id}/items                               add an item             {"title": "..."}
POST /todo-lists/{id}/items/{itemId}/completion           complete an item
```

## Run

```bash
mvn -pl examples/todo-list-demo -am verify     # compile + tests + architecture rules
```
