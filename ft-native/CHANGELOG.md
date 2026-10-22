# native 1.1.4
1. Fixed custom logcat settings being applied under the wrong collection switch: Native Crash settings now follow Native Crash enablement, and ANR settings follow native ANR enablement. This restores custom logcat line limits when only one collector is enabled, including Native Crash on Android 11+ where the Agent disables native ANR collection.
2. Improved public API Javadocs. Public method signatures and bundled native `.so` libraries are unchanged from 1.1.3.

---
# native 1.1.4-beta01
1. Fixed custom logcat settings being applied under the wrong collection switch: Native Crash settings now follow Native Crash enablement, and ANR settings follow native ANR enablement. This restores custom logcat line limits when only one collector is enabled, including Native Crash on Android 11+ where the Agent disables native ANR collection.
2. Improved public API Javadocs. Public method signatures and bundled native `.so` libraries are unchanged from 1.1.3.

---
# native 1.1.3
1. Added null address detection for native crashes, and made libft_native_exp_lib.so use stricter null and wild pointer checks when performing backtraces with libunwind.so.
2. Prevented secondary crashes in libunwind.so during native crash handling.

----------------------
# native 1.1.3-alpha02
1. Prevented secondary crashes in libunwind.so.

----------------------
# native 1.1.3-alpha01
1. Added null address detection for native crashes, and made libft_native_exp_lib.so use stricter null 
   and wild pointer checks when performing backtraces with libunwind.so.

----------------------
# native 1.1.2
1. Added support for Android 16K page size (Android 15+ compatibility)

----------------------
# native 1.1.1
1. Add logcat configuration functionality for Native Crash and ANR

----------------------
# native 1.1.1-alpha01
1. Add logcat configuration functionality for Native Crash and ANR

----------------------
# native 1.1.0
1. Support capturing ANR Crash
2. Support capturing C/C++ Native Crash
3. Support collecting application runtime status during crashes
4. Support ANR and Native Crash trigger callbacks

----------------------
# native 1.1.0-beta01
1. native 1.1.0-alpha01

----------------------
# native 1.1.0-alpha01
1. Add ANR and Native Crash callbacks

----------------------
# native 1.0.0-beta01
1. Adjust package structure

----------------------
# native 1.0.0-alpha05
1. Lower android miniSDK version

----------------------
# native 1.0.0-alpha03
1. Add runtime status data collection

----------------------
# native 1.0.0-alpha02
1. Add native crash data collection
