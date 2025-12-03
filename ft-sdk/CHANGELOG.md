# agent 1.6.15
1. Optimized the FPS monitoring mechanism by stopping detection when the app is in the background.
2. Optimized the output of long logs more than 4K
3. Optimized the generation of resource, long task, and error with View data.
4. Improved the view generation rate and merged data with the same view_id.
5. remove ActivityLifecycleCallbacks limit in child process
6. Fixed the occasional issue where View data updates were lost.
7. Fixed the issue where asynchronous network requests caused abnormal session refresh.
8. Fixed the issue where reinitialization after SDK shutdown could not trigger idle shutdown.
---
# agent 1.6.15-beta01
1. Same as agent 1.6.15-alpha10
2. Fixed pending_resource_count in Action and View metrics

---
# agent 1.6.15-alpha10
1. Optimized the FPS monitoring mechanism by stopping detection when the app is in the background.

---
# agent 1.6.15-alpha09
1. Fixed the issue where reinitialization after SDK shutdown could not trigger idle shutdown.

---
# agent 1.6.15-alpha08
1. Fixed the issue above ft-sdk:1.6.15-alpha04 where data retransmission caused data field loss.

---
# agent 1.6.15-alpha07
1. Ensure FPS is started from the main thread
2. Optimized the output of long logs more than 4K
3. Added resource_id to network error types for data correlation.

---
# agent 1.6.15-alpha06
1. remove ActivityLifecycleCallbacks limit in main process

---
# agent 1.6.15-alpha05
1. Fix view and action error of status

---
# agent 1.6.15-alpha04
1. Fixed the issue where asynchronous network requests caused abnormal session refresh. 
2. Optimized the generation of resource, long task, and error with View data.
3. Improved the view generation rate and merged data with the same view_id.

---
# agent 1.6.15-alpha03
1. WebView data collection supports Tencent X5

---
# agent 1.6.15-alpha02
1. Added background field to error_situation

---
# agent 1.6.15-alpha01
1. Fixed the incorrect way of retrieving the cursor count.

---
# agent 1.6.14
1. Using ContentProvider to optimize compatibility issues in high-load multi-process data collection scenarios.
2. Removed the `android.permission.READ_PHONE_STATE` declaration from the SDK AndroidManifest;
   integrators need to add it themselves based on actual requirements.
3. The child process only performs data collection and does not handle data synchronization.
4. Added `x-client-timestamp` http header for time correction.

---
# agent 1.6.14-beta03
1. The child process only performs data collection and does not handle data synchronization.

---
# agent 1.6.14-beta02
1. Fixed scenarios where operations were not performed through the main process.
2. Optimized batch write and delete scenarios.

---
# agent 1.6.14-beta01
1. Same as agent 1.6.14-alpha02

---
# agent 1.6.14-alpha02
1. Protect ContentProvider query operations with try-catch
   and fix the issue where SQL LIMIT was not taking effect.

---
# agent 1.6.14-alpha01
1. Using ContentProvider to optimize compatibility issues in high-load multi-process data collection scenarios.
2. Removed the `android.permission.READ_PHONE_STATE` declaration from the SDK AndroidManifest;
   integrators need to add it themselves based on actual requirements.
3. Added `x-client-timestamp` http header for time correction.

---
# agent 1.6.13
1. Added `FTRUMConfig.setActionTrackingHandler` to support custom user action tracking.
   This handler allows developers to customize how user actions (clicks, touches, etc.) are tracked in RUM data.
   Developers can modify action names, add custom properties, or skip tracking for specific actions.
2. Added `FTRUMConfig.setViewActivityTrackingHandler` to support custom Activity view tracking.
   This handler allows developers to customize how Activity views are tracked in RUM data.
   Developers can modify view names, add custom properties, or skip tracking for specific Activities.
3. Added `FTRUMConfig.setViewFragmentTrackingHandler` to support custom Fragment view tracking.
   This handler allows developers to customize how Fragment views are tracked in RUM data.
   Developers can modify view names, add custom properties, or skip tracking for specific Fragments.
4. Fixed the issue where `FTRUMGlobalManager.addAction` was missing bindUserData binding related
   information, such as userid.
5. Added `load_time` data metrics for Fragment views.
6. Added `FTRUMGlobalManager.updateTime` for customizing the current view's loading time.
7. Improve compatibility with OkHttp 3.12.x versions.
8. The tags added by appendRUMGlobalContext can be updated during the session.

---
# agent 1.6.13-beta02
1. The tags added by appendRUMGlobalContext can be updated during the session.

---
# agent 1.6.13-beta01
1. Same as agent 1.6.13-alpha01.

---
# agent 1.6.13-alpha01
1. Added `FTRUMConfig.setActionTrackingHandler` to support custom user action tracking.
   This handler allows developers to customize how user actions (clicks, touches, etc.) are tracked in RUM data.
   Developers can modify action names, add custom properties, or skip tracking for specific actions.
2. Added `FTRUMConfig.setViewActivityTrackingHandler` to support custom Activity view tracking.
   This handler allows developers to customize how Activity views are tracked in RUM data.
   Developers can modify view names, add custom properties, or skip tracking for specific Activities.
