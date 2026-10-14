# Optional SDK diagnostics

`ft-diagnostics` is a local-only component, separate from FTSDKConfig and business
SDK initialization. `ft-diagnostics-api` contains the shared internal hooks needed
by core and optional Replay; applications should not call the internal bridge.
No third-party runtime dependency is added by the collector.

## Integration

The first diagnostic release is `0.1.0-alpha01`, paired with
`ft-sdk:1.7.6-alpha07` and, when Replay monitoring is needed,
`ft-session-replay:0.1.9-alpha06`.

For this source checkout:

```groovy
implementation project(':ft-sdk')
implementation project(':ft-diagnostics')
// Add ft-session-replay only when testing Replay.
```

The three consumers declare the hook artifact in generated Maven POMs. When
releasing these changes, publish `ft-diagnostics-api` before its consumers. Direct
AAR integrations must also include that artifact. Nothing is published by the tests.

Both diagnostic artifacts and all consumers' API dependency versions use the root
`sdkVersions.ftDiagnostics` entry, independently of `ftSdk`. The agreed release tag
is `diag_${version}` for both diagnostic artifacts. Publish the API artifact first,
then the collector, before releasing the dependent SDK and Replay versions.

Call from a worker thread and keep the manager for the diagnostic lifecycle:

```java
File root = new File(context.getNoBackupFilesDir(), "ft-diagnostics/main");
FTDiagnostics diagnostics = new FTDiagnostics(root, new FTDiagnosticsConfig());
String session = diagnostics.start();
// Initialize/use SDK, optionally drain business uploads explicitly.
JSONObject finalSummary = diagnostics.stop();
JSONArray page = diagnostics.records(session, 0, 100);
diagnostics.exportSession(session, new File(exportParent, session));
diagnostics.close();
```

Use distinct stable directory names for each process role, not a shared `main`
directory in every process. `status`, `sessions`, `summary`, `deleteSession`, and
`cleanExpired` complete the management API. Custom limits are constructor arguments
on `FTDiagnosticsConfig`. Limits and collection are independent from SDK configuration.

## Android hook coverage

- NetProxy: SDK request invocations, including Replay uploads and control requests.
- OkHttp: a network interceptor counts encoded request/response bodies after
  request compression and before response decompression. Existing SDK automatic
  RUM instrumentation is not used to measure SDK traffic.
- HttpURLConnection: output/input streams count application-visible body bytes,
  including multipart output; response encoding may already have been handled by
  the platform, so its byte source explicitly differs from OkHttp.
- SQLite: bulk insert outcomes, deletion by IDs, expiration and capacity cleanup,
  existing count queries. Capacity cleanup's affected count is unavailable from
  its current ContentProvider call. Existing-row updates and historical inserts
  are also observed; updates/reinserts are writes, not unique new events.
- File store: serialized record writes (including updates/reinserts), file deletion,
  existing count queries. A file deletion without type attribution uses `other`.
- Replay: batch file writes, selection, backlog observations, retained retries and
  cleanup. Replay write bytes count raw serialized event bytes before the batch
  writer's framing/encryption; metadata writes are excluded.
- Cache policy observes incoming Log/RUM rejection decisions.
- Shadow storage performs two physical storage paths and can therefore produce
  duplicate write observations. These are not unique event-generation counts.

## Verification

```sh
./gradlew :ft-diagnostics:testDebugUnitTest
./gradlew :ft-sdk:testDebugUnitTest --tests 'com.ft.sdk.garble.http.DiagnosticNetworkInterceptorTest' --tests 'com.ft.sdk.NetProxyTest' --tests 'com.ft.sdk.SyncTaskManagerTest'
./gradlew :ft-diagnostics:assembleDebug :ft-sdk:assembleDebug :ft-session-replay:assembleDebug
```

The collector tests cover isolation, recovery, quotas, IO failures and disabled
hooks. The HTTP test checks encoded gzip response accounting while preserving
application response decoding. Device-level overhead and multi-process acceptance
remain separate validation; successful builds do not establish an overhead budget.

## Data contract (schema_version = 1)

