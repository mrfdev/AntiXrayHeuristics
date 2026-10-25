## Agent skills

### Issue tracker

Issues and specs are tracked in this repository’s GitHub Issues. See `docs/agents/issue-tracker.md`.

### Domain docs

This repository uses a single-context domain documentation layout. See `docs/agents/domain.md`.

## Maintained build and test target

- Release metadata lives in `version.properties`. Reserve one semantic patch and one build number per release; reuse them after failed builds.
- Run Gradle and compile all plugin, test and smoke-probe sources with installed Oracle JDK 25 and `--release 25`. Run every automated test JVM, including forked test processes and disposable Paper servers, with installed Oracle JDK 27.
- Use `./scripts/rebuild.sh` for the canonical clean build, full default test suite, generated metadata, bytecode verification and documentation drift checks. Use `./scripts/gradle.sh <tasks/options>` for other Gradle operations. `./scripts/test-java27.sh` reruns the full suite with a separate report; the default test task already uses Oracle 27.
- `scripts/java-env.sh` resolves installations on every invocation with `/usr/libexec/java_home -F -v 25` and `-v 27`. Optional `ONE_MB_BUILD_JAVA_HOME` and `ONE_MB_TEST_JAVA_HOME` select other installed homes. Validate actual Java/compiler major versions and Oracle vendor before building; never hardcode patch-version paths, download JDKs or fall back to another vendor/version.
- The launcher pins Gradle's JVM and toolchain discovery, disables automatic JDK detection/downloads, rejects Java property overrides and stale daemon JVM criteria, and supersedes user Gradle Java properties. Direct `gradle`/IDE builds that bypass its validated environment and properties are guarded; use the project launcher. Keep installed Gradle at 9.8.0 or newer for Java 27 toolchains (this Mac has 9.8.2).
- Keep all `JavaCompile` and `Test` toolchains explicit. `check` includes `verifyJavaBytecode` for every compiled main/test/smoke class (Java 25 class-file version 69.0). `printBuildConfig` reports actual build/compiler/test JVM paths, versions and vendors. See `docs/installation.md` for commands and evidence locations.
- Target Paper `26.3.build.41-alpha` and CoreProtect `25.0` API `13`. The compile-support JAR is gitignored at `servers/compile-support/CoreProtect-25.0.jar`, or set `-PcoreProtectJar` explicitly. Do not silently fall back to 26.2 or an older dependency.
- Start documentation discovery at the live https://docs.papermc.io/llms.txt, then check official docs and https://jd.papermc.io/paper/26.3/. When the published Javadocs lag the pin, inspect the exact Maven sources/Javadoc artifact as well.
- The maintained local instance is `servers/Server-Two-Paper-26.3/`, using the dynamically resolved and validated Oracle JDK 27, TCP `25743`, disabled query UDP `25744`, disabled RCON TCP `25745`, and session `axh-paper-26.3-25743`. Recheck port availability before startup. Its launchers and PaperScript state must refer only to that directory, with channel `ALPHA`.
- Keep `servers/Server-Two-Paper-26.2/` and the historical compatibility instances intact. Never commit server JARs, worlds, databases, logs or caches. Keep messaging integrations inactive during local smoke tests.
- See `docs/compatibility-paper-26.3.md` for upgrade evidence and remaining manual checks. The shared test server is a deployment destination only: replace artifacts whose manifest identity is `xrayheuristics`, preserve other projects, verify it is stopped before and after, and never start it as part of deployment.