3. Added `FTRUMConfig.setViewFragmentTrackingHandler` to support custom Fragment view tracking.
   This handler allows developers to customize how Fragment views are tracked in RUM data.
   Developers can modify view names, add custom properties, or skip tracking for specific Fragments.
4. Fixed the issue where `FTRUMGlobalManager.addAction` was missing bindUserData binding related
   information, such as userid.
5. Added `load_time` data metrics for Fragment views.
6. Added `FTRUMGlobalManager.updateTime` for customizing the current view's loading time.
7. Improve compatibility with OkHttp 3.12.x versions.

---
# agent 1.6.12
1. Added `FTRUMConfig.setEnableTraceWebView` to configure whether to enable WebView data collection via Android SDK. 
   Use `FTRUMConfig.setAllowWebViewHost` to filter host addresses.
2. Added `ContentHandlerHelperEx.onExceptionWithFilter` to filter local network errors.
3. Added `FTSDKConfig.setRemoteConfiguration` to support enabling remote conditional configuration.
   Added `FTSDKConfig.setRemoteConfigMiniUpdateInterval` to set the minimum update interval 
   after enabling remote control.

---
# agent 1.6.12-beta01
1. Fixed the issue of empty webHost configuration causing exceptions.

---
# agent 1.6.12-alpha02
1. Added `FTSDKConfig.setRemoteConfiguration` to support enabling remote conditional configuration. 
   Added `FTSDKConfig.setRemoteConfigMiniUpdateInterval` to set the minimum update interval after enabling remote control.

---
# agent 1.6.12-alpha01
1. Added `FTRUMConfig.setEnableTraceWebView` to configure whether to enable WebView data collection via Android SDK. 
   Use `FTRUMConfig.setAllowWebViewHost` to filter host addresses.
2. Added `ContentHandlerHelperEx.onExceptionWithFilter` to filter local network errors.

---
# agent 1.6.11
1. Added `FTRUMConfig.setEnableTraceUserViewInFragment` to support fragment view data collection, default is false.
2. Added `FTSDKConfig.setLineDataModifier` and `FTSDKConfig.setDataModifier` to support data write replacement 
   and data desensitization.
3. Added `FTRUMConfig.setSessionErrorSampleRate` to support error sampling. 
   When not sampled by `setSamplingRate`, errors can sample RUM data from the previous minute.
4. When `FTSDKConfig.setEnableAccessAndroidID(false)` is set, a local random `uuid` is used as `device_uuid`.
5. Optimized high-frequency log writing, data synchronization, and idle data closing logic.

---

# agent 1.6.11-beta02
1. Fixed the issue where data sampled by `FTRUMConfig.setSessionErrorSampleRate` was not reported.
2. Fixed the issue where, after hitting the `FTRUMConfig.setSessionErrorSampleRate` sample, 
   the page with errors had `view_error_count` as 0.

---
# agent 1.6.11-beta01
1. Same as agent 1.6.11-alpha03.

---
# agent 1.6.11-alpha03
1. Optimized data synchronization logic.
2. Optimized database idle close logic.
3. When FTSDKConfig.setEnableAccessAndroidID(false) is set, a local random uuid is used as device_uuid.

---
# agent 1.6.11-alpha02
1. Added `FTRUMConfig.setSessionErrorSampleRate` to support error sampling. When an error occurs, 
   RUM data from the previous minute can be sampled and collected.

---
# agent 1.6.11-alpha01
1. Optimized high-frequency log writing performance.
2. Added `FTRUMConfig.setEnableTraceUserViewInFragment` to support fragment view data collection, default is false.
3. Added `FTSDKConfig.setLineDataModifier` and `FTSDKConfig.setDataModifier` to support data write replacement, 
   suitable for data desensitization.

---
# agent 1.6.10
1. Supported adding a unique ResourceID to okhttp requests to solve the problem of trace_id 
   and span_id misalignment in high-concurrency identical requests. ft-plugin 1.3.5 
   and above support automatic ResourceID addition.
2. Fixed the issue of circular calls with other crash collection SDKs when initializing RUM configuration multiple times.
3. When jumping from a native page to a WebView page, the native page name is used to fill in view_referrer.
4. Fixed the issue where network request IOException rethrows altered the original type.
5. FTSDKConfig added `setProxy`, `setProxyAuthenticator`, and `setDns` for configuring Proxy, 
   ProxyAuthenticator, and Dns for OkHttp data sync requests.
6. OkHttp data sync requests support round-robin connection for known hostName DNS IPs.

---
# agent 1.6.10-beta01
1. Same as agent 1.6.10-alpha03.

---
# agent 1.6.10-alpha03
1. Supported adding a unique ResourceID to okhttp requests to solve the problem of trace_id 
  and span_id misalignment in high-concurrency identical requests. ft-plugin 1.3.5 
  and above support automatic ResourceID addition.
2. Fixed the issue of circular calls with other crash collection SDKs when initializing RUM configuration multiple times.
3. When jumping from a native page to a WebView page, the native page is used to fill in view_referrer in WebView data.

---
# agent 1.6.10-alpha03
1. Underlying network request library supports multiform.

