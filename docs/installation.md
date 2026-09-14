# Installation

## Requirements

- Paper `26.2` server
- Java `25` or `26` (live runs Java `26`)
- CoreProtect installed before `1MB XRayHeuristics`
- A build of this plugin produced from the current Gradle setup

Verified build metadata from this repository:

- Current release: `2.0.3-031-j25-26.2`
- Semantic version: `2.0.3`
- Build number: `031`
- Build JDK: Oracle JDK `25.0.4.1`
- Java bytecode target: `25`
- Supported runtime JDKs: Oracle JDK `25.0.4.1` and `26.0.2.1`
- Live runtime: Java `26`
- Paper compile target: `26.2.build.121-stable`
- Declared `plugin.yml` api-version floor: `1.21.11`
- CoreProtect compile target: `25.0-rc1` with API `13`
- Minimum runtime CoreProtect API accepted by code: `11`

## Build From Source

Place `CoreProtect-25.0-rc1.jar` in `~/Downloads`, then run:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
gradle clean build printBuildConfig --warning-mode all
```

The equivalent shortcut is `./scripts/rebuild.sh`. Both commands select the build JDK through `JAVA_HOME` and prepend its `bin` directory to `PATH`.

After the full build, run `./scripts/test-java26.sh` to execute all tests on Oracle JDK `26.0.2.1`. This keeps the compiler on JDK `25.0.4.1` and selects Java 26 only for the `testJava26` task through `JAVA26_HOME`. Gradle uses these explicit environment variables for toolchain discovery; automatic discovery and downloads are disabled. On another machine, set `JAVA_HOME` to its Java 25 installation and `JAVA26_HOME` to its Java 26 installation, then invoke the same Gradle tasks directly.

For another jar location, append `-PcoreProtectJar=/absolute/path/to/CoreProtect-25.0-rc1.jar`. The build requires the specified CoreProtect release; it does not silently fall back to an older dependency. `gradle check` verifies the jar's version and actual API version against `version.properties`.

The release jar is:

```text
build/libs/1MB-XRayHeuristics-v2.0.3-031-j25-26.2.jar
```

Release metadata comes from `version.properties`. Increment its semantic version and build number once when preparing a release. Repeated builds keep the same release identity, and `gradle check` runs the metadata drift validation.

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

## Verify Both Java Runtimes

Build once with JDK `25.0.4.1` and use that same release jar for both runtime checks. In an isolated Paper `26.2` server directory with CoreProtect installed, select the Java 25 JDK with the `JAVA_HOME` and `PATH` exports above, then start Paper:

```bash
java -Xms512M -Xmx1G -jar paper-26.2-121.jar --nogui
```

Run the readiness commands above, `/xrayer reload`, and `/xrayer debug` again. Confirm the expected runtime version and an enabled CoreProtect hook, then issue console `stop` and wait for the process to exit. Restart with the existing data and repeat the checks.

Repeat with the Java 26 runtime used by live, using the same plugin jar and data:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-26.0.2.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -Xms512M -Xmx1G -jar paper-26.2-121.jar --nogui
```

The [JDK compatibility verification](compatibility-jdk-25-26.md) records the release checksum, exact runtimes, test counts, mining probe and shutdown results. The earlier [CoreProtect 25 verification](compatibility-coreprotect-25.md) remains a historical record of its original JDK and artifact.
