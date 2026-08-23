# X-Ray Heuristics

This context covers the live mining-analysis and staff-handling feature currently shipped as 1MB XRayHeuristics. The feature is intended to become one part of the unified CoreProtect Add-ons plugin.

## Language

**CoreProtect Add-ons**:
The future 1MoreBlock plugin that hosts multiple independently owned features built around CoreProtect.
_Avoid_: CoreProtect plugin, CoreProtect itself

**X-ray Heuristics**:
The feature that observes live mining behavior and accumulates suspicion from mining patterns.
_Avoid_: AntiXrayHeuristics, AXH, XRayHeuristics when used as prose

**Suspicion Session**:
The temporary per-player state used to evaluate live mining behavior and its accumulated suspicion.
_Avoid_: Player cache, scan

**Handled Player**:
A player for whom the X-ray Heuristics feature has completed its configured handling workflow and stored a durable record.
_Avoid_: Xrayer and cheater in new domain language. Existing commands, permission nodes, config keys, schemas, locale keys, and deprecated APIs retain their legacy identifiers for compatibility.

**Handled-player Vault**:
The staff workflow for reviewing, absolving, or purging handled-player records and any stored belongings.
_Avoid_: XrayerVault except as a compatibility identifier