---
# agent 1.6.10-alpha02
1. FTSDKConfig added Proxy, ProxyAuthenticator, and Dns configuration for data sync OkHttp requests.
2. OkHttp data sync supports round-robin optimization for known hostName DNS IPs.

---
# agent 1.6.10-alpha01
1. Network request IOException rethrow altered the original type issue.

---
# agent 1.6.9
1. Modified the isAppForeground judgment mechanism to adapt to privacy-sensitive information detection.
2. Added new `resource` data fields: `resource_first_byte_time`, `resource_dns_time`, `resource_download_time`,
  `resource_connect_time`, `resource_ssl_time`, supporting enhanced display of Resource timing in Guanceyun 
   and alignment with the APM flame graph timeline.
3. Optimized sync retry mechanism, removed the option to directly discard data with `FTSDKConfig.setDataSyncRetryCount(0)`.
4. FTSDKConfig.enableDataIntegerCompatible is enabled by default to be compatible with web numeric floating-point data.
5. Fixed the issue of duplicate crash data when initializing RUM configuration multiple times.

---

# agent 1.6.9-beta04
1. isAppInForeground adapted to privacy detection rules.
2. Fixed the issue of duplicate crash data when initializing RUM configuration multiple times.

---
# agent 1.6.9-beta03
1. Added X-Pkg-Id data tracking header.
2. FTSDKConfig.enableDataIntegerCompatible is enabled by default.

---
# agent 1.6.9-beta02
1. Data synchronization logic optimization.
2. Cold and hot start logic optimization.

---
# agent 1.6.9-beta01
1. Adjusted the calling rules of Utils.isAppForeground() before SDK initialization.
2. Added resource data fields: resource_first_byte_time, resource_dns_time, resource_download_time, 
   resource_connect_time, resource_ssl_time for optimized display in Guanceyun 
   and support for APM flame graph time alignment.

---
# agent 1.6.9-alpha01
1. Optimized sync retry mechanism, removed the option to directly discard data 
   with `FTSDKConfig.setDataSyncRetryCount(0)`.

---
# agent 1.6.8
1. Fixed inaccurate fps collection when initializing RUM configuration multiple times.
2. Fault tolerance for upgrading old version cached data.
3. FTRUMConfig.setOkHttpTraceHeaderHandler migrated to FTTraceConfig.setOkHttpTraceHeaderHandler.
4. Enhanced internal information and performance optimization for WebView SDK.

---
# agent 1.6.8-beta01
1. FTRUMConfig.setOkHttpTraceHeaderHandler migrated to FTTraceConfig.setOkHttpTraceHeaderHandler.

---
# agent 1.6.8-alpha02
1. Enhanced internal information and performance optimization for webview SDK.
2. Optimized SDK version information transmission.

---
# agent 1.6.8-alpha01
1. Fixed inaccurate fps collection when initializing RUM configuration multiple times.
2. Fault tolerance for upgrading old version data.

---
# agent 1.6.7
1. Supported custom FTTraceInterceptor.HeaderHandler and associating with RUM data.
2. Supported changing the content written by ASM for FTTraceInterceptor.HeaderHandler 
   via FTRUMConfig.setOkHttpTraceHeaderHandler, and for FTResourceInterceptor.ContentHandlerHelper 
   via FTRUMConfig.setOkHttpResourceContentHandler.
3. Optimized crash collection capability, adapted to scenarios where system.exit is triggered by some OS, 
   causing crash data to not be collected.
4. Fixed the issue where tag occasionally becomes an empty string, causing data to not be reported properly.
5. Optimized ASM OkHttpListener EventListener override logic, supports retaining original project
   EventListener event parameter passing.

---
# agent 1.6.7-beta02
1. Same as 1.6.7-alpha05.

---
# agent 1.6.7-alpha05
1. Optimized ASM OkHttpListener EventListener override logic, supports retaining original EventListener event parameter passing.

---
# agent 1.6.7-alpha04
1. Supported global setting of custom FTTraceInterceptor.HeaderHandler 
   and FTResourceInterceptor.ContentHandlerHelper methods.
2. Fixed the issue where tag occasionally becomes an empty string, causing data reporting failure.

---
# agent 1.6.7-alpha02
1. Optimized crash collection capability, adapted to scenarios where system.exit is triggered by some OS, 
  causing crash data to not be collected.
2. Fixed the issue where data sync fails when the network becomes available again.

---
# agent 1.6.7-beta01
1. Fixed wrong long task error configuration logic.

---
# agent 1.6.7-alpha01
1. Supported changing the corresponding traceId and spanId in the link.

---
# agent 1.6.6
1. Optimized network status and type acquisition, supported ethernet network type display.
2. Optimized the issue of frequent database closing during data writing in no-network state.
3. Fixed the issue where the number of data entries deviated from the set number when discarding logs and RUM discarding old data.
4. TV device button event adaptation, removed non-TV device tags.
5. Supported limiting RUM data entry count via `FTRUMConfig.setRumCacheLimitCount(int)`, default 100_000.
6. Supported limiting total cache size via `FTSDKConfig enableLimitWithDbSize(long dbSize)`. 
   After enabling, `FTLoggerConfig.setLogCacheLimitCount(int)` and `FTRUMConfig.setRumCacheLimitCount(int)` will be invalid.
