## Agent skills

### Issue tracker

Issues and specs are tracked in this repository’s GitHub Issues. See `docs/agents/issue-tracker.md`.

### Domain docs

This repository uses a single-context domain documentation layout. See `docs/agents/domain.md`.

## Maintained build and test target

- Release metadata lives in `version.properties`. Reserve one semantic patch and one build number per release; reuse them after failed builds.
- Compile and run the default tests with `/Library/Java/JavaVirtualMachines/jdk-25.0.4.1.jdk/Contents/Home`, selected through `JAVA_HOME` and `PATH`. Keep toolchains and bytecode at Java 25.
- Run `./scripts/rebuild.sh` for the clean build, tests, generated metadata and documentation drift checks. Run `./scripts/test-java27.sh` for the additional runtime test suite.
- Target Paper `26.3.build.41-alpha` and CoreProtect `25.0` API `13`. The compile-support JAR is gitignored at `servers/compile-support/CoreProtect-25.0.jar`, or set `-PcoreProtectJar` explicitly. Do not silently fall back to 26.2 or an older dependency.
- Start documentation discovery at the live https://docs.papermc.io/llms.txt, then check official docs and https://jd.papermc.io/paper/26.3/. When the published Javadocs lag the pin, inspect the exact Maven sources/Javadoc artifact as well.
- The maintained local instance is `servers/Server-Two-Paper-26.3/`, using Java 27 at `/Library/Java/JavaVirtualMachines/jdk-27.jdk/Contents/Home`, TCP `25743`, disabled query UDP `25744`, disabled RCON TCP `25745`, and session `axh-paper-26.3-25743`. Recheck port availability before startup. Its launchers and PaperScript state must refer only to that directory, with channel `ALPHA`.
- Keep `servers/Server-Two-Paper-26.2/` and the historical compatibility instances intact. Never commit server JARs, worlds, databases, logs or caches. Keep messaging integrations inactive during local smoke tests.
- See `docs/compatibility-paper-26.3.md` for upgrade evidence and remaining manual checks. The shared test server is a deployment destination only: replace artifacts whose manifest identity is `xrayheuristics`, preserve other projects, verify it is stopped before and after, and never start it as part of deployment.
