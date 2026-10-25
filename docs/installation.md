# Installation

## Requirements

- Paper `26.3` server, pinned to build `41` (`ALPHA`)
- Installed Oracle JDK `25` for Gradle and compilation; installed Oracle JDK `27` for all automated tests and the maintained test server
- Installed Gradle `9.8.0` or newer for Java 27 toolchains (this Mac has `9.8.2`)
- CoreProtect installed before `1MB XRayHeuristics`
- A build of this plugin produced from the current Gradle setup

Verified build metadata from this repository:

- Current release: `2.0.4-032-j25-26.3`
- Semantic version: `2.0.4`
- Build number: `032`
- Gradle and compiler JDK: installed Oracle JDK `25`
- Automated test JDK: installed Oracle JDK `27`
- Java bytecode target: `25`
- Maintained server runtime: installed Oracle JDK `27`
- Paper compile target: `26.3.build.41-alpha`
- Declared `plugin.yml` api-version floor: `26.3`
- CoreProtect compile target: `25.0` with API `13`
- Minimum runtime CoreProtect API accepted by code: `11`

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
build/libs/1MB-XRayHeuristics-v2.0.4-032-j25-26.3.jar
```

Release metadata comes from `version.properties`. Increment its semantic version and build number once when preparing a release. Repeated builds keep the same release identity, and `./scripts/gradle.sh check` runs the metadata drift validation.

### Local verification on 2026-10-25

The results below were collected from the full local working tree, including pre-existing uncommitted Paper migration and smoke-test work. That work is separate from the Java toolchain change.

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

`servers/Server-Two-Paper-26.3/` is a separate copy of the stopped `servers/Server-Two-Paper-26.2/`. Keep the original and historical compatibility instances intact for rollback. The new instance binds to `127.0.0.1:25743`; query UDP `25744` and RCON TCP `25745` are reserved but disabled. Recheck all ports before startup. Its session name is `axh-paper-26.3-25743`.

Build with Java 25 and copy the verified release JAR into the new instance's `plugins/`, replacing only the previous `xrayheuristics` JAR. Start the new instance using its `./1MB-minecraft.sh`, or `./1MB-start.sh` for the distinct tmux session. The launcher selects Java 27 explicitly. From that server directory in Bash, the equivalent validated selection is:

```bash
source ../../scripts/java-env.sh
export JAVA_HOME="$ONE_MB_TEST_JAVA_HOME"
export PATH="$JAVA_HOME/bin:$PATH"
java -Xms512M -Xmx2G -XX:ActiveProcessorCount=2 -jar Paper-26.3.jar --nogui
```

PaperScript has fresh local state, explicit `ALPHA` channels and automatic cross-version/build upgrades disabled. To reproduce the pin from the new instance, use `./paperscript.sh --yes download --version 26.3 --build 41 --channel ALPHA`, then `./paperscript.sh verify`. Check its server directory and checksum before launch. Do not copy launcher state or download caches from another instance.

Keep DiscordSRV and other messaging integrations inactive. Run the readiness commands above, `/xrayer reload`, and `/xrayer debug` again. Confirm Java 27, the pinned Paper API and an enabled CoreProtect hook, then issue console `stop` and wait for exit. Restart with the existing data and repeat. Keep exploit protections at safe defaults.

The [Paper 26.3 verification](compatibility-paper-26.3.md) records exact results. Earlier verification documents retain their original targets. The shared server at `/Users/floris/MinecraftServer/test-1mb-3.14-mc-26.3/` is only a stopped deployment destination; never launch it for these checks. Replace only JARs with manifest identity `xrayheuristics`, retain recoverable backups outside the active plugin directory, and verify destination checksums and stopped state afterward.