7. Optimized Session refresh rules when device has no operation.

---
# agent 1.6.6-beta02
1. Adjusted SDK log output behavior.
2. Optimized log rules when discarding old data in certain scenarios.

---
# agent 1.6.6-beta01
1. Optimized db cache limit SDK log output.

---
# agent 1.6.6-alpha01
1. Inherited ft-sdk:1.6.5-alpha01, merged 1.6.5 updates.

---
# agent 1.6.5
1. Weakened WebView parameter null prompts during AOP process.
2. Optimized long Session update mechanism when app is in background.

---
# agent 1.6.5-beta04
1. Weakened WebView parameter null prompts during AOP process.
2. Optimized long Session update mechanism when app is in background.
3. Inherited ft-sdk:1.6.4.

---

# agent 1.6.5-alpha01
1. Optimized network status and type acquisition, supported ethernet network type display.
2. Optimized the issue of frequent database closing during data writing in no-network state.
3. Fixed the issue where data deviation occurred when discarding logs and RUM discarding old data.
4. TV device button event adaptation and non-TV device tag removal.
5. Supported limiting total cache size via `FTSDKConfig enableLimitWithDbSize(long dbSize)`.
   After enabling, `FTLoggerConfig#setLogCacheLimitCount(int)` and `FTRUMConfig#setRumCacheLimitCount(int)` 
   will be invalid.

---
# agent 1.6.5-beta03
1. Modified default `FTRUMConfig.setRumCacheLimitCount(int)` parameter value to 100_000.

---
# agent 1.6.5-beta02
1. Modified default `FTRUMConfig.setRumCacheLimitCount(int)` parameter value to 200_000.

---
# agent 1.6.5-beta01
1. Added RUM entry count limit feature, supported limiting SDK maximum cache entry data 
   via `FTRUMConfig.setRumCacheLimitCount(int)`. Supported specifying discard new data or old data
   via `FTRUMConfig.setRumCacheDiscardStrategy(strategy)`.
2. Added SDK internal log level filtering feature.

---
# agent 1.6.4
1. Optimized App startup time statistics on API 24 and above.
2. Supported setting detection time range via `FTRUMConfig.setEnableTrackAppUIBlock(true, blockDurationMs)`.

---
# agent 1.6.4-beta01
1. Same as 1.6.4-alpha02

---
# agent 1.6.4-alpha02
1. Optimized frequent printing of permission-related error logs.
2. Added method to set longtask detection time range.

---
# agent 1.6.4-alpha01
1. Optimized App startup time statistics in high-version systems.
2. Optimized dynamic property binding mechanism on pages.

---
# agent 1.6.3
1. Optimized performance of custom addAction during high-frequency calls.
2. Supported using FTSDKConfig.setCompressIntakeRequests to configure deflate compression for sync data.

---
# agent 1.6.3-beta03
1. Optimized deletion logic for closed and synced actions.
2. Supported deflate compression for sync data.

---
# agent 1.6.3-beta02
1. Fixed the issue of data loss during high-frequency addAction calls.

---
# agent 1.6.3-beta01
1. Optimized addAction data reporting mechanism.

---
# agent 1.6.2
1. RUM added addAction method, supported property extension attributes and frequent continuous data reporting.

---
# agent 1.6.2-beta01
1. Same as 1.6.2-alpha01

---
# agent 1.6.2-alpha01
1. RUM added addAction method, supported property extension attributes and frequent continuous data reporting.

---
# agent 1.6.1
1. Fixed the issue where custom startView called separately in RUM caused FTMetricsMTR monitoring 
  thread not being recycled.
2. Supported adding dynamic properties via FTSdk.appendGlobalContext(globalContext),
  FTSdk.appendRUMGlobalContext(globalContext), FTSdk.appendLogGlobalContext(globalContext).
3. Supported clearing unreported cached data via FTSdk.clearAllData().
4. SDK setSyncSleepTime maximum limit extended to 5000 ms.

---
# agent 1.6.1-beta02
1. Fixed missing Log link RUM dynamic tag issue.

---
# agent 1.6.1-beta01
1. Modified dynamic tag override logic.
2. Simplified metrics monitoring logic.
3. Added cache data clearing logic.

---
# agent 1.6.1-alpha04
1. Fixed the issue where custom startView called separately in RUM caused FTMetricsMTR monitoring 
  thread not being recycled.

---
# agent 1.6.1-alpha03
1. Optimized timing of dynamic tags assignment.
2. Added global, log, RUM globalContext property dynamic setting methods.

---
# agent 1.6.0
1. Optimized data storage and sync performance.(Upgrading from old versions to 1.6.0 requires 
 configuring FTSDKConfig.setNeedTransformOldCache for old data compatibility sync)
2. Fixed the issue where calling Log.w(String,Throwable) caused exceptions when using ft-plugin.

---
# agent 1.6.0-beta01
1. Same as 1.6.0-alpha02
2. Optimized Inner Log cache cleanup rules.
3. Optimized line protocol conversion storage rules.

---
# agent 1.6.0-alpha02
1. Fixed log discard strategy.
2. Optimized global json data storage.

