# AGENTS.md

Guidance for coding agents working in this repository.

## Project Snapshot

This is a multi-module Android SDK repository for Guance mobile monitoring.

- `ft-sdk`: core Android RUM/log/trace/session replay integration library.
- `ft-plugin`: Gradle plugin used by the SDK tooling; published as a Java/Groovy artifact.
- `ft-native`: Android library wrapper for native components.
- `ft-session-replay`, `ft-session-replay-material`, `ft-session-replay-compose`: session replay libraries derived from Datadog session replay components.
- `ft-test`: shared Android test support.
- `app`: sample/test Android application. It may depend on local files and credentials that are intentionally not committed.

## Build Environment

- Use the checked-in Gradle wrapper: `./gradlew`.
- Android Gradle Plugin is configured from the root `build.gradle`.
- Java compatibility differs by module:
  - Android library modules use Java 8 source/target compatibility.
  - `ft-plugin` uses Java 11 source/target compatibility.
- `local.properties` may contain private runtime and publishing values such as `LDAP_ACCOUNT`, `LDAP_PWD`, `RUM_APP_ID`, tokens, URLs, and proxy settings. Do not commit or print secrets.

## Common Commands

Run targeted commands first when changing one module.

- Core SDK unit tests: `./gradlew :ft-sdk:testDebugUnitTest`
- Core SDK assemble: `./gradlew :ft-sdk:assemble`
- Native module assemble: `./gradlew :ft-native:assemble`
- Session replay assemble: `./gradlew :ft-session-replay:assemble`
- Material replay assemble: `./gradlew :ft-session-replay-material:assemble`
- Compose replay assemble: `./gradlew :ft-session-replay-compose:assemble`
- Plugin build: `./gradlew :ft-plugin:build`
- Full build check, when practical: `./gradlew build`

Publishing tasks exist, but require credentials and should only be run when explicitly requested:

- Android AAR publish task: `generateAarAndPublish`
- Plugin publish task: `:ft-plugin:generateJarAndPublish`

## Versioning And Release Notes

- Root SDK versions live in `build.gradle` under `ext.sdkVersions`.
- `ftPlugin` is the Plugin artifact version to publish. `ftPluginBuildscript` is the already-published
  Plugin version used to configure the root build. `pluginMinAgentSupport` is embedded into the
  published Plugin as its minimum compatible Agent version.
- Each publishable module keeps a local `CHANGELOG.md`; update the relevant changelog for release-facing changes.
- Keep version bumps scoped to the module being released unless the user asks for a coordinated release.

### Coordinated Plugin And Agent Release Order

When a new Plugin version calls an Agent hook that is introduced in the same coordinated release,
publish in this order to avoid a circular dependency during Gradle's root-project configuration:

1. Set `ftPlugin` and `pluginMinAgentSupport` to the new versions, but keep
   `ftPluginBuildscript` on the latest Plugin version already present in the Maven repository.
2. Update the Plugin changelog, commit, push the branch, then create and push only the `plugin_*` tag.
3. Wait until the new Plugin POM and artifact are available from the configured Maven release repository.
4. Set `ftPluginBuildscript` to the newly published Plugin version and verify that Gradle can resolve it.
5. Commit and push this dependency switch, then create and push the dependent `agent_*` and
   `replay_*` tags.

Do not point `ftPluginBuildscript` at an unpublished version, and do not push the Plugin tag together
with dependent Agent or Replay tags. `FT_USE_LOCAL_PLUGIN=true` is only for a second-stage local
verification after the current Plugin JAR has been built under `ft-plugin/build/libs`; it does not
replace the published-version bootstrap step.

## Coding Guidelines

- Preserve existing Java/Groovy style and package structure.
- Prefer small, localized changes; this SDK is consumed externally, so public APIs and behavior need extra care.
- Treat SDK initialization, data collection, caching, threading, privacy masking, and network interception as high-risk areas.
- Avoid increasing the SDK's embedded database schema version whenever practical. First exhaust schema-compatible alternatives such as file sidecars, stable identifiers, existing columns, or recomputation, and keep SQLite/file-store behavior equivalent.
- If a database version increase is still necessary, document why compatible alternatives are insufficient, cover upgrade/downgrade and interrupted-migration recovery for released versions, and obtain explicit user approval before implementing it.
- For Android code, avoid raising `minSdkVersion` or adding new runtime dependencies without a clear need.
- For public API changes, update tests and release notes, and check documentation references when applicable.
- Do not remove Datadog-derived license notes or attribution in session replay modules.

## Testing Notes

- Add or update unit tests under the affected module when changing logic.
- Use Android instrumentation tests only when device/framework behavior is involved.
- If tests cannot run because of missing Android SDK, local AARs, credentials, or network access, report the exact command attempted and the blocker.

## Cross-Repository Session Replay Debugging

- For WebView Session Replay playback failures, blank playback, missing content, malformed events, or mobile event compatibility issues, do not inspect the Android producer path in isolation.
- Use `/Users/Brandon/Documents/workplace/WebProject/cloudcare-forethought-webclient/docs/session-replay-reference.md` as the stable index and an evidence source for the CloudCare Web query, mobile-event transformation, and Replayer consumption path.
- Correlate the Android path in `FTWebViewHandler`, `SessionReplayBridge`, `ft-session-replay/webview/DataBatcher`, `WebViewWireframeMapper`, and `SlotIdWebviewBinder` with the Web paths indexed by that reference, especially `Player.vue`, `MobileSessionReplay/transformers.ts`, and `Replayer/index.ts`.
- Compare the emitted NDJSON event types and payloads together with `applicationId`, `sessionId`, `viewId`, `slotId`, container linkage, timestamps, full snapshots, incremental snapshots, and resource URLs before deciding whether the fault is in Web recording, the Android bridge/batching/storage path, the query API, mobile transformation, or the Web Replayer.
- Treat the reference document as a navigation and evidence anchor, not as proof that the current implementation still behaves exactly as documented. When behavior details matter, inspect the current Web source files referenced by the document and note the branch or commit used.
- If the referenced local Web project or document is unavailable, report that limitation explicitly instead of guessing about playback behavior.

## Repository Hygiene

- Do not commit generated build output, local Gradle caches, keystores, `local.properties`, credentials, downloaded AAR/JAR fixtures, or IDE metadata unless the user explicitly asks.
- Several local-only files may appear in this workspace for manual testing. Leave unrelated untracked or modified files untouched.
- Use `rg` for searches and `./gradlew` for builds/tests.
