# GuanceCloud SDK Android


![](https://img.shields.io/badge/dynamic/json?label=ft-sdk&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/agent/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-native&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/native/version.json&link=https://github.com/GuanceCloud/datakit-android
) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin-legacy&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin_legacy/version.json&link=https://github.com/GuanceCloud/datakit-android)
## Introduction

GuanceCloud Application Monitoring can collect metric data from various Android applications and analyze the performance of each Android app client in a visualized way.

## Example

 [GuanceCloud SDK Android Demo](https://github.com/GuanceDemo/guance-app-demo/tree/master/src/android/demo)   

## Documentation
 [Documentation Center](https://docs.guance.com/real-user-monitoring/android/app-access/)   

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
