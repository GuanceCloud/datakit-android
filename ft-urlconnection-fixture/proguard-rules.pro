# Keep acceptance entry points callable by the separate test APK; SDK runtime is still processed by R8.
-keep class com.ft.fixture.** { *; }
-keep class com.ft.fixturejar.** { *; }
-keep class com.ft.fixtureaar.** { *; }
-dontwarn okhttp3.**
-dontwarn com.tencent.smtt.**
-dontwarn io.dcloud.**
-dontwarn com.ft.sdk.sessionreplay.**
-dontwarn ftnative.**
# The separate AndroidJUnitRunner APK also uses Kotlin runtime classes from the target APK.
-keep class kotlin.** { *; }
