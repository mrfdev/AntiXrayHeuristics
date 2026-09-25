# Paper 26.3 and Java 27 compatibility verification

Verified on 2026-09-25 on macOS ARM64 for `1MB-XRayHeuristics-v2.0.4-032-j25-26.3.jar`.

## Preserved baseline and release identity

The clean baseline is commit `dc1bc6b826d7f9d13260f6f48f57794936740424`, release `2.0.3-031-j25-26.2`. Annotated tag `v2.0.3-paper-26.2` was pushed before changing release metadata. Its recorded evidence is the [2026-09-15 JDK 25/26 verification](compatibility-jdk-25-26.md): Paper 26.2 build 121 stable, CoreProtect 25.0-rc1 API 13, 11 tests on each JDK, and successful startup, mining, reload, restart and shutdown checks. That record makes no Paper 26.3 claim.

This upgrade reserves semantic patch `2.0.4` and build `032` exactly once in `version.properties`; repeat builds reuse them. The release tag is `v2.0.4-paper-26.3`. The internal identity `xrayheuristics`, main class, `/xrayer` command, permissions, `plugins/1MB-XRayHeuristics/` data directory, serialization namespace and canonical/legacy Java APIs are unchanged. No gameplay source changes were required. The declared `plugin.yml` API floor is now `26.3`.

## Exact targets and checksums

| Component | Verified target |
| --- | --- |
| Gradle | `9.7.1` |
| Build JVM, compiler and default test JVM | Oracle `25.0.4.1+1-LTS-5` |
| Additional test JVM and local server runtime | Oracle `27+35-2325` |
| Plugin bytecode | Java 25, class-file major `69`, no preview features |
| Paper | `26.3-41-main@a15fed9`, channel `ALPHA` |
| Compile and test coordinate | `io.papermc.paper:paper-api:26.3.build.41-alpha` |
| CoreProtect compile/test/runtime target | `25.0`, API `13` |

