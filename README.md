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


## Sampling configuration (unreleased)

Use `FTRUMConfig.setSessionSamplingRate(float)`,
`FTLoggerConfig.setLogSamplingRate(float)`, and
`FTTraceConfig.setTraceSamplingRate(float)` with percentages in `[0,100]`,
default `100`. For example, `0.1f` means 0.1%. Non-finite or out-of-range values
throw `IllegalArgumentException` without changing the previous value.

The old `setSamplingRate` / `getSamplingRate` methods are deprecated and retain
`[0,1]` units. Multiply old values by 100 when migrating. Explicit new settings
win regardless of setter order, including zero. Remote configuration continues
to override local configuration, including when a legacy remote key is used.

Remote keys `sessionSamplingRate`, `logSamplingRate`, and `traceSamplingRate`
use the same percentage scale. The corresponding legacy keys `rumSampleRate`,
`logSampleRate`, and `traceSampleRate` retain `[0,1]` units. Within one remote
response the new key wins; an invalid new value ignores that field, preserves
the currently effective runtime value and never falls back to the old key in
that response. Session Replay and error-session sampling are unchanged.
