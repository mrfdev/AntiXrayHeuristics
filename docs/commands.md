# Commands

Main command: `/xrayer`

The bare `/xrayer` command with no arguments opens the help page. `/xrayer info` is the public onboarding command required by the central docs system.

## Command Reference

| Command | Aliases | Permission | Console | Notes |
| --- | --- | --- | --- | --- |
| `/xrayer` | none | none | yes | Opens the help page. |
| `/xrayer info` | none | none | yes | Shows the plugin intro, docs URL, and installed version/build. |
| `/xrayer help` | `?` | none | yes | Shows the command summary. |
| `/xrayer suspicion [player]` | `s` | `xrayheuristics.use` or `AXH.Commands.Suspicion` | partly | `/xrayer suspicion` without a target is player-only. |
| `/xrayer debug` | none | `xrayheuristics.admin` or legacy reload/admin node path | yes | Shows plugin, server, CoreProtect, storage, and config status. |
| `/xrayer debug help` | `?` after `debug` | `xrayheuristics.admin` | yes | Lists debug pages. |
| `/xrayer debug permissions` | none | `xrayheuristics.admin` | yes | Lists permission nodes and defaults. |
| `/xrayer debug commands` | none | `xrayheuristics.admin` | yes | Lists command syntax and examples. |
| `/xrayer debug config` | none | `xrayheuristics.admin` | yes | Shows supported live config keys. |
| `/xrayer debug set <key> <value>` | none | `xrayheuristics.admin` | yes | Saves a supported config value and reloads plugin state. |
| `/xrayer reload` | `r` | `xrayheuristics.admin` or `AXH.Commands.Reload` | yes | Reloads `config.yml`, `locale.yml`, `weights.yml`, and hook state. |
| `/xrayer resetsuspicion [player]` | `rs` | `xrayheuristics.admin` or `AXH.Commands.ResetSuspicion` | partly | `/xrayer resetsuspicion` without a target is player-only. |
| `/xrayer <player>` | none | `xrayheuristics.admin` or `AXH.Commands.Xrayer` | yes | Canonical manual-handle syntax. |
| `/xrayer xrayer <player>` | `x` | `xrayheuristics.admin` or `AXH.Commands.Xrayer` | yes | Explicit equivalent of `/xrayer <player>`. |
| `/xrayer xrayer` | `x` | `xrayheuristics.admin` or `AXH.Commands.Xrayer` | yes | Only creates a dummy entry when `AddRandomDummyXrayerIfNoXrayerCommandParameters` is enabled. |
| `/xrayer vault` | `v` | `xrayheuristics.admin` or `AXH.Commands.Vault` | no | Opens the handled-player vault GUI. |
| `/xrayer absolve <player>` | `a` | `xrayheuristics.admin` or `AXH.Commands.AbsolvePlayer` | yes | The target must be online so items can be returned. |
| `/xrayer purge <player>` | `p` | `xrayheuristics.admin` or `AXH.Commands.PurgePlayer` | yes | Command path only removes online targets. |

## Debug Set Keys

`/xrayer debug set` supports a verified subset of `config.yml`, including:

- `suspicion-threshold`
- `debug-verbose-mining-session`
- `track-worlds`
- `consider-adjacent-within-distance`
- `minimum-blocks-mined-to-next-vein`
- `ignore-higher-than-overworld-altitude`
- `ignore-higher-than-nether-altitude`
- `store-copy`
- `cleanse-player-items`
- `send-message-to-player`
- `nullify-suspicion-after-punish`
- `tell-players-with-permission`
- `add-random-dummy-xrayer`
- `use-heads-in-gui`
- all verified ore-weight keys shown in [configuration.md](configuration.md)

## Command Behavior Notes

- The plugin currently treats `info` as a reserved subcommand, so a player literally named `info` must be handled with `/xrayer xrayer info`.
- `/xrayer purge <player>` does not purge offline targets from the command line. The current code tells staff to use the vault GUI for offline entries.
- `/xrayer absolve <player>` requires the target to be online so confiscated items can be returned safely.
