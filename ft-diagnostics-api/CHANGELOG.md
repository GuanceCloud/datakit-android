# diagnostics-api 0.1.0
1. Initial stable release of the shared internal observation bridge for the core SDK, Replay and the optional diagnostics collector. Application integrations should use `FTDiagnostics` rather than the internal bridge.
2. Added default-disabled hooks for storage writes/removals, discard observations, backlog, batch selection, retries and HTTP upload attempts, with bounded channel attribution and stream byte counters.
3. Added paired upload attempt identifiers, monotonic durations, duplicate-completion protection and observer capture to isolate late completions from subsequent sessions. Observer callback failures are isolated from SDK operations.
4. No collector writer thread, diagnostic storage or network exporter is created when no observer is registered. Both diagnostic artifacts use version 0.1.0 and the shared `diag_0.1.0` release tag.

---
# diagnostics-api 0.1.0-beta01
1. Same as version 0.1.0-alpha01.

---
# diagnostics-api 0.1.0-alpha01
1. Initial release of the shared internal observation bridge used by the core SDK, Replay, and the optional diagnostics collector. Application integrations should use `FTDiagnostics` rather than the internal bridge.
2. Added default-disabled observation hooks for storage writes/removals, discard observations, backlog, batch selection, retries, and HTTP upload attempts, with bounded channel attribution and stream byte counters.
3. Added paired upload attempt identifiers, monotonic durations, duplicate-completion protection, and observer capture so late completions do not enter a subsequent diagnostic session. Observer callback failures are isolated from SDK operations.
4. The bridge has no collector writer thread, diagnostic storage, or network exporter when no observer is registered. Both diagnostic artifacts share the root `sdkVersions.ftDiagnostics` version and the agreed `diag_${version}` release tag.
