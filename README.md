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
No full body is recorded, and this feature does not inject Trace headers.

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
The Sample enables both Resource switches in `DemoApplication`; configure its usual RUM
application ID and intake endpoint to view uploaded Resources. Filter by
`resource_collection_source=http_url_connection`. A successful build with the default
published Plugin `1.3.8` does not instrument these calls; use the local Plugin command above.

The HTTP/HTTPS proxy forwarding methods are adapted from Firebase Performance at commit
`9fa84de12aaa9ce0afb6634030e4b6d26d66b3d0`, Copyright 2020 Google LLC, Apache License 2.0.
The original license headers are retained; request lifecycle/reporting and stream accounting are
implemented for this SDK.
