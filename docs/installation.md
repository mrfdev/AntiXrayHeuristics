# Installation

## Requirements

- Paper `26.3` server, pinned to build `41` (`ALPHA`)
- Java `27` for the maintained test server; Java `25.0.4.1` for compilation and default tests
- CoreProtect installed before `1MB XRayHeuristics`
- A build of this plugin produced from the current Gradle setup

Verified build metadata from this repository:

- Current release: `2.0.4-032-j25-26.3`
- Semantic version: `2.0.4`
- Build number: `032`
- Build JDK: Oracle JDK `25.0.4.1`
- Java bytecode target: `25`
- Maintained server runtime: Oracle Java `27+35-2325`
- Paper compile target: `26.3.build.41-alpha`
- Declared `plugin.yml` api-version floor: `26.3`
- CoreProtect compile target: `25.0` with API `13`
- Minimum runtime CoreProtect API accepted by code: `11`

## Build From Source

Copy `CoreProtect-25.0-26.3.jar` from the supplied local dependency directory to `servers/compile-support/CoreProtect-25.0.jar` (gitignored), then run:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
gradle clean build printBuildConfig --warning-mode all
```

The equivalent shortcut is `./scripts/rebuild.sh`. Both commands select the build JDK through `JAVA_HOME` and prepend its `bin` directory to `PATH`.

After the full build, run `./scripts/test-java27.sh` to execute all tests on Oracle JDK `27`. This keeps the compiler on JDK `25.0.4.1` and selects Java 27 only for the `testJava27` task through `JAVA27_HOME`. Gradle uses these explicit environment variables for toolchain discovery; automatic discovery and downloads are disabled. On another machine, set `JAVA_HOME` to its Java 25 installation and `JAVA27_HOME` to its Java 27 installation, then invoke the same Gradle tasks directly.

For another jar location, append `-PcoreProtectJar=/absolute/path/to/CoreProtect-25.0.jar`. The build requires the specified CoreProtect release; it does not silently fall back to an older dependency. `gradle check` verifies the jar's version and actual API version against `version.properties`.

The release jar is:

```text
build/libs/1MB-XRayHeuristics-v2.0.4-032-j25-26.3.jar
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

## Maintained Paper 26.3 Test Instance

`servers/Server-Two-Paper-26.3/` is a separate copy of the stopped `servers/Server-Two-Paper-26.2/`. Keep the original and historical compatibility instances intact for rollback. The new instance binds to `127.0.0.1:25743`; query UDP `25744` and RCON TCP `25745` are reserved but disabled. Recheck all ports before startup. Its session name is `axh-paper-26.3-25743`.

Build with Java 25 and copy the verified release JAR into the new instance's `plugins/`, replacing only the previous `xrayheuristics` JAR. Start the new instance using its `./1MB-minecraft.sh`, or `./1MB-start.sh` for the distinct tmux session. The launcher selects Java 27 explicitly; the equivalent command is:

```bash
export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home
export PATH="$JAVA_HOME/bin:$PATH"
java -Xms512M -Xmx2G -XX:ActiveProcessorCount=2 -jar Paper-26.3.jar --nogui
```

PaperScript has fresh local state, explicit `ALPHA` channels and automatic cross-version/build upgrades disabled. To reproduce the pin from the new instance, use `./paperscript.sh --yes download --version 26.3 --build 41 --channel ALPHA`, then `./paperscript.sh verify`. Check its server directory and checksum before launch. Do not copy launcher state or download caches from another instance.

Keep DiscordSRV and other messaging integrations inactive. Run the readiness commands above, `/xrayer reload`, and `/xrayer debug` again. Confirm Java 27, the pinned Paper API and an enabled CoreProtect hook, then issue console `stop` and wait for exit. Restart with the existing data and repeat. Keep exploit protections at safe defaults.

The [Paper 26.3 verification](compatibility-paper-26.3.md) records exact results. Earlier verification documents retain their original targets. The shared server at `/Users/floris/MinecraftServer/test-1mb-3.14-mc-26.3/` is only a stopped deployment destination; never launch it for these checks. Replace only JARs with manifest identity `xrayheuristics`, retain recoverable backups outside the active plugin directory, and verify destination checksums and stopped state afterward.
