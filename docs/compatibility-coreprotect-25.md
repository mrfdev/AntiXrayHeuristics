# CoreProtect 25 compatibility verification

Verified on 2026-09-04 for `1MB-XRayHeuristics-v2.0.2-030-j25-26.2.jar`.

| Component | Verified target |
| --- | --- |
| CoreProtect | User-supplied `CoreProtect-25.0-rc1.jar` |
| CoreProtect API | `13`, previously `12` in `24.0-dev1` |
| Paper | `26.2-121-main@a2a42c5`, stable |
| Paper compile API | `26.2.build.121-stable`, updated from build `84` |
| Build and runtime JDK | Oracle Java `25.0.4+7-LTS-189`, macOS ARM64 |
| Plugin bytecode | Java `25`, class-file major version `69` |
| Declared Bukkit API floor | `1.21.11` |
| Storage used in the test | X-ray Heuristics JSON; CoreProtect DuckDB |

## API compatibility

Inspection of the supplied jar and an executed `CoreProtectAPI.APIVersion()` call confirm API `13`. CoreProtect's `plugin.yml` value `api-version: 1.16` is its Bukkit compatibility declaration, not its CoreProtect API version.

The existing hook uses `CoreProtect.getAPI()`, `CoreProtectAPI.APIVersion()` and `CoreProtectAPI.isEnabled()`. All remain available. The minimum accepted CoreProtect API stays at `11`; the existing comparison accepts `13`. Mining analysis uses Bukkit events and does not call CoreProtect lookup or rollback methods. No lookup migration is required for the typed APIs added in CoreProtect 25.

The Gradle build now compiles and tests against the supplied release jar. Its default location is `~/Downloads/CoreProtect-25.0-rc1.jar`; use `-PcoreProtectJar=/absolute/path/to/CoreProtect-25.0-rc1.jar` to override it. There is no fallback to an older CoreProtect dependency. `CoreProtectCompatibilityTest` checks the actual dependency's plugin version and API version against the generated release metadata. The distributed plugin jar does not contain CoreProtect classes.

## Verification results

- `gradle clean build printBuildConfig --warning-mode all` passed with Java 25: 11 tests, zero failures. The build also passed the release documentation and generated metadata drift checks.
- All 58 project classes in the release jar have Java 25 bytecode. Generated `plugin.yml` and `xrayheuristics/build-info.properties` report the updated targets.
- An isolated Paper server bound to `127.0.0.1:25594` enabled both plugins, created the expected config, locale, weights and JSON files, and reported `CoreProtect 25.0-rc1 (API 13)`.
- Restart with the existing plugin data succeeded. `/version` confirmed Paper was current; `version CoreProtect`, `version xrayheuristics`, `/xrayer info`, `/xrayer help`, `/xrayer debug` and `/xrayer reload` succeeded. Debug output reported Java `25.0.4`, Paper build `121` and an enabled CoreProtect hook before and after reload.
- A temporary integration probe invoked the plugin's registered Paper block-break listener with real world blocks and a synthetic player. Stone mining created a Suspicion Session; subsequent diamond mining increased suspicion from `0.0` to `45.0`. Reload preserved that session and the CoreProtect hook. The canonical and legacy plugin APIs were available. The probe restored its block and removed its temporary session.
- Console `stop` disabled both plugins, flushed CoreProtect data, saved the worlds and exited with code `0`. The verified command/probe run contains no server-log errors. The JVM separately emitted Paper's bundled JOML `sun.misc.Unsafe` deprecation warning.
- Paper's generated exploit-protection settings were left at their defaults, including disabled piston duplication and unsafe end-portal teleportation, with no oversized-component sanitizer exclusions.

The local test server, temporary probe source and `verified-run.log` are retained under the gitignored `servers/compat-coreprotect25-paper121/` directory. This verification covers the CoreProtect integration, release build, mining listener and plugin lifecycle. It does not cover a connected player's inventory/vault interactions, MySQL, or a production server's full plugin set.

## Artifact provenance and official references

- CoreProtect jar SHA-256: `89a11ccba48140e5c0bb519364792077f633aa10ce8bad8f9169ab9e74997e32`.
- Paper jar SHA-256: `0de30efb024bc8b83c9c7d507d11802897ad8056b6110ec09fe1a91d126ccb54`, matched against the [official Paper 26.2 build service](https://fill.papermc.io/v3/projects/paper/versions/26.2/builds). Build `121` was the latest stable build at verification time.
- Documentation discovery started from the live [PaperMC LLM index](https://docs.papermc.io/llms.txt). Reviewed the [project setup guide](https://docs.papermc.io/paper/dev/project-setup/), [plugin metadata and dependencies](https://docs.papermc.io/paper/dev/plugin-yml/), [scheduler/thread rules](https://docs.papermc.io/paper/dev/scheduler/), [roadmap](https://docs.papermc.io/paper/dev/roadmap/), [global configuration](https://docs.papermc.io/paper/reference/global-configuration/) and [world configuration](https://docs.papermc.io/paper/reference/world-configuration/).
- Exact target Javadocs identify `paper-api 26.2.build.121-stable`: [JavaPlugin lifecycle](https://jd.papermc.io/paper/26.2/org/bukkit/plugin/java/JavaPlugin.html), [BlockBreakEvent](https://jd.papermc.io/paper/26.2/org/bukkit/event/block/BlockBreakEvent.html), [EventHandler](https://jd.papermc.io/paper/26.2/org/bukkit/event/EventHandler.html) and [deprecations](https://jd.papermc.io/paper/26.2/deprecated-list.html). Reviewed published changes from builds `85` through `121`; no integration API change required source migration.
- [CoreProtect API documentation](https://docs.coreprotect.net/api/) provides the API integration reference; the supplied release-candidate jar and live runtime are the authority for this verification's version values.
