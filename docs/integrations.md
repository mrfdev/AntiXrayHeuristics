# Integrations

## Required Integration

### CoreProtect

CoreProtect is the only hard dependency declared in `plugin.yml`.

Verified source behavior:

- The plugin looks up the `CoreProtect` plugin from the Bukkit plugin manager.
- It calls `getAPI()` and reads `APIVersion()`.
- It refuses to enable if the detected CoreProtect API is below `11`.
- Current build metadata and status output target CoreProtect `25.0` with API `13`.
- `/xrayer debug` reports the detected CoreProtect version, API, jar path, data path, and hook status.

The supplied CoreProtect `25.0` JAR reports `APIVersion()` `13`. The hook's existing `getAPI()`, `APIVersion()`, and `isEnabled()` calls remain available, and its minimum API check already accepts `13`. X-ray Heuristics analyzes live Bukkit mining events; it does not use CoreProtect's lookup, rollback, or database APIs, so the typed lookup API does not require a migration here.

See the [Paper 26.3 compatibility verification](compatibility-paper-26.3.md) for the exact tested versions, build results and runtime checks.

## Optional Storage Integration

### MySQL

If `StorageType` is set to `MYSQL`, the plugin:

- initializes an Apache DBCP `BasicDataSource`
- uses the configured SQL host, port, database, username, and password
- creates or reuses an `Xrayers` table
- stores handled-player metadata there instead of using `data.json`

When `StorageType` remains `JSON`, no external database is required.

## Permissions Integration

The plugin uses the standard Bukkit permission checks. It does not require LuckPerms, Vault, or another specific permissions plugin for its own permission logic.

## Placeholder Integration

There is no verified PlaceholderAPI expansion registration in the current source. The plugin only uses internal placeholders for config-driven commands and vault/locale text.

## Paper / Java Compatibility Metadata

From the current build files and `plugin.yml`:

- Java target: `25`
- Gradle and compiler JDK: dynamically selected installed Oracle JDK `25`
- Automated tests and maintained server runtime: dynamically selected installed Oracle JDK `27`
- Paper compile target: `26.3.build.175-stable`
- Declared plugin `api-version`: `26.3`

The declared API floor matches Paper `26.3`; older servers should use the preserved baseline release. Gradle and all compilation use Oracle Java 25 with `--release 25`; every automated test and the maintained local server use Oracle Java 27. Build `175` is a verified stable release, with the compile/test API pinned to the same build and channel.

The exact compile coordinate, semantic version, build number, Java target, API floor, and CoreProtect target are packaged in `xrayheuristics/build-info.properties`. The namespaced resource path prevents collisions when this feature is later packaged with other CoreProtect add-ons. `/xrayer info`, `/xrayer help`, `/xrayer debug`, startup output, and generated `plugin.yml` consume that release metadata instead of hardcoded release values.

Paper's published dependencies supply Adventure `5.2.0`, Gson `2.14.0` and SLF4J `2.0.17`, with resolution recorded in `gradle.lockfile`. JSON records now deserialize through their existing constructor, preserving field names, null handling and mutable handling counts without reflective writes to final fields on Java 27. The persistence format and MySQL schema are unchanged.

## Future Unified Host

The maintained implementation separates the standalone `XRayHeuristicsPlugin` entry point from the reusable `XRayHeuristicsModule`. The module accepts a host `JavaPlugin`, an explicit feature data directory, and optionally a shared `CoreProtectHook`; see [architecture.md](architecture.md) for the compatibility contract and future embedding example.
