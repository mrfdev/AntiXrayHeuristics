# JDK 25 and Java 26 compatibility verification

Verified on 2026-09-15 for `1MB-XRayHeuristics-v2.0.3-031-j25-26.2.jar` on macOS ARM64.

## Build and runtime targets

| Component | Verified version |
| --- | --- |
| Gradle | `9.7.1` |
| Build JVM, compiler and default test JVM | Oracle JDK `25.0.4.1+1-LTS-5` |
| Additional test and server JVM | Oracle JDK `26.0.2.1+1-7` |
| Plugin bytecode | Java `25`, class-file major `69`, no preview features |
| Paper server | `26.2-121-main@a2a42c5`, stable |
| Paper compile API | `26.2.build.121-stable` |
| CoreProtect | `25.0-rc1`, API `13` |
| Storage exercised | X-ray Heuristics JSON; CoreProtect DuckDB |

The release advances the existing `2.0.2` / `030` release identity once to `2.0.3` / `031`. Repeated checks retain that identity. Paper remains at `26.2`, build `121`; the Java target and declared Bukkit API floor (`1.21.11`) are unchanged.

`scripts/rebuild.sh` selects `/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home` through `JAVA_HOME` and prepends its `bin` directory to `PATH`, then runs the canonical command:

```bash
gradle clean build printBuildConfig --warning-mode all
```

`scripts/test-java26.sh` keeps the same build JVM and compiler, sets `JAVA26_HOME=/Library/Java/JavaVirtualMachines/jdk-26.0.2.1.jdk/Contents/Home`, and runs `gradle testJava26 printBuildConfig --warning-mode all`. The complete test suite uses Java 26 against the same Java 25 compiled classes. `gradle.properties` discovers toolchains from these explicit environment variables and disables automatic discovery and downloads.

## Results

| Check | JDK 25.0.4.1 | JDK 26.0.2.1 |
| --- | --- | --- |
| Full JUnit suite | 11 passed, 0 failed/skipped | 11 passed, 0 failed/skipped |
| Paper startup and plugin enable | Passed | Passed |
| Commands, CoreProtect hook and reload | Passed | Passed |
| Mining listener and plugin API probe | Passed | Passed |
| Restart with existing plugin data | Passed | Passed |
| Console stop, plugin disable and world save | Passed; exit `0` | Passed; exit `0` |

- The clean build passed compilation, packaging, all tests and `verifyReleaseMetadata`. Build output confirmed JDK `25.0.4.1+1-LTS-5` for the build JVM, compiler and default test launcher. The additional Gradle run confirmed the test executor used JDK `26.0.2.1`.
- All 58 project classes in the release JAR use major version `69`; bundled dependency classes are also compatible with Java 25. The JAR contains no CoreProtect classes. Packaged `plugin.yml` and `xrayheuristics/build-info.properties` report the release and pinned targets above.
- An isolated server bound to `127.0.0.1:25595` loaded the same release JAR on both runtimes. The initial fresh startup generated config, locale, weights and JSON storage files.
- Each of four completed smoke runs (startup and restart per JDK) exercised `version xrayheuristics`, `version CoreProtect`, `/xrayer info`, `/xrayer help`, `/xrayer debug`, `/xrayer reload`, and `/xrayer debug` again. Diagnostics confirmed the expected Java version, Paper API, JSON storage and enabled CoreProtect hook.
- The temporary probe ran on Paper's server thread and invoked the plugin's registered mining listener using real world blocks and a synthetic test player. Stone mining created a Suspicion Session; diamond mining increased suspicion from `0.0` to `45.0`. Reload retained the session and CoreProtect hook. Both canonical and legacy plugin APIs were available. The probe restored its block and removed its temporary session.
- All four runs disabled both plugins, saved all world dimensions and exited with code `0`, with no server errors. SHA-256 hashes of `config.yml`, `locale.yml`, `weights.yml` and `data.json` remained identical through both restarts and the Java 25 to Java 26 switch.

The first harness attempt missed a successful, ANSI-colored version response. The runner was corrected to strip terminal formatting; all four complete checks were then rerun successfully. That attempt also stopped cleanly, and its log is retained separately. No plugin fix was required.

Paper's bundled OSHI library warned that it could not map macOS `27.0` to a codename, and JOML emitted its `sun.misc.Unsafe` deprecation warning. Paper also noted that build `121` was two builds behind. The pinned Paper build and generated exploit-protection defaults were retained. This verification covers the local plugin lifecycle and mining integration; connected-player inventory/vault interactions, MySQL and the live server's full plugin set were not exercised.

## Artifacts and evidence

- Release JAR: `build/libs/1MB-XRayHeuristics-v2.0.3-031-j25-26.2.jar`
- JAR SHA-256: `a1a0b7f9c617313729d59f2c17ffdb592de6601da6d14677f5829fe367ec947d`
- JUnit reports: `build/reports/tests/test/index.html` and `build/reports/tests/testJava26/index.html`
- Retained local server, release copy, probe source, runner, logs, JUnit evidence and `smoke-results.json`: gitignored `servers/compat-jdk25041-jdk26021-paper121/`
- CoreProtect JAR SHA-256: `89a11ccba48140e5c0bb519364792077f633aa10ce8bad8f9169ab9e74997e32`
- Paper JAR SHA-256: `0de30efb024bc8b83c9c7d507d11802897ad8056b6110ec09fe1a91d126ccb54`, matching the [official build 121 metadata](https://fill.papermc.io/v3/projects/paper/versions/26.2/builds/121).

The [2026-09-04 CoreProtect verification](compatibility-coreprotect-25.md) and its original server logs retain their original JDK and artifact values.

Documentation discovery used the live [PaperMC LLM index](https://docs.papermc.io/llms.txt), followed by the official [project setup](https://docs.papermc.io/paper/dev/project-setup/), [runtime requirements](https://docs.papermc.io/paper/getting-started/) and [scheduling](https://docs.papermc.io/paper/dev/scheduler/) guides. The [Paper 26.2 Javadocs](https://jd.papermc.io/paper/26.2/) now identify build `123`; the actual build `121` API JAR was inspected with `javap` for the probe's event, listener and scheduler signatures. Toolchain configuration follows the [Gradle 9.7.1 toolchain documentation](https://docs.gradle.org/9.7.1/userguide/toolchains.html).
