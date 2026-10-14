# Unreleased
1. Made runtime Replay sampler updates visible across threads so Native RUM sampling metadata can read the effective rates while remote configuration is being updated. Replay feature context sampling fields now use 0–100 percentages; internal samplers and legacy APIs retain 0–1 probabilities.

---
# replay 0.1.9-alpha06
1. Added default-off self-monitoring hooks for Replay serialized event writes, write failures and size rejection, observed file backlog, selected batches, retained retries, and file cleanup.
2. Added the `ft-diagnostics-api:0.1.0-alpha01` runtime dependency. Use the optional `ft-diagnostics:0.1.0-alpha01` collector to inspect local diagnostics; pair with `ft-sdk >= 1.7.6-alpha07` to also observe SDK HTTP upload attempts and body bytes. Direct AAR integrations must also include the API AAR.
3. Preserved Replay recording and upload behavior. Replay backlog counts files, and write bytes describe serialized event data before batch framing/encryption rather than physical disk IO.

---
# replay 0.1.9-alpha05
1. Added `FTSessionReplayConfig.setSessionReplaySamplingRate(float)` (0–100, default 100) and `setSessionReplayOnErrorSamplingRate(float)` (0–100, default 0), matching getters and feature runtime update APIs. Deprecated the old 0–1 APIs while retaining behavior for legacy callers.
2. Preserved fractional percentages, explicit zero and new-over-old initialization precedence. When using the new config APIs with remote configuration, use `ft-sdk >= 1.7.6-alpha05` so remote values continue to override explicit new local settings.

---
# replay 0.1.9-alpha04
1. Native Replay now follows the Agent Heatmap setting and skips permanent ID resolution and transparent Heatmap binding nodes when disabled, preserving ordinary wireframes, viewport dimensions and touch visualization. Requires `ft-sdk >= 1.7.6-alpha04` to opt in through `FTRUMConfig.setEnableHeatMap(true)`; older Agents default to disabled.

---
# replay 0.1.9-alpha03
1. Fixed Replay segment compression to use a single SYNC_FLUSH-aligned zlib stream, restoring playback when multiple native or external-recorder segments are joined.

---
# replay 0.1.9-alpha02
1. Fixed native and external recorder handoffs so queued records remain linked to their original active RUM View, closed Views stop accepting new records, and native recording resumes across a clean View boundary.
2. Added budgeted WebP encoding for external recorder images, progressively reducing quality and dimensions before rejecting frames that exceed the configured byte limit.

---
# replay 0.1.9-alpha01
1. Added reusable external recorder ownership control so multiple framework or engine recorders can pause native recording while active, resume it only after the final owner releases control, and force a full snapshot when native recording resumes.
2. Made external-only recorder mode publicly configurable for framework SDK integrations.

---
# replay 0.1.8
1. Added the RUM `globalContext` values selected by `FTSessionReplayConfig.enableLinkRUMKeys(...)` to Session Replay resource existence checks and uploads, and scoped resource caching, deduplication, existence checks, and uploads by `wgt_id` to preserve routing between WGT applications.
2. Changed `SessionReplayResourceUploadCallback.onCheckFilesExist(...)` and `onUploadFiles(...)` to include a `Map<String, Object> globalContext` parameter. Custom implementations must update both callback methods and forward the context as request tags.
3. Fixed a memory leak where Session Replay retained WebViews and Activities after replay stopped.

---
# replay 0.1.8-beta01
1. Same as version 0.1.8-alpha01.

---
# replay 0.1.8-alpha01
1. Added the RUM `globalContext` values selected by `FTSessionReplayConfig.enableLinkRUMKeys(...)` to Session Replay resource existence checks and uploads, preserving the linked RUM context in resource requests.
2. Added `SlotIdWebviewBinder.unbind(String)` and `SlotIdWebviewBinder.unbind(long)`, and fixed a WebView memory leak caused by stale slot bindings retained after Session Replay stopped.

---
# replay 0.1.7
1. Added Session Replay element association for the experimental Android Heatmap; requires `ft-sdk >= 1.7.4`.
2. Fixed pointer IDs and coordinates for multi-touch and cancelled gestures.
3. Fixed touch privacy overrides set through `PrivacyOverrideExtensions.setSessionReplayTouchPrivacy(View, TouchPrivacy)` not taking effect under locale-sensitive case conversion, such as Turkish.

