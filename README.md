# 1MB XRayHeuristics

`1MB XRayHeuristics` is a standalone 1MoreBlock heuristic Anti-XRay plugin for Paper. It integrates with CoreProtect and focuses on suspicious mining patterns, live suspicion sessions, and staff handling workflows instead of lookup-report style commands. The internal plugin id is `xrayheuristics`, and the canonical root command is `/xrayer`.

## Technical Overview

- Player-facing name: `1MB XRayHeuristics`
- Internal plugin name: `xrayheuristics`
- Main command: `/xrayer`
- Current release: `2.0.4-032-j25-26.3`
- Semantic version: `2.0.4`
- Build number: `032`
- Build target: Java `25`
- Release build JDK: Oracle JDK `25.0.4.1`
- Default unit-test JDK: Oracle JDK `25.0.4.1`
- Maintained test-server runtime: Oracle Java `27+35-2325`
- Paper compile target: `26.3.build.41-alpha`
- Declared `plugin.yml` api-version floor: `26.3`
- CoreProtect compile target: `25.0` with API `13`
- Minimum CoreProtect API accepted at runtime: `11`
- Release artifact: `build/libs/1MB-XRayHeuristics-v2.0.4-032-j25-26.3.jar`
- Plugin data directory: `plugins/1MB-XRayHeuristics/`
- Persistent storage backends: `JSON` or `MYSQL`
- Maintained Java namespace: `com.onemoreblock.coreprotectaddons.xrayheuristics`
- Runtime boundary: reusable `XRayHeuristicsModule` plus a standalone `XRayHeuristicsPlugin` adapter

## Documentation

- Canonical public docs: [docs.1moreblock.com/custom-server-plugins/xrayheuristics](https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/)
- Player guide: [docs/player-guide.md](docs/player-guide.md)
- Commands: [docs/commands.md](docs/commands.md)
- Permissions: [docs/permissions.md](docs/permissions.md)
- Placeholders: [docs/placeholders.md](docs/placeholders.md)
- Configuration: [docs/configuration.md](docs/configuration.md)
- Installation: [docs/installation.md](docs/installation.md)
- Integrations: [docs/integrations.md](docs/integrations.md)
- Architecture and future consolidation: [docs/architecture.md](docs/architecture.md)
- Troubleshooting: [docs/troubleshooting.md](docs/troubleshooting.md)
- Import manifest: [docs/plugin-docs.yml](docs/plugin-docs.yml)

## Commands

| Command | Purpose | Notes |
| --- | --- | --- |
| `/xrayer info` | Public introduction, quick start, and docs link. | No permission check in the current implementation. |
| `/xrayer help` | Command summary and plugin overview. | Also available from `/xrayer` with no arguments. |
| `/xrayer suspicion [player]` | Show the current live suspicion value. | Requires `xrayheuristics.use` or the legacy suspicion node. |
| `/xrayer debug` | Show build, server, config, storage, and CoreProtect hook status. | Admin-level command. |
| `/xrayer debug help` | Show available debug pages. | Admin-level command. |
| `/xrayer debug permissions` | Show permission nodes and defaults. | Admin-level command. |
| `/xrayer debug commands` | Show command syntax and usage notes. | Admin-level command. |
| `/xrayer debug config` | Show the supported live config values. | Admin-level command. |
| `/xrayer debug set <key> <value>` | Save a supported config key and reload plugin state. | Admin-level command. |
| `/xrayer reload` | Reload `config.yml`, `locale.yml`, and `weights.yml`. | Admin-level command. |
| `/xrayer resetsuspicion [player]` | Clear an in-memory suspicion session. | Admin-level command. |
| `/xrayer <player>` | Manually handle a player as an xrayer. | Admin-level command. |
| `/xrayer vault` | Open the handled-player vault GUI. | Player-only, admin-level command. |
| `/xrayer absolve <player>` | Return stored items and remove a vault entry. | The target must be online. |
| `/xrayer purge <player>` | Remove a vault entry without returning items. | Command path only works for online targets. |

Single-letter aliases are available for several subcommands: `x`, `v`, `r`, `rs`, `s`, `a`, and `p`. The explicit `/xrayer xrayer <player>` form also works, and `/xrayer xrayer` without a player only creates a dummy entry when `AddRandomDummyXrayerIfNoXrayerCommandParameters` is enabled.

## Permissions

| Permission | Default | Effective use |
| --- | --- | --- |
| `xrayheuristics.use` | `op` | Required for `/xrayer suspicion [player]`. |
| `xrayheuristics.admin` | `op` | Required for debug, reload, resets, manual handling, vault, absolve, purge, and the dummy-entry helper. |
| `xrayheuristics.notify` | `op` | Receives automatic handled-player warning messages. |
| `xrayheuristics.ignore` | `false` | Exempts a player from heuristic tracking. |

Legacy nodes are still accepted for compatibility: `AXH.Commands.*`, `AXH.Vault.Purge`, `AXH.XrayerWarning`, and `AXH.Ignore`.

Implementation note: the current code does not enforce a permission check for `/xrayer`, `/xrayer help`, or `/xrayer info`, even though older documentation historically grouped help together with `xrayheuristics.use`.

## Placeholders

The plugin does not register PlaceholderAPI expansions. The verified placeholders are internal placeholders used in config-driven commands and GUI/locale text:

- `{PlayerName}`
- `{TimesDetected}`
- `{HandledTimesAmount}`
- `{FirstTimeDetected}`
- `{LastSeenTime}`

See [docs/placeholders.md](docs/placeholders.md) for exact usage.

## Configuration and Data

`config.yml` is the active runtime configuration file. It is loaded and saved through Paper/Bukkit's comment-aware `YamlConfiguration` API with comment parsing enabled, and admin-edited values are preserved when defaults are synchronized back in.

