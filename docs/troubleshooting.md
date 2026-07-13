# Troubleshooting

## Plugin Does Not Enable

Check these first:

- CoreProtect is installed
- CoreProtect is enabled before `1MB XRayHeuristics`
- CoreProtect reports API `11` or newer
- The server is running a Java version compatible with the plugin build target

If the plugin disables itself at startup, `/xrayer debug` will not be available, so check the startup log for the CoreProtect API requirement message.

## `/xrayer suspicion` Says No Suspicion Level Is Available

That message means there is no active in-memory suspicion session for the requested player at that moment. It does not mean the plugin failed.

## `/xrayer purge <player>` Does Not Work For An Offline Target

That is the current behavior. The command path only removes online targets. The plugin's own locale text directs staff to use the vault GUI for offline individual purge workflows.

## `/xrayer absolve <player>` Fails

The target must be online so stored items can be returned or dropped nearby if inventory space runs out.

## Config Changes Do Not Apply

- Use `/xrayer reload` for normal config, locale, and generated-weights refreshes.
- Use `/xrayer debug set <key> <value>` for supported live-edit keys.
- Restart the server after storage-backend changes such as `StorageType`, SQL credentials, or pool settings.

## Debug The Hook State

Run `/xrayer debug` and check:

- `CoreProtect hooked`
- `CoreProtect version`
- `CoreProtect API`
- `Hook status`
- `Storage type`
- `Tracked worlds`

## Persistence Files

Depending on storage mode:

- JSON mode uses `plugins/1MB-XRayHeuristics/data.json`
- MySQL mode uses the `Xrayers` table

If handled-player data looks missing, confirm the expected storage mode before troubleshooting anything else.

## `weights.yml` Looks Different From `config.yml`

That is expected in the current source tree. `weights.yml` is still generated and reloaded, but the main runtime suspicion calculator reads its active weight values from the top-level keys in `config.yml`.

## Help And Info Permissions

`/xrayer`, `/xrayer help`, and `/xrayer info` are currently open to any sender. If your server policy expects those commands to be restricted, that is an operational choice to review before changing production behavior.