---
# agent 1.6.0-alpha01
1. Optimized data serialization storage rules.
2. Restored database persistence.
3. Merged 1.5.2 features.

---
# agent 1.5.3-alpha01
1. Fixed the issue where calling Log.w(String,Throwable) caused exceptions when using ft-plugin.

---
# agent 1.5.2
1. Added local network error type prompts for Error network_error to supplement Resource data with 
   resource_status=0 scenarios.
2. Fixed uncaughtException rethrow passing issue when setEnableTrackAppCrash(false).

---
# agent 1.5.2-beta03
1. Same as agent 1.5.2-alpha03

---
# agent 1.5.2-alpha03
1. Fixed uncaughtException rethrow passing issue when setEnableTrackAppCrash(false).

---
# agent 1.5.2-beta02
1. Added simple description for resource local network errors.

---
# agent 1.5.2-beta01
1. Same as agent 1.5.2-alpha02

---
# agent 1.5.2-alpha02
1. Added local network error type prompts for resource.

---
# agent 1.5.1
1. Added other thread code stack traces for Java Crash and ANR.
2. Added additional logcat configuration for Java Crash, Native Crash, and ANR.
3. Fixed the issue of frequent session_id updates in long session scenarios with no action updates.

---
# agent 1.5.1-beta02
1. Added logcat Native Crash and ANR configuration.
2. Optimized logcat output format.

---
# agent 1.5.1-beta01
1. Same as agent 1.5.1-alpha03

---
# agent 1.5.1-alpha03
1. Added other thread code stack traces for ANR and Java Crash.
2. Added additional logcat for Java Crash.
3. Fixed occasional session_id refresh exception issues.

---
# agent 1.5.1-alpha02
1. Optimized session_id refresh mechanism in long-time scenarios.

---
# agent 1.5.1-alpha01
1. Fixed the issue of frequent session_id updates in long session scenarios with no action updates.

---
# agent 1.5.0
1. Added remote ip address resolution for RUM resource network requests.
2. Fixed array thread safety issues caused by high-concurrency network requests after enabling RUM SampleRate.
3. Optimized ConnectivityManager.registerDefaultNetworkCallback method fault tolerance.
4. Added line protocol Integer data compatibility mode to handle web data type conflicts.
5. Optimized automatic collection of control resource name id in Action click.
6. Optimized SDK config configuration reading exception fault tolerance.

---
# agent 1.5.0-beta01
1. Same as agent 1.5.0-alpha04

---
# agent 1.5.0-alpha04
1. RUM Log Trace config reading critical scenario fault tolerance.

---
# agent 1.5.0-alpha03
1. Added remote ip address resolution for RUM resource network requests.
2. Added line protocol Integer data compatibility mode to handle web data type conflicts.

---
# agent 1.4.7-alpha01
1. Fixed array thread safety issues caused by high-concurrency network requests after enabling RUM SampleRate.
2. Optimized ConnectivityManager.registerDefaultNetworkCallback method fault tolerance.
3. Optimized automatic collection of control resource name id in Action click.

---
# agent 1.4.6
1. Optimized SDK initialization fault tolerance.
2. Added Status.Debug type for new logs.
3. Adjusted console log level correspondence: Log.i -> info, Log.d -> debug.
4. FTLogger custom logs support custom status field.

---
# agent 1.4.6-beta01
1. Same as agent 1.4.6-alpha02

---
# agent 1.4.6-alpha02
1. Added Status.Debug type for new logs.
2. FTLogger custom logs support custom status field.

---
# agent 1.4.6-alpha01
1. Optimized SDK initialization fault tolerance.

---
# agent 1.4.5
1. Optimized duplicate initialization compatibility.
2. Optimized c/c++ crash collection data sync logic to avoid deadlocks caused by unexpected 
   interruption and exit in certain scenarios.
3. Optimized startAction Property attribute writing logic to avoid thread safety access issues.

---
# agent 1.4.5-beta01
1. Optimized duplicate initialization compatibility.
2. Optimized c/c++ crash collection data sync logic to avoid deadlocks caused by unexpected interruption 
   and exit in certain scenarios.
3. Optimized startAction Property attribute writing logic to avoid thread safety access issues.

---
# agent 1.4.4
1. Database connection fault tolerance protection.
2. Fixed the issue where child process configuration was ineffective when setOnlySupportMainProcess was true.
3. Fixed the issue where Crash would not rethrow when RUM View collection was not enabled.

---
# agent 1.4.4-beta01
1. Fixed the issue where Crash would not rethrow when RUM View collection was not enabled.

---
# agent 1.4.4-alpha02
1. Fixed the issue where child process configuration was ineffective when setOnlySupportMainProcess was true.

---
# agent 1.4.4-alpha01
1. Database connection fault tolerance protection.

---
# agent 1.4.3
1. Supported Dataway and Datakit address upload.
2. Supported sending RUM data of Action, View, Resource, LongTask, Error types.
   * View, Action page jumps, control clicks are automatically collected, requires ft-plugin
   * Resource, automatically collected, only supports Okhttp, and requires ft-plugin
   * Native Crash and ANR in Error require ft-native