Other generated files:

- `locale.yml`: generated language and GUI text values
- `weights.yml`: generated per-world weight-card data retained for compatibility
- `data.json`: the default JSON storage file when `StorageType` is `JSON`

Important runtime note: the current heuristic calculator reads its live suspicion weights from the top-level keys in `config.yml`. `weights.yml` is generated and reloaded, but it is not the active source for the main suspicion-weight checks in the current code.

## Architecture and Future Consolidation

The current jar remains a standalone plugin, but its maintained implementation is organized as an extractable X-ray Heuristics feature. `XRayHeuristicsPlugin` owns the standalone Paper lifecycle and compatibility paths; `XRayHeuristicsModule` owns feature state, listeners, CoreProtect access, commands, config, and handled-player persistence.

This boundary is intended for the planned unified CoreProtect Add-ons plugin. The future host can instantiate the module with a shared `JavaPlugin`, an explicit feature data directory, and a shared CoreProtect hook. Existing server-facing identifiers and data formats remain unchanged in this release. See [docs/architecture.md](docs/architecture.md) for the embedding contract and migration constraints.

## Ore Coverage

The runtime logic currently tracks these ore families:

- Coal: `COAL_ORE`, `DEEPSLATE_COAL_ORE`
- Iron: `IRON_ORE`, `DEEPSLATE_IRON_ORE`, `RAW_IRON_BLOCK`
- Copper: `COPPER_ORE`, `DEEPSLATE_COPPER_ORE`, `RAW_COPPER_BLOCK`
- Gold: `GOLD_ORE`, `DEEPSLATE_GOLD_ORE`
- Redstone: `REDSTONE_ORE`, `DEEPSLATE_REDSTONE_ORE`
- Emerald: `EMERALD_ORE`, `DEEPSLATE_EMERALD_ORE`
- Lapis: `LAPIS_ORE`, `DEEPSLATE_LAPIS_ORE`
- Diamond: `DIAMOND_ORE`, `DEEPSLATE_DIAMOND_ORE`
- Nether gold family: `NETHER_GOLD_ORE`, `GILDED_BLACKSTONE`
- Nether quartz: `NETHER_QUARTZ_ORE`
- Ancient debris: `ANCIENT_DEBRIS`

`RAW_GOLD_BLOCK` is intentionally not tracked.

## Build

Build with Gradle using the required Java `25.0.4.1` JDK. Copy the supplied `CoreProtect-25.0-26.3.jar` to the gitignored `servers/compile-support/CoreProtect-25.0.jar`, or pass `-PcoreProtectJar=/absolute/path/to/CoreProtect-25.0-26.3.jar` to select its location. Its manifest must report version `25.0` and its API must report `13`:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
gradle clean build printBuildConfig --warning-mode all
```

`./scripts/rebuild.sh` runs this canonical full rebuild with the same `JAVA_HOME` and `PATH`. Gradle compiles and runs the default test suite with Java 25; `options.release = 25` keeps the plugin bytecode compatible with Java 25 and later. Local toolchains come from `JAVA_HOME` and `JAVA27_HOME`, with automatic discovery and downloads disabled.

Run the full test suite on the maintained test server's Java version as well:

```bash
./scripts/test-java27.sh
```

This selects `/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home` through `JAVA27_HOME` for `gradle testJava27`. The build JVM and compiler remain JDK `25.0.4.1`. Reports are separate: `build/reports/tests/test/` and `build/reports/tests/testJava27/`.

Release metadata is centralized in `version.properties`. Update the semantic version and build number there exactly once for a release; repeated build and verification runs then reproduce the same artifact version instead of silently incrementing it.

`gradle check` includes `verifyReleaseMetadata`, which checks the packaged `plugin.yml`, generated `xrayheuristics/build-info.properties`, README, installation/integration docs, and public-docs manifest for stale version, build, Java, Paper API, channel, and CoreProtect metadata. A compatibility test reads the actual CoreProtect jar's plugin version and invokes `APIVersion()` to verify both against the release target. The build requires that jar and does not fall back to an older Maven dependency. CoreProtect is not bundled in the release artifact.

Official references used for the target are the [Paper project setup guide](https://docs.papermc.io/paper/dev/project-setup/) and [Paper 26.3 API Javadocs](https://jd.papermc.io/paper/26.3/).

The [Paper 26.3 compatibility verification](docs/compatibility-paper-26.3.md) records the current release's exact alpha build, checksums, tests, server smoke checks and remaining manual checks. The [CoreProtect verification](docs/compatibility-coreprotect-25.md) and [JDK 25/26 verification](docs/compatibility-jdk-25-26.md) remain historical records of the preserved baseline.

## Installation Summary

1. Install Paper `26.3` build `41` (`ALPHA`) with Java `27`, matching the maintained test instance.
2. Install CoreProtect before this plugin.
3. Place the built jar in your server's `plugins/` folder.
4. Start the server so `plugins/1MB-XRayHeuristics/` and its generated files are created.
5. Verify the load with `version xrayheuristics`, `/xrayer info`, and `/xrayer debug`.

Detailed steps are in [docs/installation.md](docs/installation.md).

## Integrations

- Required runtime dependency: CoreProtect
- Optional storage integration: MySQL
- No verified PlaceholderAPI expansion registration
- No required dependency on CMI, LuckPerms, Vault, or PlaceholderAPI for the core plugin behavior

See [docs/integrations.md](docs/integrations.md) for details.

## Credits

- Original plugin authors: `__Mithrandir__` and `Greymagic27`
- 1MoreBlock maintenance, packaging, compatibility work, and testing: [mrfloris](https://github.com/mrfloris)
- Thanks to everyone who contributed testing and follow-up fixes across the 1MoreBlock Anti-XRay plugin line
