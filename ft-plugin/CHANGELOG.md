# plugin 1.3.6-alpha03
1. Removed ASM for Activity and Fragment.
2. Added code line number display for Exceptions.
3. Added log output to file functionality.  

---
# plugin 1.3.6-alpha02
1. Added verboseLog param for more logs.
2. Added try-catch and supplemented error logs.
---
# plugin 1.3.6-alpha01
1. Numeric boundary handling in FTMethodAdapter

---
# plugin 1.3.5
1. Optimize plugin parameter stability during concurrent compilation
2. Add ASM write to FTAutoTrack.trackRequestBuilder method, requires ft-sdk version 1.6.10 or above
3. Support automatic capture of Log.println logs, requires ft-sdk version 1.6.8 or above

---
# plugin 1.3.5-alpha02
1. Add ASM write to FTAutoTrack.trackRequestBuilder method, requires ft-sdk version 1.6.10 or above

---
# plugin 1.3.5-alpha01
1. Optimize plugin parameter stability during concurrent compilation
2. Support automatic capture of Log.println logs, requires ft-sdk version 1.6.8-beta01 or above

---
# plugin 1.3.4
1. Optimize error log output
2. Fix issue where sourcemap symbol file was not generated when minifyEnabled was not enabled
3. Support generating sourcemap only without uploading by setting generateSourceMapOnly to true

---
# plugin 1.3.4-alpha03
1. Optimize error log output
2. Fix issue affecting symbol file generation when minifyEnabled is not enabled

---
# plugin 1.3.4-alpha01
1. Support generating only sourcemap files via manualUpload configuration

---
# plugin 1.3.3
1. Optimize automatic acquisition and upload of native symbol so files, support custom nativeLibPath

---
# plugin 1.3.3-alpha01
1. Optimize automatic acquisition and upload of native symbol so files, support custom nativeLibPath

---
# plugin 1.3.2
1. Support automatic capture of React Native WebView events

---
# plugin 1.3.2-alpha01
1. Compilation fault tolerance handling

---
# plugin 1.3.1
1. Add asmVersion configuration, supports asm7 - asm9, default is asm9
2. Fix issue where WebView subclass overridden methods caused infinite loops after ASM write, preventing WebView content from loading (methods involved: loadUrl, loadData, loadDataWithBaseURL, postUrl)
3. IgnoreAOP supports class-level declaration to ignore all methods in the class
4. Add ignorePackages configuration, supports ignoring ASM by package path

---
# plugin 1.3.1-beta01
1. Add source code and Java Doc to release configuration

---
# plugin 1.3.1-alpha03
1. Add ignorePackages to ignore ASM by package name

---
# plugin 1.3.1-alpha02
1. Add asmVersion configuration
2. Fix issue where custom WebView methods loadUrl, loadData, loadDataWithBaseURL, and postUrl caused infinite loops after ASM write, preventing WebView content from loading
3. IgnoreAOP supports class-level declaration for overall AOP ignore

---
# plugin 1.3.1-alpha01
1. Compatible with asm9

---
# plugin 1.3.0
1. Support automatic upload of datakit source map and native symbol
2. Support capturing Application cold/hot start, Activity page jumps, View/ListView/Dialog/Tab click events
3. Support Webview JS listener method write
4. Support automatic write of Okhttp Trace and Resource data
5. Support Gradle 8.0, AGP 8.0
6. Support IgnoreAOP ignore annotation
7. Support compatibility with Alibaba Cloud hotfix framework

---
# plugin 1.3.0-beta01
1. plugin 1.2.2-alpha02

---
# plugin 1.2.2-alpha02
1. Adapt to new datakit sourcemap rules

---
# plugin 1.2.2-beta01
1. Same as plugin 1.2.2-alpha01

---
# plugin 1.2.2-alpha01
1. Fix issue where native symbol could not be uploaded after setting flavor
2. Compatible with AGP 8.0 not generating /intermediates/cmake/debug/obj path

---
# plugin 1.2.1-beta01
1. Fix issue with sourceMap upload errors

---
# plugin 1.2.1-alpha01
1. Fix issue where View data could not be collected after integrating Alibaba Cloud Sophix hotfix

---
# plugin 1.2.0-beta03
1. Compatible with Java 11

---
# plugin 1.2.0-beta02
1. Fix issue with application uuid and plugin version not being generated
2. Performance optimization

---
# plugin 1.2.0-beta01
1. plugin 1.2.0-alpha01

---
# plugin 1.2.0-alpha01
1. Adapt to Gradle 8.0, AGP 8.0

---
# plugin 1.1.4-alpha01
1. Support Annotation IgnoreAOP

---
# plugin 1.1.3-beta01
1. Optimize com.google.android.material:material widget calls

---
# plugin 1.1.2-beta01
1. plugin 1.1.2-alpha01, plugin 1.1.1-alpha04 merged

---
# plugin 1.1.2-alpha01
1. Fix issue where WebView derived class injection caused infinite loops

---
# plugin 1.1.1-alpha04
1. Fix issue with main project cmake path error
2. Add proguard support

---
# plugin 1.1.1-alpha03
1. Optimize FtExt condition coverage rules

---
# plugin 1.1.1-alpha02
1. Support productFlavor configuration

---
# plugin 1.1.1-alpha01
1. Add native symbol file mapping file packaging function

---
# plugin 1.1.0-alpha02
1. Fix MultiDex library incompatibility issue

---
# plugin 1.1.0-alpha01
1. Application startup event capture
2. Modify compatibility rules

---
# plugin 1.0.1-beta01
1. Fix version comparison issue

---
# plugin 1.0.1-alpha12
1. Add trace feature

