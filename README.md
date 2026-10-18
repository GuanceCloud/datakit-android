# GuanceCloud SDK Android


![](https://img.shields.io/badge/dynamic/json?label=ft-sdk&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/agent/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-native&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/native/version.json&link=https://github.com/GuanceCloud/datakit-android
) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin/version.json&link=https://github.com/GuanceCloud/datakit-android) ![](https://img.shields.io/badge/dynamic/json?label=ft-plugin-legacy&color=orange&query=$.version&uri=https://static.guance.com/ft-sdk-package/badge/android/plugin_legacy/version.json&link=https://github.com/GuanceCloud/datakit-android)
## Introduction

GuanceCloud Application Monitoring can collect metric data from various Android applications and analyze the performance of each Android app client in a visualized way.

## Example

 [GuanceCloud SDK Android Demo](https://github.com/GuanceDemo/guance-app-demo/tree/master/src/android/demo)   

## Documentation
 [Documentation Center](https://docs.guance.com/real-user-monitoring/android/app-access/)   

### Cold-start Action launch type

Automatic cold-start Actions report `app_launch_type` for the observed startup interval,
from the initial Application startup hook through completion of the first-draw measurement:

| Value | Startup states |
| --- | --- |
| `foreground` | Foreground-origin startup with no observed background transition before the first draw completes |
| `background` | Background-origin startup, including entering the foreground to complete the first draw |
| `foreground_and_background` | Foreground-origin startup that enters the background before the first draw completes |

The original total duration is retained. Transitions after cold-start completion do not
change the recorded type, including when SDK initialization delays reporting. Hot-start
Actions do not include this field.

### Native RUM View timing metrics

Native View events include the following optional fields, in **nanoseconds**:

- `network_settled_time`: time from View start to the last completion among Resources starting strictly less than 100 ms after View start. All qualifying Resources must finish. Accepted Resource or network Error writes contribute their original end time; discarded Resources release pending state without contributing a duration. No qualifying completions or unfinished Resources means the field is omitted. Closing the View freezes the result, including an absent result.
- `interaction_to_next_view_time`: time from the preceding View's last accepted `tap`, `swipe`, `click`, or `back` Action to the **new View's start**. The interval must be greater than zero and strictly less than 3 seconds. Ordinary automatic and immediate Actions use their start timestamp; a waiting Action explicitly stopped by the SDK uses its stop timestamp. Custom and scroll Actions do not qualify.

Collection is automatic with native RUM View/Resource/Action tracking. The defaults are fixed at 100 ms and 3 seconds; this implementation does not expose Datadog's custom identifier interfaces. Native fields use the SDK's flat event format. Browser-owned WebView events retain their existing fields. Network settled time is not a network-idle timer, and interaction time ends at `startView`, not first draw or full page readiness. The history is process-local and bounded to four Views/interactions; unresolved values are never reconstructed across process restarts.

## Open Source Code Usage
* [ft-session-replay](ft-session-replay) is a secondary development based on datadog [dd-sdk-android-session-replay](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay)
* [ft-session-replay-material](ft-session-replay-material) is a secondary development based on datadog [dd-sdk-android-session-replay-material](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay-material)
* [ft-session-replay-compose](ft-session-replay-compose) is a secondary development based on datadog [dd-sdk-android-session-replay-compose](https://github.com/DataDog/dd-sdk-android/tree/develop/features/dd-sdk-android-session-replay-compose)

## Third-party Licenses
- Datadog [dd-sdk-android](https://github.com/DataDog/dd-sdk-android/blob/develop/LICENSE)
