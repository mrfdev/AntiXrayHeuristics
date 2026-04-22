# 1MB-XRayHeuristics

`1MB-XRayHeuristics` is a CoreProtect-backed heuristic Anti-XRay add-on for 1MoreBlock. It focuses on suspicious mining patterns instead of lookup reports, runs as the plugin `xrayheuristics`, and exposes a single root command: `/xrayer`.

This branch is aligned to:

- Paper `26.1.2`
- Java `25`
- CoreProtect `23.4`
- CoreProtect API `11`
- Artifact pattern `build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar`

The canonical command is `/xrayer`. Legacy `AXH.*` permission nodes are still accepted, but `/axh` and `/AntiXrayHeuristics` are no longer exposed as commands.

## Compatibility

- Server engine: Paper `26.1.2`
- Java runtime for building and running: Java `25`
- Required dependency: CoreProtect `23.4` with API `11`
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
gradle build
```

That writes the next jar to the same pattern:

```text
build/libs/1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar
```

After a successful jar build, `version.properties` is updated automatically, so the next local build will produce the next build number and keep the older jar in `build/libs/`.

## Install / Test Notes

1. Run a Paper `26.1.2` server on Java `25`.
2. Put `CoreProtect-23.4b.jar` in the server `plugins/` folder.
3. Put the latest `1MB-XRayHeuristics-v2.0.0-0xx-j25-26.1.2.jar` in the same `plugins/` folder.
4. Start the server and confirm `/ver xrayheuristics` reports the expected version and CoreProtect target text.

The plugin stores its own files in:

```text
plugins/1MB-XRayHeuristics/
```

The local `servers/` directory is ignored by Git. In this repo it is only used as a local development/test server location. If `servers/Server-Two-Paper-26.1.2/plugins/CoreProtect-23.4b.jar` exists, Gradle uses that local jar as the compile-only CoreProtect reference; otherwise it falls back to the published CoreProtect dependency.

## Notes

- The plugin now exposes `/xrayer debug`, `/xrayer debug permissions`, `/xrayer debug commands`, `/xrayer debug config`, and `/xrayer debug set <key> <value>`.
- The live suspicion threshold is now configurable through `SuspicionThreshold` and through `/xrayer debug set suspicion-threshold <value>`.
- The heuristic tracker now covers raw iron blocks, raw copper blocks, gilded blackstone, ancient debris, and the deepslate ore families.
- The plugin now requires CoreProtect at startup and reports whether it successfully hooked into CoreProtect `23.4` API `11`.
- `CleansePlayerItems` and `NullifySuspicionAfterPunish` are the corrected config names, while the old typo keys are still read for backwards compatibility.
- `/ver xrayheuristics` now reports the `2.0.0-0xx-j25-26.1.2` version line and a description that references CoreProtect `23.4` instead of the older `1.2.6` text.

## Credits

- Original plugin authors: __Mithrandir__ and Greymagic27
- 1MoreBlock maintenance, packaging, compatibility work, and testing: [mrfloris](https://github.com/mrfloris)
- Thanks to everyone who contributed context, testing, and follow-up fixes around the 1MB Anti-XRay plugin line
