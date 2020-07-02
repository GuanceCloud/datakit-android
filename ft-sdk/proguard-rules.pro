# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

#保护代码中的Annotation不被混淆
-keepattributes *Annotation

##避免混淆泛型
-keepattributes Signature

#抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

-keep class com.ft.sdk.FTSdk{
  *;
}
-keep class com.ft.sdk.FTMonitor{
  *;
}
-keep class com.ft.sdk.FTSDKConfig{
  *;
}
-keep class com.ft.sdk.FTTrack{
   public *;
}

-keep class com.ft.sdk.FTAutoTrack{
     *;
}

-keep enum com.ft.sdk.FTAutoTrackType{
     *;
}

-keep class com.ft.sdk.garble.http.ResponseData{
     *;
}

-keep class com.ft.sdk.garble.http.HttpBuilder{
    *;
}

-keep enum com.ft.sdk.garble.http.RequestMethod{
    *;
}

-keep class com.ft.sdk.garble.http.INetEngine{
    *;
}

-keep class com.ft.sdk.garble.http.NetWorkTracerInterceptor{
    *;
}

-keep class com.ft.sdk.garble.http.OkHttpEngine{
    *;
}

-keep class * extends com.ft.sdk.garble.http.ResponseData{
     *;
}

-keep class com.ft.sdk.garble.SyncCallback{
    *;
}

-keep class com.ft.sdk.garble.bean.TrackBean{
    *;
}

-keep class com.ft.sdk.garble.bean.LogBean{
    *;
}

-keep class com.ft.sdk.garble.bean.KeyEventBean{
    *;
}

-keep class com.ft.sdk.garble.bean.ObjectBean{
    *;
}


-keep class com.ft.sdk.garble.bean.Status{
    *;
}

-keep class com.ft.sdk.MonitorType{
    *;
}

-keep class com.ft.sdk.garble.utils.TrackLog{
    *;
}


