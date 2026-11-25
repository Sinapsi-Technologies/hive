# hive-mcp

Spring Boot MCP server for Hive project operations, implemented with Spring AI.

The server uses Spring AI's MCP WebMVC starter and keeps the project root fixed
with `hive.project-root` (default: current directory). The first tools are
registered with `@McpTool`:

- `hive_init`
- `hive_create_value_object`
- `hive_create` for domain, application, infrastructure, module, and test artifacts

The MCP adapter is self-contained and calls its own shared operations through
the `io.sinapsi.hive.commons` package. Domain, application, and infrastructure
generation entry points are separated under `commons/domain`,
`commons/application`, and `commons/infrastructure`. Inbound adapters live under
`commons/infrastructure/inbound`; generic utilities live under `commons/service`.
The low-level `SourceGenerationSupport` component is shared only for safe file
writing and template rendering; context-specific generators own the public
operations.

Run it with:

```bash
mvn -pl hive-mcp -am package
java -jar hive-mcp/target/hive-mcp-0.1.0-SNAPSHOT.jar \
  --hive.project-root=/path/to/project
```

Tests cover tool invocation and the exact generated value object source. MCP
protocol negotiation and transport are delegated to Spring AI.

## Application views

Use `hive_create_record` with a name ending in `View`, for example `UserView` with
fields `id:UUID` and `name:String`. The generator places it in
`application.ports.in.views` (inside the selected module when provided) and adds:

```java
public static UserView of(UUID id, String name) {
    return new UserView(id, name);
}
```

Other record names remain in `commons`. Use views as plain output models in
`Result<UserView>`; input contracts must not import models from `application.services`.
Use cases without output data continue to return `Result<Unit>`.

## Secret configuration

`application.yml` imports `classpath:secret.yml` through Spring Config Data.
`secret.yml` holds environment-variable references for credentials, currently
`SONATYPE_GUIDE_TOKEN`. Set secret values in the runtime environment; the resource
contains no credential values.
