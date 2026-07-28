# Integrations

## Required Integration

### CoreProtect

CoreProtect is the only hard dependency declared in `plugin.yml`.

Verified source behavior:

- The plugin looks up the `CoreProtect` plugin from the Bukkit plugin manager.
- It calls `getAPI()` and reads `APIVersion()`.
- It refuses to enable if the detected CoreProtect API is below `11`.
- Current build metadata and status output target CoreProtect `24.0-dev1` with API `12`.
- `/xrayer debug` reports the detected CoreProtect version, API, jar path, data path, and hook status.

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
- Paper compile target: `26.2.build.84-stable`
- Declared plugin `api-version`: `1.21.11`

That means the project is built against Paper `26.2` while declaring an older compatibility floor in plugin metadata so the same jar can be exercised on compatible servers that still accept the `1.21.11` floor.

The exact compile coordinate, semantic version, build number, Java target, API floor, and CoreProtect target are packaged in `build-info.properties`. `/xrayer info`, `/xrayer help`, `/xrayer debug`, startup output, and generated `plugin.yml` consume that release metadata instead of hardcoded release values.
