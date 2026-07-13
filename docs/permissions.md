# Permissions

This page reconciles `plugin.yml` and the current command implementation.

## Effective Permission Matrix

| Permission | Default | Verified effect |
| --- | --- | --- |
| none | n/a | `/xrayer`, `/xrayer help`, and `/xrayer info` are currently open to any sender. |
| `xrayheuristics.use` | `op` | Allows `/xrayer suspicion [player]`. |
| `xrayheuristics.admin` | `op` | Allows debug, reload, resets, manual handling, vault access, absolve, purge, and the dummy-entry helper path when enabled. |
| `xrayheuristics.notify` | `op` | Receives automatic handled-player warning messages. |
| `xrayheuristics.ignore` | `false` | Exempts a player from heuristic tracking during block-break analysis. |

## Legacy Compatibility Nodes

The plugin still accepts these legacy nodes:

- `AXH.Commands.Suspicion`
- `AXH.Commands.Reload`
- `AXH.Commands.ResetSuspicion`
- `AXH.Commands.Xrayer`
- `AXH.Commands.Vault`
- `AXH.Commands.AbsolvePlayer`
- `AXH.Commands.PurgePlayer`
- `AXH.Vault.Purge`
- `AXH.XrayerWarning`
- `AXH.Ignore`

## Important Notes

- `plugin.yml` now describes `xrayheuristics.use` as the suspicion-lookup node. That matches the current implementation more accurately than the older “help output and suspicion lookups” wording.
- Console senders bypass most player permission checks, but player-only restrictions still apply to commands that require an open inventory GUI or self-targeted player context.
- The vault GUI includes a purge-all action that is governed by the legacy `AXH.Vault.Purge` node in the current code path.
