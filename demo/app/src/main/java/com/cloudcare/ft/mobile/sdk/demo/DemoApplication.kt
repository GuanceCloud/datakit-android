package com.cloudcare.ft.mobile.sdk.demo

import android.app.Application
import com.ft.sdk.*

/**
 * BY huangDianHua
 * DATE:2019-12-13 11:44
 * Description:
 */
class DemoApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val ftSDKConfig = FTSDKConfig.builder(
            AccountUtils.getProperty(this, AccountUtils.ACCESS_SERVER_URL)
        )
            .setXDataKitUUID("ft-dataKit-uuid-001")
            .setUseOAID(true)//是否使用OAID
            .setDebug(true)//是否开启Debug模式（开启后能查看调试数据）
        FTSdk.install(ftSDKConfig)

        FTSdk.initLogWithConfig(
            FTLoggerConfig()
                .setEnableConsoleLog(true)
                .setServiceName("ft-sdk-demo")
                .setEnableLinkRumData(true)
                .setEnableCustomLog(true)
                .setSamplingRate(0.8f)

        )
        FTSdk.initRUMWithConfig(
            FTRUMConfig()
                .setRumAppId(AccountUtils.getProperty(this, AccountUtils.RUM_APP_ID))
                .setEnableTraceUserAction(true)
                .setSamplingRate(0.8f)
                .setExtraMonitorTypeWithError(MonitorType.ALL)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppANR(true)
        )

        FTSdk.initTraceWithConfig(
            FTTraceConfig()
                .setServiceName("ft-sdk-demo")
                .setSamplingRate(0.8f)
                .setEnableLinkRUMData(true)
        )

    }
}
