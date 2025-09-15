# plugin 1.1.9-alpha06
1. Removed ASM for Activity and Fragment.
2. Added code line number display for Exceptions.

---
# plugin 1.1.9-alpha05
1. Fixed thread safety issues with global variables.
2. Debug log add tid info

---
# plugin 1.1.9-alpha04
1. Added verboseLog param for more logs.

---
# plugin 1.1.9-alpha03
1. Added try-catch and supplemented error logs.

---
# plugin 1.1.9-alpha02
1. Fixed the issue where ASM writes did not take effect in 1.1.9-alpha01.

---
# plugin 1.1.9-alpha01
1. Optimize plugin parameter stability during concurrent compilation
2. Add ASM write to FTAutoTrack.trackRequestBuilder method, requires ft-sdk version 1.6.10 or above
3. Support automatic capture of Log.println logs, requires ft-sdk version 1.6.8 or above
4. Fix issue where sourcemap symbol file was not generated when minifyEnabled was not enabled
5. Support generating sourcemap only without uploading by setting generateSourceMapOnly to true
6. Optimize automatic acquisition and upload of native symbol so files, support custom nativeLibPath

---
# plugin 1.1.8
1. Support automatic capture of React Native WebView events

---
# plugin 1.1.7
1. Fixed the issue where WebView subclass overridden methods caused circular calls after ASM writing, preventing WebView content from loading
   (involving methods loadUrl, loadData, loadDataWithBaseURL, postUrl)
2. IgnoreAOP supports declaration in classes for method ignoring across the entire class
3. Added ignorePackages configuration to support ASM ignoring through package path configuration

---
# plugin 1.1.7-alpha04
1. Added ignorePackages to ignore ASM through package name method

---
# plugin 1.1.7-alpha03
1. Fixed TingYun conflict logic

---
# plugin 1.1.7-alpha01
1. Fixed webview custom loadUrl, loadData, loadDataWithBaseURL, postUrl internal methods causing calls after ASM writing, preventing webview content from loading
2. IgnoreAOP supports class declaration for overall AOP ignoring
3. Optimized datakit source map upload method

---
# plugin 1.1.6
1. Support automatic datakit source map upload, support native symbol upload
2. Support capturing Application cold/hot start, Activity page navigation, View, ListView, Dialog, Tab click events
3. Support Webview Js listener method writing
4. Support automatic Okhttp Trace and Resource data writing
5. Support AGP versions below 7.4.2
6. Support IgnoreAOP ignore markers
7. Support compatibility with Alibaba Cloud hotfix framework

---
# plugin 1.1.6-beta01
1. Same as plugin 1.1.6-alpha01

---
# plugin 1.1.6-alpha01
1. Fixed issue where native source symbol could not be uploaded after setting flavor

---
# plugin 1.1.5-beta01
1. Same as plugin 1.1.5-alpha01

---
# plugin 1.1.5-alpha01
1. Adapted to Alibaba Cloud Sophix hotfix integration issue where View data could not be collected

---
# plugin 1.1.4-beta02
1. Performance optimization

---
# plugin 1.1.4-beta01
1. Same as plugin 1.1.4-alpha01

---
# plugin 1.1.4-alpha01
1. Support Annotation IgnoreAOP

---
# plugin 1.1.3-beta01
1. Optimized com.google.android.material:material control calls

---
# plugin 1.1.2-beta01
1. Merged plugin 1.1.2-alpha01 and plugin 1.1.1-alpha04

---
# plugin 1.1.2-alpha01
1. Fixed circular call issue after WebView derived class injection

---
# plugin 1.1.1-alpha04
1. Fixed cmake path main project error
2. Added proguard support
---
# plugin 1.1.1-alpha03
1. Optimized FtExt condition coverage rules

---
# plugin 1.1.1-alpha02
1. Support productFlavor configuration

---
# plugin 1.1.1-alpha01
1. Added native symbol file mapping file packaging functionality

---
# plugin 1.1.0-alpha02
1. Fixed MultiDex library incompatibility issue

---
# plugin 1.1.0-alpha01
1. Application startup event capture
2. Modified compatibility version rules

---
# plugin 1.0.1-beta01
1. Fixed version comparison issue

---
# plugin 1.0.1-alpha12
1. Added trace functionality

