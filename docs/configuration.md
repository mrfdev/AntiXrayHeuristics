# Configuration

Main runtime file: `plugins/1MB-XRayHeuristics/config.yml`

The active heuristic logic reads its live settings from `config.yml`. The plugin also generates `locale.yml`, `weights.yml`, and either `data.json` or MySQL records depending on the storage mode.

## Reload Behavior

- Manual `config.yml` edits require `/xrayer reload` or a full restart unless noted otherwise.
- `/xrayer debug set <key> <value>` saves and reloads only the supported live-edit keys.
- Storage backend changes require a full restart.

## Runtime Monitoring Keys

| Key | Default | Type | Reload | Notes |
| --- | --- | --- | --- | --- |
| `TrackWorlds` | `general, wild, nether` | string list | reload or `debug set` | Exact Bukkit world names to track. |
| `SuspicionThreshold` | `100.0` | decimal | reload or `debug set` | Auto-handle threshold. |
| `DebugVerboseMiningSession` | `false` | boolean | reload or `debug set` | Verbose mining math in console. |
| `FinalEmeraldWeightDivisionReducer` | `2` | number | reload | Reduces emerald suspicion in mountain-style biomes. |
| `FinalGoldWeightDivisionReducer` | `4` | number | reload | Reduces gold suspicion in badlands biomes. |
| `ConsiderAdjacentWithinDistance` | `10` | integer | reload or `debug set` | Nearby ore still counted as the same vein. |
| `MinimumBlocksMinedToNextVein` | `10` | integer | reload or `debug set` | Non-ore blocks required before a new vein counts normally. |
| `IgnoreHigherThanOverworldAltitude` | `65` | integer | reload or `debug set` | Ignore overworld tracking above this Y level. |
| `IgnoreHigherThanNetherAltitude` | `240` | integer | reload or `debug set` | Ignore nether tracking above this Y level. |

## Ore Weight Keys

These keys are live suspicion weights and are the active source used by the runtime heuristic calculator.

| Key | Default | Family | Reload |
| --- | --- | --- | --- |
| `CoalWeight` | `5.0` | `COAL_ORE` | reload or `debug set` |
| `IronWeight` | `5.0` | `IRON_ORE`, `RAW_IRON_BLOCK` | reload or `debug set` |
| `CopperWeight` | `5.0` | `COPPER_ORE`, `RAW_COPPER_BLOCK` | reload or `debug set` |
| `GoldWeight` | `5.0` | `GOLD_ORE` | reload or `debug set` |
| `RedstoneWeight` | `9.0` | `REDSTONE_ORE` | reload or `debug set` |
| `EmeraldWeight` | `22.0` | `EMERALD_ORE` | reload or `debug set` |
| `LapisWeight` | `8.0` | `LAPIS_ORE` | reload or `debug set` |
| `DiamondWeight` | `15.0` | `DIAMOND_ORE` | reload or `debug set` |
| `DeepslateCoal` | `5.0` | `DEEPSLATE_COAL_ORE` | reload or `debug set` |
| `DeepslateIron` | `5.0` | `DEEPSLATE_IRON_ORE` | reload or `debug set` |
| `DeepslateCopper` | `5.0` | `DEEPSLATE_COPPER_ORE` | reload or `debug set` |
| `DeepslateGold` | `5.0` | `DEEPSLATE_GOLD_ORE` | reload or `debug set` |
| `DeepslateRedstone` | `9.0` | `DEEPSLATE_REDSTONE_ORE` | reload or `debug set` |
| `DeepslateEmerald` | `22.0` | `DEEPSLATE_EMERALD_ORE` | reload or `debug set` |
| `DeepslateLapis` | `8.0` | `DEEPSLATE_LAPIS_ORE` | reload or `debug set` |
| `DeepslateDiamond` | `22.0` | `DEEPSLATE_DIAMOND_ORE` | reload or `debug set` |
| `NetherGoldWeight` | `5.0` | `NETHER_GOLD_ORE`, `GILDED_BLACKSTONE` | reload or `debug set` |
| `QuartzWeight` | `5.0` | `NETHER_QUARTZ_ORE` | reload or `debug set` |
| `AncientDebrisWeight` | `22.0` | `ANCIENT_DEBRIS` | reload or `debug set` |

`RAW_GOLD_BLOCK` is intentionally not part of the tracked families.

## Automatic Handling Keys

| Key | Default | Reload | Notes |
| --- | --- | --- | --- |
| `StoreCopy` | `false` | reload or `debug set` | Stores inventory/equipment before handling. |
| `CleansePlayerItems` | `false` | reload or `debug set` | Clears inventory/equipment on handling. |
| `ClensePlayerItems` | `false` | reload or `debug set` | Legacy typo key retained for compatibility. |
| `SendMessageToPlayer` | `false` | reload or `debug set` | Sends the handled-player warning message. |
| `NullifySuspicionAfterPunish` | `true` | reload or `debug set` | Removes the active suspicion session after handling. |
| `NullifySuspicionAferPunish` | `true` | reload or `debug set` | Legacy typo key retained for compatibility. |
| `TellPlayersWithPermission` | `true` | reload or `debug set` | Sends staff warnings to `xrayheuristics.notify`. |

## Configured Command Sections

| Key | Default | Reload | Notes |
| --- | --- | --- | --- |
| `CommandsExecutedOnXrayerDetected` | bundled example section | reload | Bucketed console commands keyed by handled count. Uses `{PlayerName}` and `{TimesDetected}`. Review bundled examples before public/production use. |
| `CommandsExecutedOnPlayerAbsolved` | empty list | reload | Console commands run when a player is absolved. Uses `{PlayerName}`. |

## Storage Keys

| Key | Default | Reload | Notes |
| --- | --- | --- | --- |
| `StorageType` | `JSON` | restart | Accepted values: `JSON`, `MYSQL`. |
| `SQLDriverClassName` | empty | restart | Optional JDBC driver class override. |
| `SQLHost` | `127.0.0.1` | restart | MySQL host. |
| `SQLPort` | `3306` | restart | MySQL port. |
| `SQLDatabaseName` | `myDatabase` | restart | MySQL schema/database name. |
| `SQLUsername` | `root` | restart | MySQL username. |
| `SQLPassword` | `root` | restart | MySQL password. Stored as plain text in the config file. |
| `SQLMaxActiveConnections` | `10` | restart | Max pooled MySQL connections. |
| `UseDriverClassName` | `false` | restart | Legacy compatibility flag. Current code behavior is driven by whether `SQLDriverClassName` is empty. |

## Tools and GUI Keys

| Key | Default | Reload | Notes |
| --- | --- | --- | --- |
| `AddRandomDummyXrayerIfNoXrayerCommandParameters` | `false` | reload or `debug set` | Lets `/xrayer xrayer` create a dummy entry for testing. |
| `UseHeadsInGUI` | `false` | reload or `debug set` | Uses player heads in the vault GUI. Existing viewers may need to reopen the GUI after changing it. |

## Generated Companion Files

| File | Role | Current source behavior |
| --- | --- | --- |
| `locale.yml` | Generated message and GUI text values | Reloaded by `/xrayer reload`. |
| `weights.yml` | Generated per-world weight-card data | Generated and reloaded, but the main runtime suspicion logic reads live weights from `config.yml`. |
| `data.json` | Default JSON persistence file | Used only when `StorageType` is `JSON`. |
