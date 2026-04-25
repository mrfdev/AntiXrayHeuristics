# 1MB-XRayHeuristics

`1MB-XRayHeuristics` is a CoreProtect-backed heuristic Anti-XRay add-on for 1MoreBlock. It focuses on suspicious mining patterns instead of lookup reports, runs as the plugin `xrayheuristics`, and exposes a single root command: `/xrayer`.

This branch is aligned to:

- Paper API compile target `26.1.2`
- Declared `plugin.yml` api-version floor `1.21.11`
- Java `25`
- CoreProtect `24.0-dev1`
- CoreProtect API `12`
- Artifact pattern `build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar`

The canonical command is `/xrayer`. Legacy `AXH.*` permission nodes are still accepted, but `/axh` and `/AntiXrayHeuristics` are no longer exposed as commands.

## Compatibility

- Server engine targets: Paper `1.21.11` and Paper `26.1.2`
- Compiled against: Paper API `26.1.2`
- Declared in `plugin.yml`: `api-version: 1.21.11`
- Java runtime for building and running: Java `25`
- Required dependency target: CoreProtect `24.0-dev1` with API `12`
- Minimum accepted CoreProtect API at runtime: `11`
- Internal plugin name for `/ver`: `xrayheuristics`
- Current plugin version: `2.0.0`
- Build numbering: each successful `gradle build` writes a new jar name and increments `version.properties`

Tracked material coverage includes:

- `COAL_ORE`, `DEEPSLATE_COAL_ORE`
- `IRON_ORE`, `DEEPSLATE_IRON_ORE`, `RAW_IRON_BLOCK`
- `COPPER_ORE`, `DEEPSLATE_COPPER_ORE`, `RAW_COPPER_BLOCK`
- `GOLD_ORE`, `DEEPSLATE_GOLD_ORE`
- `REDSTONE_ORE`, `DEEPSLATE_REDSTONE_ORE`
- `EMERALD_ORE`, `DEEPSLATE_EMERALD_ORE`
- `LAPIS_ORE`, `DEEPSLATE_LAPIS_ORE`
- `DIAMOND_ORE`, `DEEPSLATE_DIAMOND_ORE`
- `NETHER_GOLD_ORE`, `GILDED_BLACKSTONE`
- `NETHER_QUARTZ_ORE`
- `ANCIENT_DEBRIS`

`RAW_GOLD_BLOCK` is intentionally not tracked.

## Commands

| Command | Description | Example |
| --- | --- | --- |
| `/xrayer help` | Shows the main help page, command summary, and placeholders. | `/xrayer help` |
| `/xrayer debug` | Shows plugin, server, build, storage, and CoreProtect hook status. | `/xrayer debug` |
| `/xrayer debug help` | Lists the available debug pages. | `/xrayer debug help` |
| `/xrayer debug permissions` | Lists the permission nodes and defaults. | `/xrayer debug permissions` |
| `/xrayer debug commands` | Lists the command syntax and usage notes. | `/xrayer debug commands` |
| `/xrayer debug config` | Shows the live config values exposed through debug. | `/xrayer debug config` |
| `/xrayer debug set <key> <value>` | Updates a supported config value, saves `config.yml`, and reloads the plugin state. | `/xrayer debug set suspicion-threshold 125` |
| `/xrayer reload` | Reloads `config.yml`, `locale.yml`, `weights.yml`, and the CoreProtect hook state. | `/xrayer reload` |
| `/xrayer suspicion [player]` | Shows the current live suspicion value for yourself or another tracked player. | `/xrayer suspicion mrfloris` |
| `/xrayer resetsuspicion [player]` | Clears a live suspicion session. | `/xrayer resetsuspicion mrfloris` |
| `/xrayer <player>` | Manually handles a player as xrayer. | `/xrayer mrfloris` |
| `/xrayer vault` | Opens the handled-player vault GUI. | `/xrayer vault` |
| `/xrayer absolve <player>` | Returns stored items and removes the player from the vault. | `/xrayer absolve mrfloris` |
| `/xrayer purge <player>` | Removes the player from the vault without returning items. | `/xrayer purge mrfloris` |

## Permissions

