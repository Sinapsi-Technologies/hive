---
description: Bootstrap a new Hive project with the deterministic CLI and verify it builds
argument-hint: "[basePackage]"
---

You are bootstrapping a new **Hive** hexagonal-architecture Java project in the current directory, using the `hive` CLI. Be deterministic: let the CLI create structure, then only edit configuration requested by the user.

## 1. Resolve the `hive` CLI
- Try `hive --help`. If it's not on the PATH, look for a `./hive` launcher in this directory or a parent.
- If neither is available **and** you are inside the Hive toolkit repository, build it once: `mvn -q -pl hive-cli -am package`, then use `./hive`.
- If you still can't run it, tell the user how to install the CLI (download `hive-cli.jar` from a GitHub Release, or build from source) and stop.

## 2. Initialize
- Run `hive init --readme --json`.
- If the user passed a base package in `$ARGUMENTS`, set `basePackage:` in the generated `hive.yml` to that value (otherwise keep the default `com.example.app`).
- Read the JSON result and keep the exact created paths for the final summary.

## 3. Scaffold a starter slice (so the project is non-empty and verifiable)
- `hive create usecase Ping --factory --json`
- `hive create archtest --json`

Read the `created` array from the `--json` output to confirm the exact files. The starter use case gives the ArchUnit rules real classes to check.

## 4. Verify
- Run `mvn -q verify` and report the result.
- If the build fails because the `io.sinapsi.hive` dependencies can't be resolved, the libraries aren't published yet — tell the user to either use a released version or run `mvn -q install` in the Hive repo first. Fix anything else and retry until green.

## Rules
- Never create structural files by hand — always go through `hive create …`.
- Use `hive create vo` for value objects.
- Don't move generated files out of their packages; the ArchUnit rules enforce the layout.
- When done, summarize what was created (from the JSON) and how to add features next (the `hive` skill, or `hive create usecase|port|adapter|module`).