3. Supported sending Log data, console automatic writing, requires ft-plugin.
4. Link http header propagation, only supports Okhttp, and requires ft-plugin.
5. Supported data sync parameter configuration, request entry data, sync interval time, and log cache entry count.
6. Supported converting SDK internal logs to files.

---
# agent 1.4.3-beta04
1. Optimized high-speed cache strategy judgment conditions.

---
# agent 1.4.3-beta03
1. Optimized Action, View data generation methods.
2. Optimized SDK initialization.

---
# agent 1.4.3-beta02
1. Try to stopView when crash to supplement page stay time.
2. Extended data sync request timeout and response time.
3. Optimized internal log output.
4. Fixed log discard old data strategy table name error issue.

---
# agent 1.4.3-beta01
1. Internal log file writing method try-with-resources protection.
2. agent 1.4.3-alpha01, agent 1.4.3-alpha02, agent 1.4.3-alpha03.

---
# agent 1.4.3-alpha03
1. Transmission performance optimization.
2. Added javadoc and source jar to aar.
3. Optimized internal file log management.

---
# agent 1.4.3-alpha02
1. Fixed Native Library version number acquisition method.

---
# agent 1.4.3-alpha01
1. Optimized Native Crash capture timing, will try to capture at crash moment.
2. Java Crash rethrows after crash is stored.

---
# agent 1.4.2-alpha04
1. Added sync request interval time setting.

---
# agent 1.4.2-alpha03
1. Added internal log to file method.

---
# agent 1.4.2-alpha02
1. Added log maximum cache entry count limit.

---
# agent 1.4.2-alpha01
1. Added automatic sync switch, supported manual data upload.
2. Added arch cpu abi architecture field display.
3. Supported modifying sync request single request data.

---
# agent 1.4.1-beta01
1. agent 1.4.1-alpha01, agent 1.4.1-alpha02, agent 1.4.1-alpha03.
2. Adjusted longtask occurrence time point.

---
# agent 1.4.1-alpha03
1. Adapted webview time precision issues.
2. Adjusted skywalking propagation header service parameters.

---
# agent 1.4.1-alpha02
1. Added ANR Error events and logs for crashes caused by ANR.
2. Fixed the issue where resource was not sent when using encryption algorithms in Okhttp,
   relying solely on ASM writing.
3. Supported custom Resource content, enabled together with FTTraceConfig.enableAutoTrace 
   and FTRUMConfig.enableTraceUserResource.

---
# agent 1.4.1-alpha01
1. Fixed the issue of duplicate view display data caused by ultra-long sessions.
2. Fixed the issue of duplicate view data transmission caused by unfinished resources.
3. Modified random uuid algorithm without userid binding.
4. Added view_update_time increment to solve data concurrent write order issues.
5. Optimized data synchronization.

---
# agent 1.4.0-beta01
1. Optimized data synchronization mechanism.
2. Optimized internal log output.
3. Supported public network Dataway upload.
4. Supported custom error_type field attributes in error data.

---
# agent 1.3.17-alpha05
1. Optimized log output.
2. Added Resource null pointer data processing.

---
# agent 1.3.17-alpha04
1. Optimized retry mechanism.
2. Added addResource header map support.

---
# agent 1.3.17-alpha03
1. Added maximum sync count for data synchronization.
2. Added internal log takeover object.

---
# agent 1.3.17-beta01
1. Same as agent 1.3.17-alpha01, agent 1.3.17-alpha02.
2. Added custom TraceHeader.

---
# agent 1.3.17-alpha02
1. Supported custom data methods for resource.

---
# agent 1.3.17-alpha01
1. Optimized resource body size calculation logic.

---
# agent 1.3.16-beta03
1. Adjusted Open API structure.
2. Added Resource local error logic.

---
# agent 1.3.16-beta02
1. Optimized SDK shutdown.
2. Same as agent 1.3.16-alpha05.

---
# agent 1.3.16-alpha05
1. Weakened READ_PHONE_STATE permission dependency, optimized READ_PHONE_STATE error prompts.

---
# agent 1.3.16-alpha04
1. SDK lazy loading exception handling.

---
# agent 1.3.16-beta01
1. Weakened service process SDK initialization error prompts.

---
# agent 1.3.16-alpha02
1. Modified rules when tag and field are empty.

---
# agent 1.3.16-alpha01
1. Optimized AOP MenuItem click ActionName description generation.

---
# agent 1.3.15-beta01
1. Same as 1.3.15-alpha03.

---
# agent 1.3.15-alpha03
1. Modified webview js call service assignment.

---
# agent 1.3.15-alpha02
1. Modified webview js callback upload logic.

---
# agent 1.3.15-alpha01
1. Adapted to Alibaba Cloud Sophix hot fix integration causing inability to collect View data.

---
# agent 1.3.14-beta01
1. Same as 1.3.13-alpha01.
2. Added custom log printing to console feature.

---
# agent 1.3.13-alpha01
1. Added custom env.

---
# agent 1.3.12-beta01
1. Fixed the issue where okhttp dependency resource was not sent in certain cases in 3.12.0.

---
# agent 1.3.12-alpha01
1. Optimized TraceHeader logic.