---
# replay 0.1.7-beta01
1. Same as 0.1.7-alpha01

---
# replay 0.1.7-alpha01
1. Added stable `permanentId` metadata to Session Replay Wireframes and exposed `Wireframe.getPermanentId()` and `Wireframe.setPermanentId(String)`, enabling replay elements to be associated with RUM Heatmap Action targets when used with `ft-sdk >= 1.7.4-alpha02`.
2. Improved touch interaction recording for multi-pointer down/up and cancelled gestures, ensuring pointer IDs and coordinates are recorded for the correct touch.
3. Fixed locale-sensitive `TouchPrivacy` parsing under languages such as Turkish by using locale-stable case conversion.

---
# replay 0.1.6
1. Added Flutter Session Replay external recorder mode so Flutter-side recorders can reuse Session Replay sampling, context updates, and data writing while disabling the native recorder lifecycle.
2. Fixed a crash when Session Replay handles a `DialogFragment` whose `getDialog()` or owner window is null during fragment resume or pause callbacks.

---
# replay 0.1.6-beta01
1. same as 0.1.6-alpha02

---
# replay 0.1.6-alpha02
1. Added Flutter Session Replay external recorder mode so Flutter-side recorders can reuse Session Replay sampling, context updates, and data writing while disabling the native recorder lifecycle.

---
# replay 0.1.6-alpha01
1. Fixed a crash when Session Replay handles a `DialogFragment` whose `getDialog()` or owner window is null during fragment resume or pause callbacks.

---
# replay-compose 0.1.5
1. Fixed Session Replay upload retry handling so retryable data and resource upload failures preserve retry state and use exponential backoff, while invalid upload URL configuration is skipped until a valid endpoint is available.
2. Replaced remaining JetBrains nullability annotations with AndroidX annotations in Session Replay code to avoid redundant annotation references.

# replay 0.1.5-beta01
1. Same as 0.1.5-alpha01

---
# replay 0.1.5-alpha02
1. Fixed Session Replay upload retry handling so retryable data and resource upload failures preserve retry state and use exponential backoff, while invalid upload URL configuration is skipped until a valid endpoint is available.

---
# replay 0.1.5-alpha01
1. Added Flutter Session Replay upload compatibility by supporting Flutter RUM context keys when mapping replay batches to segments.
2. Fixed Session Replay file reader/writer factory compatibility for plain and encrypted storage implementations.
3. Replaced remaining JetBrains nullability annotations with AndroidX annotations in Session Replay code to avoid redundant annotation references.

---
# replay 0.1.4
1. Removed redundant Kotlin dependencies to simplify the Session Replay dependency graph.
2. Improved public API documentation for Session Replay configuration, privacy controls, custom mapper extensions, resource upload callbacks, and mapper helper types to make IDE integration and custom extension development clearer.

---
# replay 0.1.4-beta01
1. Removed redundant Kotlin dependencies

---
# replay 0.1.4-alpha03
1. Improved public API documentation for Session Replay configuration, privacy controls, custom mapper extensions, resource upload callbacks, and mapper helper types to make IDE integration and custom extension development clearer.
2. Optimized the internal UTF-8 encoding implementation used when writing replay records, keeping the output format unchanged while avoiding a Kotlin charset dependency in Java code.

---
# replay 0.1.4-alpha02
1. fix: correct dependency version constraint with ft-sdk and replay

---
# replay 0.1.4-alpha01
1. No Changed

---
# replay 0.1.3
1. Added WebView Session Replay support, including DCloud WebView support and full-snapshot keyframe support for WebView containers.
2. Added RUM context association for Session Replay, allowing WebView containers to be linked with the context of loaded HTML content.
3. Added error sampling support and remote configuration support for Session Replay.
4. Added image resource upload support and improved metadata handling, including userAgent, appVersion, and forceFullSnapshot.
5. Optimized frame generation and consumption, frame completion during data sharding, delayed writes for WebView replay data, invisible WebView handling, and disk write limits to improve replay stability and performance.
6. Fixed issues involving WebView replay data loss, blank replay caused by writes occurring too early, synchronization failures on lower OkHttp versions, recording failures caused by bitmap recycling, null value handling for rumLinkKey, and replay timeout issues when many images are present.

---
# replay 0.1.3-beta02
1. Same as 0.1.3-beta01