Each session contains `summary.json` (an atomic checkpoint, updated approximately
once per second) and `events.jsonl` (bounded batch/upload diagnostic records).
Records use `session_id`, `event_type`, `channel`, and Unix `time_ms`. Upload start
and end records share a generated `attempt_id`; durations use a monotonic clock.
No endpoint, query string, headers, business payload, token, user identifier or
business event ID is exported. Channels are bounded: log, rum, replay, other.
The latter includes SDK control requests and storage operations without reliable
channel attribution. A diagnostic attempt is not a business-event delivery receipt.

Event types:

| Type | Meaning |
| --- | --- |
| storage_write | Completed storage writes; aggregate only, no per-event details |
| storage_remove | Observed storage cleanup, with a reason and actual count when available |
| discard | Observed filtering, write failure or non-retryable outcome; inspect reason rather than treating all observations as lost events |
| backlog | Successful existing storage count/read observation, not a new polling query |
| batch | Selected upload batch; repeated selections can include retries |
| retry | Scheduled retry or retained failed batch, not proof a retry reached the server |
| upload_start / upload_end | One SDK transport invocation and its observed outcome |

`summary.counters` is keyed by `channel.event_type.field`. Counts and byte/duration
values are summed; `.samples` counts observations with a valid value and `.max`
retains the maximum. Backlog is the latest value and includes `.observed_ms` for
freshness. `observations` and bounded `reason.*` counters count hook invocations,
not necessarily business events. Missing fields are unavailable, never implied zero.
Log/RUM backlog is in events; Replay backlog is in files and may precede age
eligibility checks. These are observations of shared business storage, and must
not be added across processes. No extra periodic scan of business storage is made.

Byte scopes are deliberately separate:

- `storage_write.bytes`: serialized payload bytes for Log/RUM; Replay uses its
  documented writer boundary. This is not SQLite/WAL/device IO volume.
- `request_body_bytes` / `response_body_bytes`: observed HTTP body bytes; consult
  `byte_source` and `byte_availability` for each attempt. Headers, TLS/TCP overhead,
  TCP retransmissions and radio traffic are excluded. Partial failed writes may
  be unobservable. Do not infer server receipt from stream writes.
- `batch.age_ms`: uses `age_source`; Log/RUM uses the oldest event timestamp,
  which is not enqueue time. Replay file age has a different source. Never compare
  these as if they were a common exact end-to-end delivery latency.

HTTP observations are attributed at completion; they are useful for totals and
whole-window averages, not true instantaneous bandwidth peaks. Automatic transport
redirects/retries can be folded into one attempt; SDK-scheduled retries are separate.
A snapshot cannot reconstruct a complete per-event lifecycle or prove zero data loss.

## Lifecycle and bounded storage

Use a dedicated private directory for each process role. A filesystem lock prevents
concurrent managers from owning the same directory. No automatic collection occurs
on app restart. Sessions left active (or without a readable checkpoint) are recovered
as interrupted. The last checkpoint may omit work since its write; interrupted
sessions are always marked incomplete. A manager never resumes business uploads.

Defaults are 20 MiB per session, 100 MiB total per process directory, and 7 days.
Limits include reserved room for summary checkpoints. Old completed sessions are
removed before a new session reserves its maximum size. Age cleanup runs at manager
creation, start, or explicit cleanup; it is not an always-running background job.
After the detail cap, aggregates continue and omitted records are counted. The
writer queue is bounded to 1024 detail records. IO errors appear in status and do
not throw into SDK operations. No guarantees are made for sudden process death
before a checkpoint or filesystem power-loss durability.

`stop` disconnects the observer, drains only diagnostic work, and writes the final
summary. It never flushes, waits for, cancels or modifies SDK business uploads.
Open attempts remain `in_flight_uploads`; the summary is incomplete. Late completions
cannot enter a subsequent session. To include business drain time, flush/drain the
SDK externally first and then stop diagnostics. Start before SDK initialization to
observe its initial storage inventory. Keep the manager alive while collecting.

Management operations perform IO and should run off the UI thread. Export requires
a stopped session and a new destination outside diagnostic storage. Exported copies
are caller-owned and are not counted or removed by diagnostics retention. Detail
pagination uses a record offset and at most 1000 records per call. Historical files
are inspectable without enabling collection.

## Extension boundary

The core bridge is an internal contract with a default empty implementation. It
has no writer thread, local diagnostics storage or exporter while unregistered.
The optional component owns aggregation and local files. It has no network exporter,
does not reuse Log/RUM telemetry storage, and never logs its own failures through
the monitored SDK. A future uploader can consume completed exports independently.