---
# agent 1.3.11-beta02
1. Fixed incorrect Fetch/XHR count statistics in viewer.

---
# agent 1.3.11-beta01
1. Same as agent 1.3.11-alpha01, agent 1.3.11-alpha02.

---
# agent 1.3.11-alpha02
1. Fixed compiler method index error but normal compilation issue.

---
# agent 1.3.11-alpha01
1. Optimized test log output.
2. Added AOP Ignore method.
3. Fixed Resource data loss issue.

---
# agent 1.3.10-beta01
1. Adjusted log Tag.

---
# agent 1.3.10-alpha01
1. Adapted to AGP 3.2.0 and below, removed lambda syntax.

---
# agent 1.3.9-beta02
1. Optimized DeviceMetricsMonitorType, ErrorMonitorType Config parameter passing.

---
# agent 1.3.9-beta01
1. Optimized test case adaptation.
2. Added documentation comments.

---
# agent 1.3.9-alpha02
1. Optimized dependency on com.google.android.material:material.

---
# agent 1.3.9-alpha01
1. Added extension parameters for View, Action, Resource, Error, LongTask, Log.

---
# agent 1.3.8-beta03
1. Modified DDtrace Header Propagation rules.

---
# agent 1.3.8-beta02
1. Fixed cold start time error in cross-platform SDK applications.

---
# agent 1.3.8-beta01
1. Same as agent 1.3.8-alpha05.

---
# agent 1.3.8-alpha05
1. Optimized startup time statistics.
2. Android ID acquisition logic.

---
# agent 1.3.8-alpha04
1. Changed MonitorType variable to Enum type.

---
# agent 1.3.8-alpha03
1. Adjusted user ext setting type.

---
# agent 1.3.8-alpha02
1. Fixed incorrect Guanceyun js and webview callback issues.

---
# agent 1.3.8-alpha01
1. Expanded user data binding fields.
2. Released FTAutoTrack WebView settings.

---
# agent 1.3.7-beta01
1. Same as agent 1.3.7-alpha04.

---
# agent 1.3.7-alpha04
1. Merged agent 1.3.6-beta05, agent 1.3.6-beta06 features.
2. Fixed battery maximum value error.

---
# agent 1.3.7-alpha03
1. Modified CPU metrics rules.

---
# agent 1.3.7-alpha02
1. Fixed cold start statistics time rules.

---
# agent 1.3.7-alpha01
1. Added battery, memory, CPU, FPS monitoring data support.

---
# agent 1.3.6-beta06
1. Modified session time statistics rules.

---
# agent 1.3.6-beta05
1. Optimized cold start rules.

---
# agent 1.3.6-beta04
1. Optimized action statistics.

---
# agent 1.3.6-beta03
1. Optimized cold start time statistics.

---
# agent 1.3.6-beta02
1. Added resource null value supplement logic.

---
# agent 1.3.6-bete01
1. agent 1.3.6-alpha01, 1.3.6-alpha02 update content.

---
# agent 1.3.6-alpha02
1. Optimized action time statistics.

---
# agent 1.3.6-alpha01
1. Fixed application code start statistics.
2. Fixed tcp, dns metrics exceptions.
3. Optimized long task statistics.

---
# agent 1.3.5-beta02
1. Fixed view rum config configuration issue.

---
# agent 1.3.5-beta01
1. Same as agent 1.3.5-alpha01.

---
# agent 1.3.5-alpha01
1. Fixed OkHttp 3.12 compatibility crash issue.

---
# agent 1.3.4-beta01
1. Removed redundant code.
2. Improved test case coverage.

---
# agent 1.3.4-alpha04
1. Removed config action association with app login.

---
# agent 1.3.4-alpha03
1. Branch merge updates.

---
# agent 1.3.4-alpha02
1. Fixed the issue where closed actions were still modified in duration.

---
# agent 1.3.4-alpha01
1. Modified startup event calculation rules.
2. Modified RUM page viewReferrer recording rules.

---
# agent 1.3.3-alpha05
1. Fixed ID overflow issue.

---
# agent 1.3.3-alpha04
1. Fixed DDTrace ID algorithm.

---
# agent 1.3.3-alpha02
1. Removed redundant Trace configuration.

---
# agent 1.3.3-alpha01
1. Supported Skywalking, W3c TraceParent.
2. Added single header support for Zipkin.

---
# agent 1.3.2-alpha02
1. Fixed Trace and log tags underlying mutual pollution issue.

---
# agent 1.3.2-alpha01
1. Added Trace Log and global globalContext features.

---
# agent 1.3.1-alpha15
1. Fixed the issue where ResponseBody could not be read when trace returned 400+ errors.

---
# agent 1.3.1-alpha14
1. Fixed the issue where single Trace data did not trigger synchronization.

---
# agent 1.3.1-alpha13
1. Optimized Trace resource scheduling logic, removed sequential call requirements.

---
# agent 1.3.1-alpha12
1. Optimized resource, trace automatic data capture logic judgment.

---
# agent 1.3.1-alpha11
1. Optimized thread pool nesting issues.

---
# agent 1.3.1-alpha10
1. Fixed thread pool deadlock issue.