---
# replay 0.1.3-beta01
1. Same as 0.1.3-alpha15

---
# replay 0.1.3-alpha15
1. Fix an issue where replay recording times out when there are many images due to 
   local icon cache misses.

---
# replay 0.1.3-alpha14
1. ft-sdk:>= 1.7.0-alpha40
2. Added image resource upload logic for Session Replay.

---
# replay 0.1.3-alpha13
1. ft-sdk:>= 1.7.0-alpha39
2. Added Session Replay support to remote configuration.

---
# replay 0.1.3-alpha12
1. ft-sdk:>= 1.7.0-alpha37
2. Fixed the issue where enabling ErrorSampleRate caused key playback data to be lost in WebView
3. Optimized the logic for handling replay data when the WebView is not visible

---
# replay 0.1.3-alpha11
1. ft-sdk:>= 1.7.0-alpha36
2. Optimized the linkView frame completion process during Replay data sharding.

---
# replay 0.1.3-alpha10
1. Added meta data for forceFullSnapshot.
2. Optimized frame generation and consumption logic.
3. Fixed the issue where recording failed due to bitmap recycling.
4. ft-sdk:>= 1.7.0-alpha35

---
# replay 0.1.3-alpha09
1. ft-sdk:>= 1.7.0-alpha34
2. Added null value check for rumLinkKey.

---
# replay 0.1.3-alpha08
1. ft-sdk:>= 1.7.0-alpha34
2. Added full snapshot keyframe Session Replay support for the WebView container

---
# replay 0.1.3-alpha07
1. ft-sdk:>= 1.7.0-alpha33
2. Increased the disk write limit for Session Replay
3. Added RUM Session Replay context association feature,
   and associated the WebView container with the context of the loaded HTML.

---
# replay 0.1.3-alpha06
1. Delayed write for WebView Session Replay data.
2. ft-sdk:>= 1.7.0-alpha29
---
# replay 0.1.3-alpha05
1. Fixed the issue where obtaining write too early caused WebView Session Replay to be blank, and added log output.
2. ft-sdk:>= 1.7.0-alpha28
---
# replay 0.1.3-alpha04
1. resolve the css issues in uni App
2. ft-sdk:>= 1.7.0-alpha27
---
# replay 0.1.3-alpha03
1. DCloud WebView Support
2. ft-sdk:>= 1.7.0-alpha26

---
# replay 0.1.3-alpha02
1. Added `CustomExtensionSupport`for quickly customizing and adding ExtensionSupport settings.
2. WebView session replay supports Tencent X5

---
# replay 0.1.3-alpha01
1. Webview Session Replay support ft-sdk:>= 1.7.0-alpha23

---
# replay 0.1.2-alpha02
1. Adapt to react native replay feature

---
# replay 0.1.2-alpha01
1. Added `FTSessionReplayConfig.setSessionReplayOnErrorSampleRate` support for error sampling, in case it is not sampled by `setSamplingRate`, it can sample data from session replay 1 minute ago when an error occurs

---
# replay 0.1.1-alpha01
1. Added TextAndInputPrivacy, TouchPrivacy settings
2. Supported for covering TextAndInputPrivacy and TouchPrivacy settings for separate pages

---
# replay 0.1.0-alpha09
1. UserAgent appended Session Replay SDK information
2. Supported for depending on Dns and Proxy, unifying data entry dependency ft-sdk internal settings to create thread pool and send data

---
# replay 0.1.0-alpha08
1. Modified X-Pkg-Id placement

---
# replay 0.1.0-alpha07
1. X-Pkg-Id Session Replay data synchronization tracking adaptation

---
# replay 0.1.0-alpha05
1. Fixed React Native View crash problem

---
# replay 0.1.0-alpha04
1. Supported React Native

---
# replay 0.1.0-alpha03
1. Line protocol packet version display
2. dialog background acquisition optimization

---
# replay 0.1.0-alpha02
1. Supported session replay recording function
2. Supported sampling rate, privacy configuration, and Material component additional configuration settings
3. Supported datakit, dataway transmission

# session-replay 0.1.1-alpha01
1. Support for CardView and Chip components

---
# session-replay 0.1.0-alpha04
1. Debug low version support

---
# session-replay 0.1.0-alpha03
1. Line protocol packet version display

---
# session-replay 0.1.0-alpha01
1. Support for session replay Material components
