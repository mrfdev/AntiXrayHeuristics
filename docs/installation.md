# Installation

## Requirements

- Paper `26.3` server, pinned to build `175` (`STABLE`)
- Installed Oracle JDK `25` for Gradle and compilation; installed Oracle JDK `27` for all automated tests and the maintained test server
- Installed Gradle `9.8.0` or newer for Java 27 toolchains (this Mac has `9.8.2`)
- CoreProtect installed before `1MB XRayHeuristics`
- A build of this plugin produced from the current Gradle setup

Verified build metadata from this repository:

- Current release: `2.0.5-033-j25-26.3`
- Semantic version: `2.0.5`
- Build number: `033`
- Gradle and compiler JDK: installed Oracle JDK `25`
- Automated test JDK: installed Oracle JDK `27`
- Java bytecode target: `25`
- Maintained server runtime: installed Oracle JDK `27`
- Paper compile target: `26.3.build.175-stable`
- Declared `plugin.yml` api-version floor: `26.3`
- CoreProtect compile target: `25.0` with API `13`
- Minimum runtime CoreProtect API accepted by code: `11`

Paper 26.3 requires Java 25 or newer. This project compiles all plugin, test and smoke-probe sources with Oracle JDK 25 and `--release 25`, while every automated test and maintained test server runs on Oracle JDK 27. The published compile dependency is `io.papermc.paper:paper-api:26.3.build.175-stable`; `plugin.yml` continues to declare the Minecraft version `26.3`.

## Build From Source

Copy `CoreProtect-25.0-26.3.jar` from the supplied local dependency directory to `servers/compile-support/CoreProtect-25.0.jar` (gitignored), then run:

```bash
./scripts/rebuild.sh
```

This is the canonical clean build. It runs `clean build printBuildConfig --warning-mode all` through `scripts/gradle.sh`. The shared `scripts/java-env.sh` resolves installed JDK homes afresh with `/usr/libexec/java_home -F -v 25` and `-v 27`. Before Gradle starts, it checks executable `java` and `javac`, actual Java/compiler major versions and the `Oracle Corporation` vendor. It fails if the required installation is missing or mismatched. It never downloads a JDK or falls back to another major version or vendor.

Gradle itself and every `JavaCompile` task use the selected Oracle 25 installation. Every `Test` task uses the selected Oracle 27 installation, including forked test workers. Test and disposable-server processes receive Oracle 27 through `JAVA_HOME` and the start of `PATH`, so their child Java processes use the same runtime. Java 27 test runs deny illegal reflective final-field mutation.

For a deliberate installed-home selection, set `ONE_MB_BUILD_JAVA_HOME=/path/to/installed/oracle-25/home` and/or `ONE_MB_TEST_JAVA_HOME=/path/to/installed/oracle-27/home` before running the scripts. Overrides receive the same validation. Ordinary macOS use needs neither override, and a new Oracle 25/27 patch installation requires no file edits. The shell's existing `JAVA_HOME`, `JAVA27_HOME` or default `java` does not select these toolchains.

Use the project launcher for individual tasks:

```bash
./scripts/gradle.sh test printBuildConfig
./scripts/gradle.sh check
./scripts/test-java27.sh
```

`test-java27.sh` retains the separate `testJava27` full-suite report; both it and the default `test` task use Oracle 27. Reports are under `build/reports/tests/test/` and `build/reports/tests/testJava27/`, with XML results under `build/test-results/`. `printBuildConfig` reports the actual build JVM, compiler and test launcher paths, versions and vendors. `check` also runs `verifyJavaBytecode`, which inspects every compiled main, test and smoke-probe class for Java 25 class-file version `69.0` without preview features.

