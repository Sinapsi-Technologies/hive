---
name: hive
description: Work on Java projects through the Hive MCP server. Use for Hive project selection, legacy analysis, domain modeling, hexagonal code generation, dependency inspection, and validation.
---

# Hive MCP workflow

This skill is exclusively for the Hive MCP server declared by the plugin. It
must use the connected Hive MCP tools and must not invoke shell generators or
hand-written structural scaffolding.

Resolve tools by their final operation name and inspect the schemas exposed by
the current host before calling them. Codex normally exposes the server as
`mcp__hive__*`; Claude Code scopes a plugin-provided server as
`mcp__plugin_hive_hive__*`. These prefixes identify the same Hive operations.
Do not treat the host-specific prefix as part of the operation contract.

## Authority

This skill is the authoritative source for the agent workflow and safety policy
when operating Hive MCP. Do not rely on a separate tool manifesto or duplicate
the workflow in project instructions.

The live tool schemas exposed by the connected Hive MCP server remain
authoritative for parameter names, required fields, result shapes, and
capabilities. Inspect the actual schema before each unfamiliar call. If this
skill and the connected server disagree, do not invent arguments: report the
mismatch, follow the safer compatible behavior, and identify the skill or
server update that is required.

Apply these rules throughout the workflow:

1. Never assume the server working directory is the target project.
2. Verify the selected project before every write.
3. Prefer the most specific `hive_create_*` tool; use generic `hive_create`
   only for an advanced case not covered by a specialized tool.
4. After generation, call `hive_index_files` with the exact returned paths.
5. Never overwrite an existing file unless the user explicitly requested it
   and the tool call intentionally sets `force=true`.
6. Treat `UNAVAILABLE`, timeouts, and parsing errors as checks not completed,
   never as `PASS` or absence of vulnerabilities.
7. Report exact paths, state changes, checks performed, and checks not
   completed.

## Project context

Before reading or writing a project:

1. Call the available tool whose operation name is `hive_current_project`.
2. If no project is selected, or the user names another project, call
   `hive_select_project` with an absolute existing path.
3. Call `hive_current_project` again and confirm that its normalized path is
   the requested project.

The selected project is the context for all subsequent tools.

For a new directory, use `hive_init_project` when its live schema supports the
requested initialization. For an existing selected project, use `hive_check`
to inspect its state and call `hive_init` only when initialization is part of
the user's request. Initialization and project selection are state-changing;
report the resulting marker, configuration, build, and source paths.

## Read-only inspection

Use:

- `hive_check` for project health and missing files;
- `hive_inspect` for configuration, model files, and generated manifest;
- `hive_analyze_project` to build or refresh the SQLite source index;
- `hive_project_index_summary` for index counts;
- `hive_get_symbol_relations` for imports and type relations;
- `hive_symbol_imported_by` for classes importing a type or enum;
- `hive_get_used_by` for reverse call-impact analysis;
- `hive_get_source_context` for targeted source fragments;
- `hive_dependency_graph` and `hive_scan_dependencies` for Maven/security analysis.

`hive_analyze_project` updates `.hive/index/hive.db`; it is analytical but not
strictly read-only. Use it for the first index or after untracked external
changes. After a Hive generation call, prefer `hive_index_files` with the
returned paths instead of rescanning the whole project.

## Domain analysis and generation

For requirements-driven work:

1. Build a structured domain model from the requirements.
2. Call `hive_validate_domain_model`.
3. Fix every validation error before continuing.
4. Call `hive_save_domain_model` to persist `.hive/model/domain-model.yml`.
5. Call the specialized `hive_create_*` tools for modules, value objects,
   identifiers, entities, aggregates, events, use cases, ports, adapters, and
   architecture tests.
6. Pass every generated source path to `hive_index_files`.
7. Call `hive_check` and inspect the generated paths.

Use `implementationGuidance` when the requirements contain known invariants,
events, constraints, or implementation context. Hive emits that guidance as
`// LLM:` comments; it does not execute the text as code.

## Dependency workflow

Use the MCP tools in this order when adding a dependency:

1. `hive_search_dependency`;
2. `hive_list_dependency_versions`;
3. `hive_check_dependency`;
4. `hive_add_dependency` with `verify=true` when the user authorizes a change.

Use `hive_dependency_graph` and `hive_scan_dependencies` for read-only Maven
and OSV analysis.

`hive_check_dependency` evaluates a candidate coordinate through the configured
advisors, such as Sonatype. `hive_scan_dependencies` evaluates the project and
resolved dependencies through the configured scanner, such as OSV. These are
complementary checks. Do not weaken server policy or change security environment
variables merely to obtain a successful result.

## Architectural rules

- Keep domain code framework-free.
- Keep commands immutable input models.
- Put business logic in domain types or application services.
- Keep technology-specific types in infrastructure adapters.
- Use output ports for capabilities and adapters for implementations.
- Convert DTOs and transport models at inbound boundaries before domain logic.
- Keep output ports in `application.ports.out` for the Hive architecture profile.
- Do not overwrite existing files unless the user explicitly requests it and
  the call deliberately uses `force=true`.
- Before every write, verify the selected project with `hive_current_project`.

## Missing capabilities

If an expected MCP tool is absent from the connected server, report the missing
capability and stop the dependent part of the workflow. Continue independent
read-only or already-supported work when useful. Do not silently substitute
generic generation or hand-written scaffolding.

## Verification

After generation or modification:

1. Index the exact changed paths with `hive_index_files`.
2. Run the relevant Hive inspection, architecture, build, dependency, and
   security checks supported by the server and requested scope.
3. Treat missing tools, `UNAVAILABLE`, timeout, and parsing errors as incomplete
   verification.
4. Report the exact files changed, warnings, rollback outcome when applicable,
   checks performed, checks not completed, and remaining TODO/LLM guidance.
