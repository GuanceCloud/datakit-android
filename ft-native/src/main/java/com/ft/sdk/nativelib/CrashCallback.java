package com.ft.sdk.nativelib;

/**
 * Native Crash 崩溃回调对象
 */
public interface CrashCallback {
    /**
     * Native Crash 后进行回调，返回 crashPath 崩溃文件路径
     *
     * @param crashPath 目标崩溃 dump 文件
     */
    void onCrash(String crashPath);
}