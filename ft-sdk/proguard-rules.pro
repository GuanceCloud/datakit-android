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

-keep class **.R$* {
    <fields>;
}
-keep public class * extends android.content.ContentProvider
-keepnames class * extends android.view.View

# AlertDialog
-keep class android.app.AlertDialog {*;}
-keep class android.support.v7.app.AlertDialog {*;}
-keep class androidx.appcompat.app.AlertDialog {*;}
-keep class * extends android.support.v7.app.AlertDialog {*;}
-keep class * extends androidx.appcompat.app.AlertDialog {*;}
-keep class * extends android.app.AlertDialog {*;}

# Fragment
-keep class android.app.Fragment {*;}
-keep class android.support.v4.app.Fragment {*;}
-keep class androidx.fragment.app.Fragment {*;}
-keepclassmembers class * extends androidx.fragment.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}
-keepclassmembers class * extends android.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}
-keepclassmembers class * extends android.support.v4.app.Fragment {
    public void setUserVisibleHint(boolean);
    public void onViewCreated(android.view.View, android.os.Bundle);
    public void onHiddenChanged(boolean);
    public void onResume();
}

# TabLayout
-keep class android.support.design.widget.TabLayout$Tab {*;}
-keep class com.google.android.material.tabs.TabLayout$Tab {*;}
-keep class * extends android.support.design.widget.TabLayout$Tab {*;}
-keep class * extends com.google.android.material.tabs.TabLayout$Tab {*;}


# SwitchCompat
-keep class android.support.v7.widget.SwitchCompat {*;}
-keep class androidx.appcompat.widget.SwitchCompat {*;}
-keep class * extends android.support.v7.widget.SwitchCompat {*;}
-keep class * extends androidx.appcompat.widget.SwitchCompat {*;}

# ContextCompat
-keep class android.support.v4.content.ContextCompat {*;}
-keep class androidx.core.content.ContextCompat {*;}
-keep class * extends android.support.v4.content.ContextCompat {*;}
-keep class * extends androidx.core.content.ContextCompat {*;}

# AppCompatActivity
-keep class android.support.v7.app.AppCompatActivity {
    public android.support.v7.app.ActionBar getSupportActionBar();
}
-keep class androidx.appcompat.app.AppCompatActivity {
    public androidx.appcompat.app.ActionBar getSupportActionBar();
}
-keep class * extends android.support.v7.app.AppCompatActivity {
    public android.support.v7.app.ActionBar getSupportActionBar();
}
-keep class * extends androidx.appcompat.app.AppCompatActivity {
    public androidx.appcompat.app.ActionBar getSupportActionBar();
}

#ActionBar
-keep class android.support.v7.app.ActionBar {*;}
-keep class androidx.appcompat.app.ActionBar {*;}
-keep class * extends android.support.v7.app.ActionBar {*;}
-keep class * extends androidx.appcompat.app.ActionBar {*;}


#SDK

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}


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

-keep class com.ft.sdk.garble.http.ResponseData{
     *;
}

-keep class com.ft.sdk.garble.http.HttpBuilder{
    *;
}

-keep class com.ft.sdk.garble.http.INetEngine{
    *;
}

-keep class com.ft.sdk.FTNetWorkInterceptor{
    *;
}

-keep class com.ft.sdk.garble.http.OkHttpEngine{
    *;
}

-keep class * extends com.ft.sdk.garble.http.ResponseData{
     *;
}

-keep class com.ft.sdk.garble.manager.AsyncCallback{
    *;
}

-keep class com.ft.sdk.garble.bean.LineProtocolBean{
    *;
}

-keep class com.ft.sdk.garble.bean.LogData{
    *;
}

-keep class com.ft.sdk.garble.bean.ObjectBean{
    *;
}

-keep class com.ft.sdk.garble.utils.TrackLog{
    *;
}
-keep class com.ft.sdk.InitSDKProcessException{
    *;
}
-keep class com.ft.sdk.garble.manager.FTExceptionHandler{
    *;
}
-keep enum com.ft.sdk.LogCacheDiscard {
    *;
}
-keep enum com.ft.sdk.nativelib.*


