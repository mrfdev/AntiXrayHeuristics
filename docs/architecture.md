# Architecture

The maintained code treats X-ray Heuristics as an independently owned feature that currently ships in a standalone Paper plugin and can later be hosted by the unified CoreProtect Add-ons plugin.

## Runtime Shape

```text
Paper
└── XRayHeuristicsPlugin              standalone JavaPlugin adapter
    ├── XRayHeuristicsModule          reusable feature lifecycle and state
    │   ├── CoreProtectHook           required CoreProtect access
    │   ├── listeners and commands    Paper-facing feature adapters
    │   ├── HandledPlayerStore        JSON or MySQL persistence
    │   └── handled-player services   handling and vault workflows
    └── com.greymagic27 adapters      legacy Java API compatibility
```

`XRayHeuristicsModule` is deliberately not a `JavaPlugin`. Its constructor receives the owning `JavaPlugin`, the feature data directory, and optionally a `CoreProtectHook`. The module uses the host solely where Paper needs an owning plugin instance, such as listener and scheduler registration.

Feature identity and release metadata are also independent from the owning plugin. The standalone adapter validates that its `plugin.yml` matches the feature build metadata; an embedded module does not require the future combined host to share its version or plugin id.

The standalone adapter supplies `plugins/1MB-XRayHeuristics/`, registers `/xrayer`, and delegates enable, disable, config, and resource operations to the module. Embedded feature resources live under `xrayheuristics/` so they will not collide with other features when their code is packaged in one jar.

## Compatibility Contract

The modularization does not rename the current server-facing contract:

- plugin id: `xrayheuristics`
- command: `/xrayer`
- permissions: `xrayheuristics.*` and the documented legacy `AXH.*` nodes
- data directory: `plugins/1MB-XRayHeuristics/`
- files: `config.yml`, `locale.yml`, `weights.yml`, and `data.json`
- MySQL table and JSON field names
- legacy Java entry point and API under `com.greymagic27`
- legacy Bukkit serialization alias for existing `BlockWeightInfo` entries

New Java integrations should use `XRayHeuristicsPlugin#getApi()` and `XRayHeuristicsApi`. The old `GetPlugin()` and `GetAPI()` surface remains available through deprecated adapters.

## Future Combined Host

The unified CoreProtect Add-ons plugin can instantiate the feature with its own plugin instance and an explicit subdirectory:

```java
File featureDirectory = new File(getDataFolder(), "features/xrayheuristics");
XRayHeuristicsModule xrayHeuristics = new XRayHeuristicsModule(this, featureDirectory, sharedCoreProtectHook);

if (!xrayHeuristics.enable()) {
    getLogger().severe("The X-ray Heuristics feature could not start.");
}
```

The combined host should call `disable()` during its own shutdown. It can keep a legacy `/xrayer` command by passing that command to `registerStandaloneCommand`, or route a future unified command tree to the module API while retaining `/xrayer` as an alias during migration.

The final consolidation should decide the data migration policy explicitly. Until then, the standalone adapter continues using the existing directory and schemas, so this refactor does not silently move production data.
