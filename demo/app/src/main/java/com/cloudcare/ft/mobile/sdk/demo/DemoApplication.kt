package com.cloudcare.ft.mobile.sdk.demo

import android.app.Application
import android.content.Context
import com.ft.sdk.*

/**
 * BY huangDianHua
 * DATE:2019-12-13 11:44
 * Description:
 */
class DemoApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        setSDK(this)
    }

    companion object {
        private const val CUSTOM_STATIC_TAG = "static_tag"
        private const val CUSTOM_DYNAMIC_TAG = "dynamic_tag"
        private const val SP_STORE_DATA= "store_data"

        fun setSDK(context: Context) {
            val ftSDKConfig = FTSDKConfig.builder(
                AccountUtils.getProperty(context, AccountUtils.ACCESS_SERVER_URL)
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

            val sp = context.getSharedPreferences(SP_STORE_DATA, MODE_PRIVATE)
            val customDynamicValue = sp.getString(CUSTOM_DYNAMIC_TAG, "not set")

            //配置 RUM
            FTSdk.initRUMWithConfig(
                FTRUMConfig()
                    .setRumAppId(AccountUtils.getProperty(context, AccountUtils.RUM_APP_ID))
                    .setEnableTraceUserAction(true)
                    .setEnableTraceUserView(true)
                    .setSamplingRate(0.8f)
                    .addGlobalContext(CUSTOM_STATIC_TAG, BuildConfig.CUSTOM_VALUE)
                    .addGlobalContext(CUSTOM_DYNAMIC_TAG, customDynamicValue!!)
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

        fun setDynamicParams(context: Context, value: String) {
            val sp = context.getSharedPreferences(SP_STORE_DATA, MODE_PRIVATE)
            sp.edit().putString(CUSTOM_DYNAMIC_TAG, value).apply()

        }

    }


}

