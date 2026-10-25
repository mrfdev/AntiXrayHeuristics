# Paper 26.3 stable compatibility verification

Verified on 2026-10-11, macOS ARM64, for release `2.0.5-033-j25-26.3`. Minecraft and the declared plugin API floor remain `26.3`. The [2026-09-25 alpha verification](compatibility-paper-26.3-alpha.md) is a dated historical record, not the maintained target.

The build workflow was updated on 2026-10-25: Oracle 25 now compiles every source set and Oracle 27 runs every automated test. See [current build instructions and verification](installation.md#build-from-source). The dated migration results below retain the original JDK versions and test arrangement.

## Verified Release and Dependencies

Discovery started at the live [Paper documentation index](https://docs.papermc.io/llms.txt). The [Fill 26.3 build list](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds) and [build 175 metadata](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/175) identified build `175`, published `2026-10-11T15:32:14Z`, channel `STABLE`, commit `bd9daa8fba19cb288d690c1621ad1749de3e0a78`. It was the newest stable 26.3 build at selection time. No other Minecraft version was selected.

The [Maven version list](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml) explicitly published `26.3.build.175-stable`. Its global `latest` and `release` fields pointed at a prerelease, so those fields were not used to select this target. The [exact POM](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/26.3.build.175-stable/paper-api-26.3.build.175-stable.pom) was inspected before resolving the dependency.

| Component | Verified version |
| --- | --- |
| Plugin | `2.0.5-033-j25-26.3` |
| Paper server | `26.3-175-bd9daa8`, channel `STABLE` |
| Compile/test dependency | `io.papermc.paper:paper-api:26.3.build.175-stable` |
| Gradle used | `9.8.1` |
| Compiler, build JVM and default tests | Oracle `25.0.4.1+1-LTS-5` |
| Additional tests and disposable server | Oracle `27+35-2325` |
| Plugin bytecode | Java 25, major `69`, minor `0`, no preview features |
| CoreProtect compile/test/server | `25.0`, API `13` |
| Paper-provided Adventure / Gson / SLF4J | `5.2.0` / `2.14.0` / `2.0.17` |
| Bundled DBCP / Commons Pool | `2.14.0` / `2.13.0` |

The [official setup guide](https://docs.papermc.io/paper/dev/project-setup/) requires Java 25 or newer. Java 25 compilation and unit tests remain the default; the maintained runtime remains Java 27. This run did not start a Paper server under Java 25.

- Plugin JAR: `build/libs/1MB-XRayHeuristics-v2.0.5-033-j25-26.3.jar`
- Plugin SHA-256: `2c41b266c9c9d9828a0763ce9b9a50f4ccba8807114cf6374a57174e9242d166`
- Paper SHA-256: `ece5afb6d0cdf0c930489618bf92341e33d2ebcd4f21e6d365daff6ed26dafb0`
- CoreProtect SHA-256: `097f77f7721fb2728073bae1fbd97916936dc1ff019064a1008b7b6ef036c17f`

There are no paperweight, development-bundle, NMS, or CI workflow dependencies to migrate in this repository. Existing explicit pins for older Paper-provided libraries were removed so compilation follows Paper's published dependencies. `gradle.lockfile` records the resolved compile, test, probe and bundled-library configurations. `version.properties` is the source of truth for the release, stable API pin and official server checksum. Semantic patch `2.0.5` and build `033` were reserved once and reused throughout verification.

## API Review and Compatibility Fixes

The hosted [26.3 Javadocs](https://jd.papermc.io/paper/26.3/) still identified build 157 beta when checked. Both build 175's [source artifact](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/26.3.build.175-stable/paper-api-26.3.build.175-stable-sources.jar) and [Javadoc artifact](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/26.3.build.175-stable/paper-api-26.3.build.175-stable-javadoc.jar) were inspected; the latter identifies `paper-api 26.3.build.175-stable`. No documentation index or full documentation dump was vendored.

Reviewed JavaPlugin enable/disable, event priority and cancellation, BlockBreakEvent, inventory identity guidance, ItemStack serialization, PDC/data components, scheduler thread rules, plugin API-version, deprecations and the [Paper roadmap](https://docs.papermc.io/paper/dev/roadmap/). Relevant published build changes include the Adventure-mode BlockBreakEvent correction in build 139 and component-upgrade fixes in build 171. The plugin uses supported Bukkit/Paper APIs and has no internals to version-gate. Existing ItemStack constructors remain supported, though the roadmap recommends `ItemStack.of` for future development.

Two focused behavior fixes resulted from verification:

1. A real-server regression probe demonstrated that the original listener created a Suspicion Session for an already-cancelled break. It now observes events at `MONITOR` with `ignoreCancelled = true`. Completed mining retains its existing weighting and thresholds; cancelled attempts no longer affect suspicion.
2. Restart/storage checks exposed Java 27 warnings from Gson reflectively populating final fields in `HandledPlayerRecord`. `HandledPlayerJson` now deserializes via the existing constructor. JSON field names, nullable belongings, default values, handling counts, public fields and stored location/item payloads are preserved. The Java 27 suite and server probe pass with `--illegal-final-field-mutation=deny`, following [OpenJDK JEP 500](https://openjdk.org/jeps/500); no warning-suppression flags are used. The adapter uses Gson's supported [custom deserialization API](https://google.github.io/gson/UserGuide.html#custom-serialization-and-deserialization).

Plugin identity, commands, permissions, data directory, config keys, MySQL schema and canonical/legacy APIs are preserved. No LuckPerms API or built-in LuckPerms command integration exists, and this migration does not introduce one.

## Automated Verification

| Check | Result |
| --- | --- |
| `./scripts/rebuild.sh smokeTestJar` | Clean compile, package, unit tests, probe build and documentation drift checks passed |
| Java 25 JUnit suite | 16 passed, 0 failed/errors/skipped |
| `./scripts/test-java27.sh` | 16 passed, 0 failed/errors/skipped, strict final-field mutation denial |
| Artifact inspection | All 59 project classes use Java 25 bytecode; correct manifest/version/API floor; no Paper, CoreProtect or probe classes bundled |
| Disposable Java 27 Paper startup | Plugin and CoreProtect enabled; 126 probe assertions passed |
| Restart with existing disposable data | 126 assertions passed; config, locale, weights and populated JSON hashes unchanged |
| Missing CoreProtect | Paper rejected the plugin with the expected missing dependency diagnostic; server still stopped cleanly |
| Shutdown | All three runs exited 0, disabled loaded plugins, completed world I/O and released the reserved ports |
| Maintained local development instance | Build 175 SHA-256 matches Fill; stable channels configured; instance stopped before and after upgrade |

Runtime evidence is retained locally in the ignored directory `servers/paper-26.3-stable-033-6olmf5b6/`: `startup.log`, `restart.log`, `missing-coreprotect.log`, `runtime.txt`, `paper-build.json` and `smoke-results.json`. The failed cancellation probe and pre-fix Gson warning logs are retained in earlier disposable run directories. No test loaded an existing server world or database. Only CoreProtect, the release JAR and the separately built probe were installed. No messaging integration was installed. The shared deployment destination and historical instances were untouched.

The probe checks the actual Paper-registered mining listener using synthetic players, uncancelled ore suspicion, ignored players, denied vault/reload/reset/suspicion/debug commands, reload state, canonical and legacy API availability, all 41 stored inventory/equipment slots, foreign PDC, names/lore, a changed stack-size component, a shulker box containing a custom item, absent and corrupt item payloads, asynchronous JSON insert/read/delete, and main-thread callbacks. A populated handled-player fixture survives the restart. Console commands `version`, `xrayer info`, `xrayer help`, `xrayer debug` and `xrayer reload` passed in both successful startup phases.

Normal startup/restart had no ERROR/SEVERE diagnostics. Remaining warnings were OSHI not knowing the macOS 27 codename, the first-run partial Paper config lacking a version marker, and the intentionally offline loopback server. The plugin's final-field mutation warning is resolved. The missing-dependency run contains the intentionally expected loader error.

The maintained `servers/Server-Two-Paper-26.3/` was upgraded while stopped, with the previous Paper JAR backed up by PaperScript. Its `STABLE` channel settings are explicit, its directory/session/ports and Java 27 launcher remain local to this project, and automatic same-version/cross-version upgrades remain disabled. This instance was not started during this migration. Follow [installation](installation.md#disposable-stable-compatibility-tests) to reproduce the disposable checks.

## Remaining Checks and Existing Limits

These checks establish build/runtime compatibility for the exercised paths. They do not certify every live staff-handling or transaction path. The following require a real player client, a wider plugin stack or database infrastructure that was not used in this isolated test:

1. Join an isolated stable server with a Java player and a `.bedrock` player through the intended Floodgate/Geyser stack. Mine configured ores in tracked/untracked worlds and above/below altitude limits, verify both ignore permissions, and cancel mining through the intended protection plugin. Verify cancelled breaks never raise suspicion and successful mining still does.
2. With two staff clients, open the vault and confiscated-item view. Exercise normal/shift/double clicks, drag, hotbar/offhand swaps, drop, creative actions, page changes, close, quit and stale views. Revoke permissions while a view is open. Verify authoritative access and that no item can be copied, lost or moved through the GUI unintentionally.
3. On throwaway player inventories, handle and absolve players with full/partial inventories, armour/offhand items, PDC from other plugins, bundles and filled containers. Repeat concurrent/rapid actions and quit/kick/death/world-change cases; verify each stored item is returned once, overflow is handled correctly, and no record is removed before delivery. Repeat with AutoSell and other inventory plugins if used.
4. With a disposable MySQL database and deliberately invalid credentials, test initialization, storage, reconnect, failed writes, shutdown during work, and restart recovery. Also force a crash between confiscation, persistence and delivery in both storage modes. No MySQL, crash-recovery or financial/reward transaction was tested here.
5. Re-test any locally configured commands and third-party integrations separately. The bundled defaults do not call LuckPerms. If an administrator adds LuckPerms commands, this plugin's existing renderer only expands its documented name/count placeholders; it does not implement verified UUID resolution or `{uuid}`. Do not add name-targeted LuckPerms commands. A supported integration must resolve the authoritative local server UUID, preserve Bedrock prefixes during lookup, reject ambiguous/missing records, and retain that UUID during retries without an external identity lookup.

Existing implementation limits remain relevant to those manual checks: the handled-player vault identifies inventories by title rather than a custom holder, live-player storage still reads some Player/inventory state inside asynchronous tasks, and storage/delivery does not implement durable idempotent transactions. These are pre-existing design concerns; the synthetic persistence test does not establish their safety. They need focused follow-up before relying on concurrent or failure-prone confiscation/absolution workflows. The [scheduler](https://docs.papermc.io/paper/dev/scheduler/) and [custom InventoryHolder](https://docs.papermc.io/paper/dev/custom-inventory-holder/) guidance remains applicable.
