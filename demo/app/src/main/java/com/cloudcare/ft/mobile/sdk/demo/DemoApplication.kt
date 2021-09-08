package com.cloudcare.ft.mobile.sdk.demo

import android.app.Application
import com.ft.sdk.*
import com.ft.sdk.garble.bean.Status

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

        //配置 Log
        FTSdk.initLogWithConfig(
            FTLoggerConfig()
                .setEnableConsoleLog(true)
//                .setEnableConsoleLog(true,"log prefix")
                .setServiceName("ft-sdk-demo")
                .setEnableLinkRumData(true)
                .setEnableCustomLog(true)
//                .setLogLevelFilters(arrayOf(Status.CRITICAL))
                .setSamplingRate(0.8f)

        )
        //配置 RUM
        FTSdk.initRUMWithConfig(
            FTRUMConfig()
                .setRumAppId(AccountUtils.getProperty(this, AccountUtils.RUM_APP_ID))
                .setEnableTraceUserAction(true)
                .setSamplingRate(0.8f)
                .setExtraMonitorTypeWithError(MonitorType.ALL)
                .setEnableTrackAppCrash(true)
                .setEnableTrackAppANR(true)
        )

        //配置 Trace
        FTSdk.initTraceWithConfig(
            FTTraceConfig()
                .setServiceName("ft-sdk-demo")
                .setSamplingRate(0.8f)
                .setEnableLinkRUMData(true)
        )

    }
}
