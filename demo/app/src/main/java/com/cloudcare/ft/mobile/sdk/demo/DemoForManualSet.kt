package com.cloudcare.ft.mobile.sdk.demo

import android.app.Application
import com.cloudcare.ft.mobile.sdk.demo.DemoApplication.Companion.setSDK
import com.ft.sdk.FTAutoTrack

/**
 * 在不使用 ft-plugin 的前提下使用这种方式初始化
 */
class DemoForManualSet : Application() {

    override fun onCreate() {
        super.onCreate()
        //需要在 SDK 初始化前调用
        FTAutoTrack.startApp(null)
        //设置 SDK 配置
        setSDK(this)

    }
}