---
# agent 1.3.1-alpha09
1. Fixed thread pool core count.

---
# agent 1.3.1-alpha08
1. Added crash protection for uninitialized situations.

---
# agent 1.3.1-alpha07
1. Handled occasional crash issues.

---
# agent 1.3.1-alpha06
1. Changed trace automatic configuration settings.

---
# agent 1.3.1-alpha05
1. Adjusted method visibility modifiers.
2. Added method comments.

---
# agent 1.3.1-alpha04
1. Optimized AddError.

---
# agent 1.3.1-alpha03
1. Adjusted user behavior tracking logic.
2. RUM opened LongTask interface.

---
# agent 1.3.1-alpha02
1. Restored url path encode display method.

---
# agent 1.3.1-alpha01
1. Opened RUM, TRACE interfaces.
2. Added third-party library judgment.

---
# agent 1.2.4-alpha02
1. Added escape rules to line protocol.

---
# agent 1.2.4-alpha01
1. Adjusted RUM resource statistics algorithm.

---
# agent 1.2.3-alpha03
1. Modified custom tag methods.

---
# agent 1.2.3-alpha02
1. Fixed SpanId and TraceId error logic in Trace feature.

---
# agent 1.2.3-alpha01
1. Added RUM custom tag feature.

---
# agent 1.2.2-alpha01
1. Added log level filtering feature.
2. Added console log prefix filtering feature.
3. Removed redundant permission declarations.

---
# agent 1.2.1-alpha01
1. Added log level filtering feature.
2. Added console log prefix filtering feature.
3. Removed redundant permission declarations.

---
# agent 1.2.0-alpha06
1. Adapted to okhttp3 3.12.0 version.

---
# agent 1.2.0-alpha05
1. Fixed UIBlock error condition reading issue.
2. Optimized UIBlock algorithm.

---
# agent 1.2.0-alpha04
1. Fixed action viewName error issue.
2. Optimized Tab click description.

---
# agent 1.2.0-alpha03
1. Fixed action id judgment issue.
2. Fixed click event action_name error issue.

---
# agent 1.2.0-alpha02
1. Modified action_name description.
2. Fixed action time statistics error issue.

---
# agent 1.2.0-alpha01
1. Added log link association RUM feature.
2. Added DDtrace link type.
3. Replanned RUM Trace Log configuration.

---
# agent 1.1.0-alpha11
1. Supplemented missing resource fields.

---
# agent 1.1.0-alpha10
1. Fixed rapid page switching statistics error issue.
2. Fixed statistics misalignment issue.

---
# agent 1.1.0-alpha09
1. Fixed statistics deviation issue.

---
# agent 1.1.0-alpha08
1. Modified response size statistics.

---
# agent 1.1.0-alpha07
1. Optimized action duration statistics.

---
# agent 1.1.0-alpha06
1. Fixed long task calculation issue.
2. Removed launch action viewid attribute.

---
# agent 1.1.0-alpha04
1. Optimized resource count statistics.
2. Added duration action logic.

---
# agent 1.1.0-alpha03
1. Added trace resource attributes.
2. Added user action configuration.

---
# agent 1.1.0-alpha02
1. Optimized action statistics algorithm.
2. Modified long task time calculation.

---
# agent 1.1.0-alpha01
1. RUM added action, session, view statistics features.

---
# agent 1.0.4-alpha17
1. Supplemented missing fields.

---
# agent 1.0.4-alpha16
1. Performed field replacement.

---
# agent 1.0.4-alpha15
1. Fixed device model space translation error.

---
# agent 1.0.4-alpha14
1. Further optimized page time statistics algorithm.

---
# agent 1.0.4-alpha13
1. Fixed deadlock and lag issues caused by no network.
2. Optimized app startup time statistics.

---
# agent 1.0.4-alpha12
1. Optimized data assembly, reduced transmission data.

---
# agent 1.0.4-alpha09
1. Fixed Trace address and transmission data structure issues.

---
# agent 1.0.4-alpha06
1. Fixed exposed methods.

---
# agent 1.0.4-alpha04
1. Adjusted data structure, optimized performance.
2. Provided time precision.

---
# agent 1.0.4-alpha03
1. Reduced features.
2. Added RUM data output.

---
# agent 1.0.4-alpha02
1. Fixed Object output data issue.
2. Optimized test cases.

---
# agent 1.0.4-alpha01
1. Added network response time statistics.
2. Added crash, lag, ANR statistics.

---
# agent 1.0.3-beta01
1. Fixed several error issues, released stable version.

---
# agent 1.0.3-alpha12
1. App startup reports one object data.

---
# agent 1.0.3-alpha09
1. Fixed log format issue.

---
# agent 1.0.3-alpha01
1. Added trace feature.
2. Added service retry mechanism.
3. Added log reporting feature.
4. Added event Flow log feature.

---
# agent 1.0.2-alpha14
1. Added page alias and event alias.

---
# agent 1.0.2-alpha01
1. Independent monitoring module, provided periodic reporting feature.

---
# agent 1.0.1-alpha10
1. Reporting flow chart.
2. Modified monitoring item indicator set.

---
# agent 1.0.0
1. User custom tracking points.
2. FT Gateway data synchronization.