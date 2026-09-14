# GuanceCloud SDK Android


![](https://img.shields.io/badge/dynamic/json?label=ft-sdk&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/agent/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-native&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/native/version.json&link=https://github.com/GuanceCloud/datakit-android
) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin-legacy&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin_legacy/version.json&link=https://github.com/GuanceCloud/datakit-android)
## Introduction

GuanceCloud Application Monitoring can collect metric data from various Android applications and analyze the performance of each Android app client in a visualized way.

## Example

 [GuanceCloud SDK Android Demo](https://github.com/GuanceDemo/guance-app-demo/tree/master/src/android/demo)   

## Documentation
 [Documentation Center](https://docs.guance.com/real-user-monitoring/android/app-access/)   

## Visitor ID request headers (unreleased)

Visitor ID provides an anonymous rate-limiting and RUM correlation key independently of
Trace and RUM sampling. Set a valid `FTVisitorIdConfig` to enable it; omitting the configuration
or setting it to `null` disables it. Initialize before building business OkHttp clients:

```java
FTSdk.install(FTSDKConfig.builder("https://your-intake.example/"));
FTSdk.initRUMWithConfig(new FTRUMConfig()
        .setRumAppId("your-rum-app-id")
        .setVisitorIdConfig(new FTVisitorIdConfig()
                .setHeader("x-rum-visitor-id")
                .addMatch("https://api.example.com/")
                .setTtlHours(24)));
```

The existing Plugin `OkHttpClient.Builder.build()` hook installs Visitor ID independently
of automatic Resource and Trace collection. There is no new Plugin hook or coordinated
Plugin release requirement. Without Plugin instrumentation, install manually after configuring
your other interceptors and before `build()`:

```java
OkHttpClient.Builder builder = new OkHttpClient.Builder();
// Add your other interceptors, including optional FTResourceInterceptor/FTTraceInterceptor.
OkHttpClient client = FTVisitorIdInterceptor.install(builder).build();

// Only after your application confirms sign-out or an account switch:
FTSdk.resetVisitorId();
```

`install(builder)` adds both an application interceptor and a network redirect guard, deduplicates
them, places the application interceptor first and the guard last. Using only
`addInterceptor(new FTVisitorIdInterceptor())` omits redirect protection; use the installer.
Clients built before initialization need manual installation or rebuilding. An installed interceptor
becomes inactive after SDK shutdown. The helper does not enable Resource or Trace collection.

Configuration is captured at initialization; changing the configuration object afterwards does not
change the running SDK. There is no separate `setEnabled` switch. `header` must be a lowercase HTTP token, with no
authentication, CSRF, signature, Trace or protocol-reserved header name. The header is required
when enabled; there is no brand-specific default. Invalid configuration disables only Visitor ID
and emits a diagnostic through SDK logging (enable `FTSDKConfig.setDebug(true)` to inspect it).
Provide at least one matching rule: `addMatch(String)` requires a complete URL prefix with the
same scheme, host and effective port; `addMatch(Pattern)` searches the full URL;
`addMatch(FTVisitorIdConfig.UrlMatcher)` accepts a predicate. Matching predicates should be pure
and may run more than once per request; exceptions are isolated as non-matches.

Matched requests replace all existing values of the configured header with the SDK value.
Unmatched requests retain caller headers. SDK intake endpoint prefixes are excluded under the
configured DataKit/Dataway base path: RUM (including replay uploads), logging, replay asset checks,
remote configuration and filter polling. Exclusion follows dynamic upload-address changes;
same-origin business APIs remain eligible. `FTRUMConfig.setResourceUrlHandler(...)` also excludes
Visitor headers, independently of Resource collection; a throwing exclusion callback skips injection.
The redirect guard checks each network destination and removes SDK-injected headers outside the
match range. Allowed redirect/retry hops retain the initial request ID even after TTL/reset.

The default TTL is 24 hours, measured from generation without sliding renewal. Expiration is
checked on the next read. SharedPreferences stores `p` plus 32 lowercase hexadecimal characters;
failed persistence falls back to `s` plus 32 lowercase hexadecimal characters for the current
manager lifetime. Reads, rotation and reset are synchronized. A valid persistent record survives
process restarts; damaged records self-heal. A fallback remains stable until TTL/reset even if
storage recovers, then retries persistence. When storage cannot be written, a reset cannot be
guaranteed to survive process death; the fallback lifetime ends with that process/SDK lifecycle.
Do not parse IDs in business logic. Each app process has its own storage record and ID; this is
not a cross-process SharedPreferences coordination mechanism. The SDK defaults to its main process.

Native RUM events carry `visitor_id` as a tag. Requests capture an ID before execution; their
Resource and associated network Error use that snapshot even if reset occurs in flight.
Other native RUM events capture the current ID before entering the asynchronous write queue.
When a trusted snapshot exists, ordinary Global/View/Event contexts cannot override it. Following
Web 3.3.15's `beforeSend` semantics, final modifiers may explicitly change the reported value:
the SDK applies `DataModifier` to the trusted ID, then `LineDataModifier` to the assembled event,
then data filters. Return an empty string for `visitor_id` to omit the tag from line protocol.
The existing Modifier contract is unchanged: `null` means no modification; returning a map without
`visitor_id` also leaves it unchanged. These changes affect RUM only, never the manager or request
header, so deliberate redaction breaks Header-to-RUM equality for that event.
Browser RUM inside WebView retains the Browser SDK's Visitor ID; native global contexts do not
supply or overwrite it.
Historical recovered events without an original Visitor ID snapshot are not retroactively labeled.

```java
// Configure on FTSDKConfig before FTSdk.install(...).
baseConfig.setLineDataModifier((measurement, data) -> {
    HashMap<String, Object> changes = new HashMap<>();
    if (data.containsKey("visitor_id")) {
        changes.put("visitor_id", ""); // Explicitly omit it from RUM, as with Web beforeSend deletion.
    }
    return changes;
});
```

`resetVisitorId()` does not reset the RUM Session, bound user, Trace IDs or sampling. It is not
implicitly called by `unbindRumUserData()`, a 401 response, a network failure or an unsampled
session. An unsampled session still sends Visitor headers but does not force RUM data collection.

Coverage is OkHttp HTTP(S) requests. WebSocket handshakes, HttpURLConnection, Cronet, WebView
internal traffic and other network stacks are outside the initial coverage. No runtime dependency,
minimum Android version or database schema change is required.

Visitor ID is client-controlled and is only a rate-limit bucket/correlation key, never an
authentication, tenant-isolation or audit identity. For Cloudflare Advanced Rate Limiting, use
two separate rules: requests with the configured header count by that header; missing-header
traffic counts by `ip.src`. Keep penalties short and adapt thresholds to the service.
The [Web Visitor ID guide](https://docs.guance.com/real-user-monitoring/web/visitor-id/)
includes a two-rule template, including `counting_expression`; the same template applies to
Android traffic using the same header. Follow the
[Cloudflare API documentation](https://developers.cloudflare.com/waf/rate-limiting-rules/create-api/)
to adapt it to an existing `http_ratelimit` ruleset. SDK initialization does not deploy gateway rules.

## Open Source Code Usage
* [ft-session-replay](ft-session-replay) is a secondary development based on datadog [dd-sdk-android-session-replay](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay)
* [ft-session-replay-material](ft-session-replay-material) is a secondary development based on datadog [dd-sdk-android-session-replay-material](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay-material)
* [ft-session-replay-compose](ft-session-replay-compose) is a secondary development based on datadog [dd-sdk-android-session-replay-compose](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay-compose)

## Third-party Licenses
- Datadog [dd-sdk-android](https://github.com/DataDog/dd-sdk-android/blob/develop/LICENSE)


## HTTP / HTTPS URLConnection Resources

The candidate Agent `1.7.6-alpha03` and Plugin `1.3.9-alpha01` support standard
`HttpURLConnection` and `HttpsURLConnection`. Collection is **off by default**:

```java
FTSdk.initRUMWithConfig(new FTRUMConfig()
        .setRumAppId("your-rum-app-id")
        .setEnableTraceUserResource(true)
        .setEnableHttpURLConnectionResource(true));
```

With the Plugin applied, calls to `URL.openConnection()` and `URL.openConnection(Proxy)`
in instrumented Java/Kotlin application and dependency code preserve the original call,
then wrap its result. The independent hook does not require OkHttp or Session Replay.
HTTP I/O, TLS trust/hostname checks, certificates, timeouts, caching, redirects and upload
configuration remain owned by the original connection. Non-HTTP results are unchanged.

```groovy
FTExt {
    instrumentHttpURLConnection = false // build-level rollback; default true
    // Or exclude selected call sites with the existing exact/wildcard matching rules:
    ignorePackages = ['com.example.network.CustomAdapter', 'com.example.legacy.**']
}
```

Runtime URL filtering uses `setResourceUrlHandler(url -> shouldExclude(url))`;
`true` means **exclude**. SDK internal call sites and configured intake endpoints are excluded.
Connections created before initialization or while disabled are not retroactively wrapped.
Disabling a switch prevents new tracking; requests already started finish under their original
policy. Shutdown discards outstanding tracking without closing business connections.

A Resource starts on the first request-triggering operation and finishes once at response EOF,
input close, disconnect, confirmed HEAD/204/304 response, or I/O failure. Closing an output stream
only ends the upload. HTTP 4xx/5xx error streams remain readable and are measured until completion.
No full body is recorded. To propagate Trace headers and link the same IDs to RUM Resources:

```java
FTSdk.initTraceWithConfig(new FTTraceConfig()
        .setEnableAutoTrace(true)
        .setEnableLinkRUMData(true)
        .setTraceType(TraceType.DDTRACE)
        .setSamplingRate(1f));
```

With URLConnection Resource collection enabled, Trace headers are injected before the first
request-triggering operation. The default SDK generator supports Datadog, W3C Trace Context,
Zipkin multi/single header, Jaeger and SkyWalking. The configured format replaces existing
same-name headers, matching the default OkHttp interceptor. The OkHttp-specific custom header
handler does not apply to URLConnection. Disabling automatic Trace preserves application headers;
disabling RUM linking still propagates headers but omits `trace_id` / `span_id` from the Resource.
Sampling controls the propagated sampling decision; it does not suppress Trace IDs.
Header injection failures never replace the business call or produce a fabricated RUM link.
One context is retained for the connection lifecycle, including redirects and transport retries;
URL filters and SDK-intake exclusions apply before propagation. The connection layer does not
create or upload an independent APM span; the backend tracer must consume the selected format.

`resource_collection_source=http_url_connection`, `resource_completion` and
`resource_body_complete` describe the observation. `resource_request_bytes_observed` and
`resource_response_bytes_observed` count successful application-side I/O (including skipped
response bytes), not wire traffic. Reset/reread of a marked stream is counted once. Complete size
uses the existing body-plus-header estimate; incomplete or uncertain response sizes are omitted.
Headers retained for collection are bounded to 16 KiB per direction. Total duration includes
application stream-consumption delays. DNS/TCP/TLS/strict TTFB, download phases and connection reuse
are omitted because this layer cannot reliably observe them.

The tracking table, weak connection-identity registry and queued pending-start updates each
hold at most 1,000 entries.
A shared task discards tracking after five minutes without observed progress (checked once per
minute); it balances pending counters without inventing a timeout/error or touching the socket.
Exclude long-lived streams/SSE when this observation window is unsuitable.

Existing application OkHttp tracking is unchanged. The concrete
`okhttp3.internal.huc.OkHttpURLConnection` / `OkHttpsURLConnection` types from
[`okhttp-urlconnection` 3.12.13](https://github.com/square/okhttp/tree/parent-3.12.13/okhttp-urlconnection)
are returned unchanged to avoid outer-layer collection. Inner collection depends on that client's
SDK integration. System `com.android.okhttp` implementations are eligible. Unknown/relocated
adapters and other APM wrappers have no cross-layer deduplication guarantee; use exclusions.
Concrete implementation-class casts, uninstrumented reflection/dynamic code, native/libcurl,
Cronet, and WebView internal networking are outside this feature. Direct `URL.openStream()` and
`URL.getContent()` call sites are not instrumented; methods on an already wrapped connection
remain delegated. No Cocos-specific adaptation is included.

For coordinated release, publish the Plugin first while `ftPluginBuildscript` remains on the
already-published `1.3.8`. After the new Plugin exists in Maven, switch that buildscript dependency
and publish the Agent. The current repository configuration does not point to an unpublished
Plugin; `FT_USE_LOCAL_PLUGIN=true` is for local verification after building the Plugin JAR.

To try this in the Sample, build the local Plugin and then the Sample:

```bash
./gradlew :ft-plugin:build
FT_USE_LOCAL_PLUGIN=true ./gradlew :app:assembleProdTestDebug
```

Open **Network Test (HTTPS)** near the top of the Sample home screen. The
**HttpsURLConnection Automatic Collection** section provides GET `/get`, JSON POST `/post`, and
GET `/status/404` buttons, using `https://httpbin.org` by default. The editable HTTPS
root address must support those routes. Requests run on a background thread with
15-second connect/read timeouts and standard TLS verification. The page displays status,
elapsed time, bytes read, and up to 512 bytes of response preview; the response body is
not recorded by the SDK. It also reports whether the automatic collection wrapper is active.
The Sample enables both Resource switches and automatic Trace/RUM linking in `DemoApplication`.
GET/POST responses from the default echo service also display the server-observed Datadog
`trace_id` and `span_id`; the page buffers at most 16 KiB to parse the echo and displays a
512-byte preview. Configure its usual RUM
application ID and intake endpoint to view uploaded Resources. Filter by
`resource_collection_source=http_url_connection`. A successful build with the default
published Plugin `1.3.8` does not instrument these calls; use the local Plugin command above.

The HTTP/HTTPS proxy forwarding methods are adapted from Firebase Performance at commit
`9fa84de12aaa9ce0afb6634030e4b6d26d66b3d0`, Copyright 2020 Google LLC, Apache License 2.0.
The original license headers are retained; request lifecycle/reporting and stream accounting are
implemented for this SDK.


## Experimental Android Heatmap

Heatmap collection is **disabled by default**. Configure the shared RUM/native Replay switch
before initializing RUM; changing the config object afterwards does not change the active setting.
Existing Heatmap users must explicitly enable it when upgrading:

```java
FTSdk.initRUMWithConfig(new FTRUMConfig()
        .setRumAppId("your-rum-app-id")
        .setEnableTraceUserAction(true)
        .setEnableHeatMap(true));
```

Use `setEnableHeatMap(false)` (or omit it) as a fallback to stop Heatmap-specific touch target
lookup, `action_position` / `action_target` generation, automatic RUM `display` enrichment,
Replay `permanentId` resource-name/path/hash calculation, and transparent Heatmap binding nodes.
These paths are skipped before calculation, avoiding their resource lookup and `Invalid ID`
overhead. Ordinary RUM Actions, Views and Resources, native replay recording, replay viewport
dimensions and replay touch visualization keep their existing settings. Caller-supplied fields
and already stored records are not removed. This does not suppress unrelated Android resource
errors or guarantee elimination of all application jank.

Use the Agent and Replay changes together for the complete switch. An older Replay still calls
the Agent resolver, which returns immediately when disabled, but does not skip its transparent
binding nodes. A new Replay with an older Agent defaults Heatmap bindings to disabled.
Enabling Heatmap does not enable automatic Action collection or change Replay sampling.
Only initialization configuration is supported; there is no runtime or remote toggle.
The SDK configuration log includes `enableHeatMap`.
