-dontwarn com.ft.sdk.**

### ft-sdk lib
-keep class com.ft.sdk.** {*;}

### ft-native lib
-keep class ftnative.*{*;}

### Prevent class names in action_name from being obfuscated when retrieving actions ###
-keepnames class * extends android.view.View
-keepnames class * extends android.view.MenuItem

### keep Class.forName(“okhttp3.OkHttpClient”)
-keepnames class okhttp3.OkHttpClient

### keep Class.forName(“com.tencent.smtt.sdk.WebView”)
-keepnames class com.tencent.smtt.sdk.WebView

# keep com.google.android.material.tabs.TabLayout click events
-keepclassmembers class com.google.android.material.tabs.TabLayout$Tab {
    *;
}