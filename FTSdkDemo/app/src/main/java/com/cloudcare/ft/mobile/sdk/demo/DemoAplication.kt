package com.cloudcare.ft.mobile.sdk.demo

import android.app.Application
import com.ft.sdk.FTSDKConfig
import com.ft.sdk.FTSdk

/**
 * BY huangDianHua
 * DATE:2019-12-13 11:44
 * Description:
 */
class DemoAplication : Application() {
    private val accesskey_id = "accid"
    private val accessKey_secret = "accsk"
    override fun onCreate() {
        super.onCreate()
        val ftSDKConfig = FTSDKConfig(
            "http://10.100.64.106:19557/v1/write/metrics",//服务器地址
            true,
            accesskey_id,
            accessKey_secret
        )
        ftSDKConfig.isUseOAID = true
        ftSDKConfig.isDebug = true
        FTSdk.install(ftSDKConfig)
    }
}
