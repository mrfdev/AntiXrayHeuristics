# Player Guide

## Introduction

`1MB XRayHeuristics` is a 1MoreBlock Anti-XRay plugin that helps staff react to suspicious mining behavior. It is primarily a staff tool, but it now includes `/xrayer info` so players and staff can quickly see what the plugin is, which build is installed, and where the official documentation lives.

Canonical docs page: [docs.1moreblock.com/custom-server-plugins/xrayheuristics](https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/)

## How Players Use It

Most regular players will never need to interact with this plugin directly. The public-facing entry points are:

- `/xrayer info`
- `/xrayer help`

If you are trusted staff or otherwise have permission, you can also check live suspicion sessions with `/xrayer suspicion [player]`.

## Available Features

- A public `/xrayer info` page with a quick start and docs link
- A `/xrayer help` page with command summaries
- Live suspicion lookups for authorized users
- Staff handling workflows for suspicious players
- Optional staff notifications when someone is automatically handled

## Quick Start

1. Run `/xrayer info` to open the introduction page.
2. Run `/xrayer help` to see the full command list.
3. If you have permission, run `/xrayer suspicion <player>` to check a live suspicion session.

## Commands

| Command | Who it helps | Notes |
| --- | --- | --- |
| `/xrayer info` | Everyone | Shows the plugin introduction, docs URL, and installed version/build. |
| `/xrayer help` | Everyone | Shows the command list and overview. |
| `/xrayer suspicion [player]` | Staff or trusted users | Requires the suspicion permission node. |

The plugin's deeper handling commands such as debug, reload, vault, absolve, purge, and manual handling are staff/admin features and are documented in the technical pages.

## Permissions or Rank Requirements

- `/xrayer info` and `/xrayer help` do not enforce a permission check in the current implementation.
- `/xrayer suspicion [player]` requires `xrayheuristics.use` or the legacy `AXH.Commands.Suspicion` node.
- Most staff workflows require `xrayheuristics.admin`.

## Rewards, Costs, Limits, and Cooldowns

- This plugin does not grant rewards or player economy payouts.
- There are no public-facing cooldowns or costs.
- A suspicion value only exists while a live in-memory session is active.
- If staff are configured to store or cleanse items, a handled player may need staff assistance before their stored items are returned.

## Placeholders

The plugin does not expose verified PlaceholderAPI identifiers for players to use elsewhere. Its placeholders are internal placeholders used in config commands and GUI text.

## Important Notes

- `1MB XRayHeuristics` is designed for staff review workflows, not normal player progression.
- CoreProtect is required for the plugin to enable.
- The public entry point required by the central docs system is `/xrayer info`.

## Related Features

- CoreProtect integration for hook status and compatibility checks
- Staff-facing vault GUI for handled-player storage review
- JSON or MySQL persistence for stored handled-player data

## Technical Documentation

- Canonical page: [docs.1moreblock.com/custom-server-plugins/xrayheuristics](https://docs.1moreblock.com/custom-server-plugins/xrayheuristics/)
- Project technical overview: [../README.md](../README.md)
- Command reference: [commands.md](commands.md)