| Permission | Default | Description |
| --- | --- | --- |
| `xrayheuristics.use` | `op` | Allows help output and suspicion lookups. |
| `xrayheuristics.admin` | `op` | Allows reload, debug pages, vault actions, manual player handling, suspicion resets, and vault GUI admin actions. |
| `xrayheuristics.notify` | `op` | Receives the automatic “handled xrayer” staff warning messages. |
| `xrayheuristics.ignore` | `false` | Bypasses heuristic tracking for that player. |
| `AXH.Commands.*`, `AXH.Vault.Purge`, `AXH.XrayerWarning`, `AXH.Ignore` | legacy | Older permission nodes are still supported for backward compatibility. |

## Placeholders

These placeholders are currently used by configurable command strings:

- `{PlayerName}`
- `{TimesDetected}`

`CommandsExecutedOnXrayerDetected` supports both placeholders.

`CommandsExecutedOnPlayerAbsolved` supports `{PlayerName}`.

## Command Examples

- `/xrayer help`
- `/xrayer debug`
- `/xrayer debug permissions`
- `/xrayer debug commands`
- `/xrayer debug config`
- `/xrayer debug set debug-verbose-mining-session true`
- `/xrayer debug set track-worlds general,wild,nether`
- `/xrayer debug set nether-gold-weight 7.5`
- `/xrayer debug set suspicion-threshold 125`
- `/xrayer suspicion`
- `/xrayer suspicion mrfloris`
- `/xrayer resetsuspicion mrfloris`
- `/xrayer mrfloris`
- `/xrayer vault`
- `/xrayer absolve mrfloris`
- `/xrayer purge mrfloris`

## Build

Use Gradle with Java `25`:

```bash
gradle build printBuildConfig
```

That writes the next jar to the same pattern:

```text
build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar
```

After a successful jar build, `version.properties` is updated automatically, so the next local build will produce the next build number and keep the older jar in `build/libs/`.

## Test Runner

Use the centralized Paper runner from:

```text
/Users/floris/Projects/Codex/servers/run-test-server
```

Example foreground test runs:

```bash
/Users/floris/Projects/Codex/servers/run-test-server --paper 1.21.11 --plugin build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar --foreground
/Users/floris/Projects/Codex/servers/run-test-server --paper 26.1.2 --plugin build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar --foreground
```

The same jar is compiled against Paper `26.1.2`, but it declares `api-version: 1.21.11` so it can be exercised on both Paper versions.

The plugin stores its own files in:

```text
plugins/1MB-XRayHeuristics/
```

This repo should not rely on a local `./servers/` folder for building or testing. The path is ignored by Git, but the intended test flow is the centralized runner above.

If `/Users/floris/Projects/Codex/servers/cache/Paper-26.1.2/plugins/CoreProtect-24.0-dev1.jar` exists, Gradle uses that centralized jar for compile/test classpaths. Otherwise it falls back to the published CoreProtect `23.4` dependency coordinates for local build resilience.

## Notes

- The plugin now exposes `/xrayer debug`, `/xrayer debug permissions`, `/xrayer debug commands`, `/xrayer debug config`, and `/xrayer debug set <key> <value>`.
- The live suspicion threshold is now configurable through `SuspicionThreshold` and through `/xrayer debug set suspicion-threshold <value>`.
- The heuristic tracker now covers raw iron blocks, raw copper blocks, gilded blackstone, ancient debris, and the deepslate ore families.
- The plugin now requires CoreProtect API `11` or newer at startup and reports the exact CoreProtect version and API level it successfully hooked at runtime.
- `/xrayer help`, `/xrayer debug`, `plugin.yml`, and `printBuildConfig` now distinguish between the Paper `26.1.2` compile target and the declared `1.21.11` api-version floor.
- `CleansePlayerItems` and `NullifySuspicionAfterPunish` are the corrected config names, while the old typo keys are still read for backwards compatibility.
- `/ver xrayheuristics` now reports the `2.0.0-0xx-j25-26.1.2` version line and a description that references the current CoreProtect `24.0-dev1` / API `12` target.

## Credits

- Original plugin authors: __Mithrandir__ and Greymagic27
- 1MoreBlock maintenance, packaging, compatibility work, and testing: [mrfloris](https://github.com/mrfloris)
- Thanks to everyone who contributed context, testing, and follow-up fixes around the 1MB Anti-XRay plugin line
