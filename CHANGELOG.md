# Changelog

## 2.0.5, build 033 (2026-10-11)

- Pin development, compilation and compatibility testing to Paper 26.3 build 175 STABLE, verified through Fill and Maven metadata. Keep Minecraft/API floor 26.3, Java 25 bytecode and CoreProtect 25.0 API 13.
- Use Paper's published runtime API dependencies and commit resolved Gradle dependency locks. Validate stable channel, manifest API floor and current documentation during builds.
- Observe completed, uncancelled block breaks at MONITOR priority so cancelled mining cannot create suspicion sessions.
- Deserialize handled-player JSON through its constructor to support Java 27 final-field restrictions while preserving existing stored data and public fields.
- Add packaging, bytecode and JSON regression coverage, plus a disposable server probe for permissions, mining, item metadata, asynchronous JSON work, restart/persistence and missing CoreProtect.
- Update the maintained local development server pin/channel and preserve the dated alpha verification separately. Keep historical instances and the shared deployment destination untouched.
