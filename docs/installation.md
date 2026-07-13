# Installation

## Requirements

- Paper server
- Java `25`
- CoreProtect installed before `1MB XRayHeuristics`
- A build of this plugin produced from the current Gradle setup

Verified build metadata from this repository:

- Paper compile target: `26.2.build.60-beta`
- Declared `plugin.yml` api-version floor: `1.21.11`
- CoreProtect compile target: `24.0-dev1`
- Minimum runtime CoreProtect API accepted by code: `11`

## Build From Source

Run:

```bash
gradle build printBuildConfig
```

The jar name pattern is:

```text
build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.2.jar
```

Each successful build increments `version.properties` and leaves older jars in `build/libs/`.

## Install On A Server

1. Stop the server.
2. Make sure CoreProtect is already installed.
3. Place the newest `1MB-XRayHeuristics` jar in the server's `plugins/` folder.
4. Start the server.
5. Confirm the plugin creates `plugins/1MB-XRayHeuristics/`.

## First-Run Output And Files

The plugin creates or maintains:

- `plugins/1MB-XRayHeuristics/config.yml`
- `plugins/1MB-XRayHeuristics/locale.yml`
- `plugins/1MB-XRayHeuristics/weights.yml`
- `plugins/1MB-XRayHeuristics/data.json` when `StorageType: JSON`

## Recommended Readiness Checks

After startup, verify:

- `version xrayheuristics`
- `/xrayer info`
- `/xrayer help`
- `/xrayer debug`

`/xrayer debug` is the best built-in hook check because it reports:

- runtime Java version
- server version
- CoreProtect hook status
- CoreProtect version and API
- config, locale, and weights file paths
- current storage mode

## Updates

1. Stop the server.
2. Replace the old jar with the new jar.
3. Keep your existing `plugins/1MB-XRayHeuristics/` data directory.
4. Start the server.
5. Run `/xrayer info` and `/xrayer debug` again.

If you changed storage settings, restart fully instead of relying on `/xrayer reload`.