The launcher pins Gradle's Java home and toolchain discovery settings in both command-line property forms, superseding stale user/distribution `gradle.properties`. It disables automatic JDK detection and downloads, starts without reusing a daemon, and rejects Java/Gradle JVM property overrides and a stale `gradle/gradle-daemon-jvm.properties`. Use the installed-home environment overrides above instead of `-D`/`-P` Java settings. `settings.gradle` checks the actual Gradle JVM and effective properties before configuring the build, so direct `gradle` and IDE invocations that bypass the launcher are unsupported and guarded. Gradle 9.8.2 already meets the required minimum; no tooling upgrade is needed. See Gradle's official [Java compatibility matrix](https://docs.gradle.org/current/userguide/compatibility.html) and [toolchain configuration](https://docs.gradle.org/current/userguide/toolchains.html).

For another jar location, append `-PcoreProtectJar=/absolute/path/to/CoreProtect-25.0.jar`. The build requires the specified CoreProtect release; it does not silently fall back to an older dependency. `./scripts/gradle.sh check` verifies the jar's version and actual API version against `version.properties`.

The release jar is:

```text
build/libs/1MB-XRayHeuristics-v2.0.5-033-j25-26.3.jar
```

Release metadata comes from `version.properties`. Increment its semantic version and build number once when preparing a release. Repeated builds keep the same release identity, and `./scripts/gradle.sh check` runs the metadata drift validation.

### Local verification on 2026-10-25

The results below cover the combined Java toolchain, Paper migration and smoke-test changes. The Java toolchain setup and the Paper migration are recorded in separate local commits.

These paths record this Mac's selected installations, not configuration values to copy into scripts:

| Role | Actual version and vendor | Resolved home |
| --- | --- | --- |
| Gradle 9.8.2 and all compilers | `25.0.5.0.3+1-LTS-2`, Oracle Corporation | `/Library/Java/JavaVirtualMachines/jdk-25.0.5.0.3.jdk/Contents/Home` |
| Test workers, forked Java and Paper tests | `27.0.1.0.3+1-2`, Oracle Corporation | `/Library/Java/JavaVirtualMachines/jdk-27.0.1.0.3.jdk/Contents/Home` |

`./scripts/rebuild.sh smokeTestJar --offline --console=plain` passed the clean compile, packaging, 17 tests and all verification tasks. `./scripts/test-java27.sh --offline --console=plain` passed the same 17 tests in its separate report. Neither suite had failures, errors or skipped tests. The runtime test checks the actual worker's version/vendor/home, inherited environment and a child Java process.

All 59 plugin classes, 7 test classes and 2 probe classes have class-file version `69.0` (Java 25, no preview). Independent JAR inspection confirmed all 59 packaged project classes match the verified output. Build logs and a machine-readable summary are retained locally under the ignored `.gradle/java-toolchain-verification/`; detailed JUnit reports remain under `build/`.

Negative checks confirmed that missing homes, wrong major versions, a non-Oracle vendor and conflicting command-line/environment Java settings fail before Gradle launches. Explicit valid installed-home overrides also passed. The local audit found no global Gradle properties/init scripts, daemon JVM criteria or Maven toolchain configuration; stale paths in project scripts and the maintained server launcher were replaced. Existing release metadata and unrelated work were preserved.

The disposable Paper run passed startup and restart with 126 probe assertions each, unchanged persistence hashes, and the expected missing-CoreProtect failure. All three servers exited cleanly and released their ports. Evidence is in `servers/paper-26.3-stable-033-tlf2schl/smoke-results.json` and its adjacent logs. The maintained server launcher passed shell syntax validation but that instance was not started. Player-client scenarios, a real MySQL integration and the broader manual checks in the [compatibility report](compatibility-paper-26.3.md) remain outside this automated run. No deployment or remote repository changes were made.

## Install On A Server

1. Stop the server.
2. Make sure CoreProtect is already installed.
3. Place the newest `1MB-XRayHeuristics` jar in the server's `plugins/` folder.
4. Start the server.
5. Confirm the plugin creates `plugins/1MB-XRayHeuristics/`.

## First-Run Output And Files

The plugin creates or maintains:

- `plugins/1MB-XRayHeuristics/config.yml`
- `plugins/1MB-XRayHeuristics/locale.yml`
- `plugins/1MB-XRayHeuristics/weights.yml`
- `plugins/1MB-XRayHeuristics/data.json` when `StorageType: JSON`

## Recommended Readiness Checks

After startup, verify:

- `version xrayheuristics`
- `/xrayer info`
- `/xrayer help`
- `/xrayer debug`

`/xrayer debug` is the best built-in hook check because it reports:

- runtime Java version
- server version
- exact compiled Paper API and channel
- compiled Java bytecode target
- CoreProtect hook status
- CoreProtect version and API
- config, locale, and weights file paths
- current storage mode

## Updates

1. Stop the server.
2. Replace the old jar with the new jar.
3. Keep your existing `plugins/1MB-XRayHeuristics/` data directory.
4. Start the server.
5. Run `/xrayer info` and `/xrayer debug` again.

If you changed storage settings, restart fully instead of relying on `/xrayer reload`.

## Maintained Paper 26.3 Test Instance

`servers/Server-Two-Paper-26.3/` is the maintained development instance, originally copied from `servers/Server-Two-Paper-26.2/`. Keep the original and historical compatibility instances intact for rollback. The maintained instance binds to `127.0.0.1:25743`; query UDP `25744` and RCON TCP `25745` are reserved but disabled. Recheck all ports before startup. Its session name is `axh-paper-26.3-25743`.

Build with Java 25 and copy the verified release JAR into the new instance's `plugins/`, replacing only the previous `xrayheuristics` JAR. Start the new instance using its `./1MB-minecraft.sh`, or `./1MB-start.sh` for the distinct tmux session. The launcher selects Java 27 explicitly. From that server directory in Bash, the equivalent validated selection is:

```bash
source ../../scripts/java-env.sh
export JAVA_HOME="$ONE_MB_TEST_JAVA_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
java -Xms512M -Xmx2G -XX:ActiveProcessorCount=2 -jar Paper-26.3.jar --nogui
```

PaperScript has local state, explicit `STABLE` channels and automatic cross-version/build upgrades disabled. The maintained instance is installed at build `175`; `./paperscript.sh verify` checks it against Fill. For an intentional upgrade from an older 26.3 build, first verify the instance is stopped, temporarily set `allow_same_version_build_upgrade` to `true`, run `./paperscript.sh --yes download --version 26.3 --build 175 --channel STABLE`, then restore that setting to `false` and run `./paperscript.sh verify`. This PaperScript version checks the same-version setting even for explicit downloads. Keep `running_server_action: abort`, confirm the directory/checksum, and preserve its backup. Do not copy launcher state or download caches from another instance.

Keep DiscordSRV and other messaging integrations inactive. Run the readiness commands above, `/xrayer reload`, and `/xrayer debug` again. Confirm Java 27, the pinned Paper API and an enabled CoreProtect hook, then issue console `stop` and wait for exit. Restart with the existing data and repeat. Keep exploit protections at safe defaults.

The [Paper 26.3 verification](compatibility-paper-26.3.md) records exact results. Dated verification documents and deployment receipts retain their original targets as historical evidence.

## Deployment Destinations

The temporary shared test at `/Users/floris/MinecraftServer/test-1mb-3.14-mc-26.3/` has ended and its server directory was removed. There is no configured shared deployment destination. Historical references to that path do not identify an active server, and deployment does not include recreating it.

The project-local development instance described above and the disposable smoke servers described below are separate from the retired shared server. For a future deployment, use the destination identified for that task. Confirm it is stopped, replace only JARs with manifest identity `xrayheuristics`, preserve other plugins and data, retain recoverable backups outside the active plugin directory, and verify the deployed checksum and stopped state afterward. Leave the server stopped unless startup is explicitly requested.

## Disposable Stable Compatibility Tests

Use the tracked runner for migration checks without loading existing worlds, data or optional integrations:

```bash
./scripts/rebuild.sh smokeTestJar
./scripts/test-java27.sh
python3 scripts/test-paper-stable.py
```

The runner requires Python 3.9+, network access for official Paper metadata/dependencies, the compile-support CoreProtect JAR, and the installed Oracle JDKs used by the build. It calls the same `scripts/java-env.sh` resolver and validator, then launches Oracle 27; `ONE_MB_TEST_JAVA_HOME` can select another installed Oracle 27 home. The selected Java executable, vendor, version and properties are retained in `runtime.txt` and `smoke-results.json`. It reuses the existing locally accepted EULA file from the maintained instance; on another machine pass `--eula-file /path/to/an/already-accepted/eula.txt`. It does not accept a new EULA on the user's behalf.

Every invocation creates a new `servers/paper-26.3-stable-033-*` directory, verifies build `175` is `STABLE`, and verifies its JAR against `paperServerSha256` and Fill. `--paper-jar /path/to/paper-26.3-175.jar` can reuse a cached JAR with the same checks; `--coreprotect-jar` selects another copy of the required CoreProtect release. The runner copies only the three test JARs, creates fresh configuration and worlds, disables metrics/profiling, and uses loopback with an empty enforced whitelist. It starts in offline mode for this clientless test only. Query and RCON remain disabled. Ports `25843`/`25844`/`25845` are checked before each run and after shutdown; use `--port` to select a different three-port range.

The probe calls the actual registered mining listener using synthetic players, checks command denial, round-trips PDC/data components and a shulker-box item, and exercises JSON insert/read/delete callbacks and a persistent record. The runner executes status/help/reload commands, restarts with existing disposable data and compares file hashes, then verifies that removing CoreProtect prevents this plugin from loading. Java 27 runs use `--illegal-final-field-mutation=deny`. Logs and `smoke-results.json` remain in the disposable directory; failures also shut down the process owned by the runner. The test probe is built separately under `build/smoke/` and must never be installed on a live server.

These synthetic checks do not replace a player-client inventory/handling test or a MySQL integration test. Follow the concrete checklist in [the compatibility report](compatibility-paper-26.3.md#remaining-checks-and-existing-limits).