Build 41 was the newest 26.3 build at selection time; all available builds were alpha. The experimental target was explicitly authorized. There was no fallback to 26.2. PaperScript downloaded build 41 and matched its SHA-256 to the [official build metadata](https://fill.papermc.io/v3/projects/paper/versions/26.3/builds/41), both during installation and in a separate verification after testing.

- Release JAR SHA-256: `854efcbe36117cdc6939ae3f561515b15ff9f8bda1128bbadccba5da54a4b770`
- Paper server JAR SHA-256: `2b77166ee61886a9bc9ab33dc9e4847fa3538b36d9ba6e5f2fa7ed90973aa748`
- CoreProtect JAR SHA-256: `097f77f7721fb2728073bae1fbd97916936dc1ff019064a1008b7b6ef036c17f`

## Build and automated results

| Check | Result |
| --- | --- |
| `./scripts/rebuild.sh` | Clean compile, package, tests and release/documentation drift checks passed |
| Java 25 JUnit suite | 11 passed, 0 failures/errors/skipped |
| `./scripts/test-java27.sh` | 11 passed, 0 failures/errors/skipped, using Java 25 compiled classes |
| Artifact inspection | One distributable JAR; 58 project classes at major 69; every bundled class compatible with Java 25; no CoreProtect classes bundled |
| Generated metadata | Descriptor and namespaced build information match version, build, Paper coordinate/channel, API floor and CoreProtect target |
| Java compiler deprecations | Clean compilation with `-Xlint:deprecation`; no compiler deprecation warnings emitted |
| Paper startup and restart | Both complete smoke cycles reached readiness and enabled the plugin and CoreProtect hook |
| Commands | `plugins`, `version xrayheuristics`, `version CoreProtect`, `xrayer info`, `xrayer help`, `xrayer debug`, `xrayer reload`, then `xrayer debug` passed in both cycles |
| Mining probe | Real world blocks and a synthetic test player exercised the registered listener on the server thread; stone created a Suspicion Session and diamond raised suspicion from `0.0` to `45.0` |
| Reload and APIs | Suspicion Session and CoreProtect hook survived reload; canonical and legacy plugin APIs were available |
| Shutdown | Both cycles disabled plugins, saved all three dimensions, drained region I/O and exited `0`; no server errors |
| Persistent plugin files | Config, locale, weights and JSON hashes identical across the verified restart |

The temporary probe added the copied instance's `spawn` world to the in-memory tracked-world list, restored its block, removed its synthetic session and reloaded the original configuration. Its JAR was removed from the active plugin directory after testing. This is a listener integration check, not a connected-client gameplay test.

The first attempt passed the plugin checks and exited `0`, but the old harness looked for `All dimensions are saved`. Paper 26.3 logs per-dimension I/O completion instead. The harness was updated to require completion for overworld, Nether and End plus region I/O drain, then both complete cycles passed. The first attempt's log is retained separately.

## Preserved and maintained instances

All paths below are under `/Users/floris/Projects/Codex/AntiXrayHeuristics/`:

- Preserved original: `servers/Server-Two-Paper-26.2/`
- Preserved baseline evidence and release copy: `servers/compat-jdk25041-jdk26021-paper121/`
- Earlier preserved evidence: `servers/compat-coreprotect25-paper121/`
- New instance: `servers/Server-Two-Paper-26.3/`
- Compile support: `servers/compile-support/CoreProtect-25.0.jar`

The stopped 26.2 directory was copied with APFS file cloning; the original was not modified. It contained an older `2.0.0-025` plugin JAR, while the latest `2.0.3-031` snapshot and evidence were in the compatibility instance. Old launcher/runtime files and replaced plugin JARs remain recoverable inside the new instance's `.upgrade-26.3-backup/`.

The new instance uses loopback TCP `25743`, disabled query UDP `25744`, disabled RCON TCP `25745`, and session `axh-paper-26.3-25743`. Ports were checked against other project configurations and actual socket availability. Its launcher explicitly exports the installed Java 27 `JAVA_HOME` and `PATH`. PaperScript was initialized from tool code and example defaults only; shared configuration, player data, download caches and old state were not imported. Its state identifies the new directory, 26.3 build 41 and verified checksum; both channels are explicitly `ALPHA`, with automatic version/build upgrades disabled. DiscordSRV is absent.

The copied plugin's config, locale and JSON data remain byte-identical to the original 26.2 directory. Its older `weights.yml` serialization aliases were normalized by the existing compatibility reader from `com.greymagic27.util.BlockWeightInfo` to `xrayheuristics:BlockWeightInfo`; all world names, materials, heights and numeric weights remain unchanged. All four files remained stable across the verified restart.

The generated and inherited exploit protections remain safe: piston duplication, headless pistons, permanent block-break exploits, unsafe end portals and tripwire validation bypasses are disabled; the oversized-component sanitizer has no exclusions.

## Local dependency updates

Only dependency JARs were copied from `/Users/floris/MinecraftServer/test-1mb-3.14-mc-26.3/plugins/`. Their actual manifests were inspected. The source files were left intact, and no shared configurations or player databases were imported.

| Dependency | Previous maintained local JAR | New local JAR / manifest version |
| --- | --- | --- |
| CoreProtect | `CoreProtect-24.0-dev1.jar` | `CoreProtect-25.0-26.3.jar` / `25.0`, API `13` |
| CMI | `CMI-9.8.8.1.jar` | `CMI-9.8.10.1.jar` / `9.8.10.1` |
| CMILib | `CMILib1.5.9.7.jar` | `CMILib1.6.0.0.jar` / `1.6.0.0` |
| LuckPerms | `LuckPerms-Bukkit-5.5.57.jar` | `LuckPerms-Bukkit-5.5.85.jar` / `5.5.85` |
| PlaceholderAPI | `PlaceholderAPI-2.12.3-DEV-266.jar` | `PlaceholderAPI-2.12.3.jar` / `2.12.3` |

Vault was retained: `Vault-1.7.4.jar` actually declares `1.7.3-CMI`. All seven runtime plugins enabled. CoreProtect is the only hard dependency of X-ray Heuristics; WorldEdit, WorldGuard, Geyser and Floodgate were not needed for this plugin's smoke coverage. The compile dependency moved from CoreProtect 25.0-rc1 to the supplied 25.0 JAR.

## Notices and remaining checks

Paper's bundled JOML emits an `Unsafe::objectFieldOffset` warning, OSHI cannot map macOS 27.0 to a codename, and LuckPerms' bundled Commodore warns about reflective final-field mutation on Java 27. These are unresolved third-party notices. No JVM flags were added to hide or relax them.

Manual checks remain for connected Java/Bedrock players, staff vault and inventory interactions, metadata-heavy items, MySQL, full CoreProtect database migration behavior and the shared server's complete plugin set. The shared server must remain stopped during artifact replacement and was not used for runtime testing. The intended replacement is only `plugins/1MB-XRayHeuristics-v2.0.2-030-j25-26.2.jar` with the exact verified `1MB-XRayHeuristics-v2.0.4-032-j25-26.3.jar`; preserve recoverable backups and verify its checksum after deployment.

## Evidence and official review

The gitignored new instance retains `build-java25.log`, `test-java27.log`, `build-evidence/`, `artifact-check.json`, `paper-build-41.json`, `paper-builds-at-selection.json`, `upgrade-dependencies.json`, `java-27-startup.log`, `java-27-restart.log`, `java-27-initial-shutdown-marker-attempt.log`, `smoke-results.json`, `run-smoke.py`, probe source and its inactive JAR. Build reports are also under `build/reports/tests/test/` and `build/reports/tests/testJava27/`.

Discovery started with the live [PaperMC LLM index](https://docs.papermc.io/llms.txt). Reviewed [project setup](https://docs.papermc.io/paper/dev/project-setup/), [plugin descriptors](https://docs.papermc.io/paper/dev/plugin-yml/), [runtime requirements](https://docs.papermc.io/paper/getting-started/), [scheduling](https://docs.papermc.io/paper/dev/scheduler/), [roadmap](https://docs.papermc.io/paper/dev/roadmap/), [global configuration](https://docs.papermc.io/paper/reference/global-configuration/), [world configuration](https://docs.papermc.io/paper/reference/world-configuration/) and the [download service](https://docs.papermc.io/misc/downloads-service/). The live documentation still illustrated stable 26.2 in some places; the actual 26.3 descriptor was verified by the successful server enable.

[Maven metadata](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/maven-metadata.xml) confirmed the exact coordinate. The [26.3 Javadocs](https://jd.papermc.io/paper/26.3/) still identified build 40, so build 41's [source artifact](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/26.3.build.41-alpha/paper-api-26.3.build.41-alpha-sources.jar) and [Javadoc artifact](https://repo.papermc.io/repository/maven-public/io/papermc/paper/paper-api/26.3.build.41-alpha/paper-api-26.3.build.41-alpha-javadoc.jar) were also inspected. Reviewed JavaPlugin lifecycle, BlockBreakEvent, EventHandler priority/cancellation behavior, ItemStack APIs, deprecations and published 26.3 build changes. No 26.3 compatibility source migration was required; existing ItemStack constructors remain supported but are marked obsolete in favor of `ItemStack.of`, as discussed in the roadmap. Broader inventory changes are outside this target upgrade.
