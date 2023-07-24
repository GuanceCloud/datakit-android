package com.cloudcare.ft.mobile.sdk.demo.manager

import android.content.Context
import com.cloudcare.ft.mobile.sdk.demo.*
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_API_ADDRESS
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_APP_ID
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_DATAKIT_ADDRESS
import com.ft.sdk.FTApplication

data class SettingData(val datakitAddress: String, val demoApiAddress: String, val appId: String)

object SettingConfigManager {

    private const val PREFS_USER_DATA_NAME = "gc_demo_sdk_setting"
    private const val KEY_DEMO_DATAKIT_ADDRESS = "datakitAddress"
    private const val KEY_DEMO_API_ADDRESS = "demoApiAddress"
    private const val KEY_DEMO_APP_ID = "appId"


    fun saveSetting(data: SettingData) {
        val sharedPreferences = FTApplication.getApplication()
            .getSharedPreferences(PREFS_USER_DATA_NAME, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString(KEY_DEMO_DATAKIT_ADDRESS, data.datakitAddress)
        editor.putString(KEY_DEMO_API_ADDRESS, data.demoApiAddress)
        editor.putString(KEY_DEMO_APP_ID, data.appId)
        editor.apply()

    }

    fun readSetting(): SettingData {
        val sharedPreferences = FTApplication.getApplication()
            .getSharedPreferences(PREFS_USER_DATA_NAME, Context.MODE_PRIVATE)

        return SettingData(
            sharedPreferences.getString(
                KEY_DEMO_DATAKIT_ADDRESS, if (BuildConfig.DEBUG)
                    BuildConfig.ACCESS_SERVER_URL
                else
                    DEFAULT_DATAKIT_ADDRESS
            )!!,
            sharedPreferences.getString(
                KEY_DEMO_API_ADDRESS, if (BuildConfig.DEBUG)
                    BuildConfig.DEMO_API_URL
                else
                    DEFAULT_API_ADDRESS
            )!!,
            sharedPreferences.getString(
                KEY_DEMO_APP_ID, if (BuildConfig.DEBUG)
                    BuildConfig.RUM_APP_ID
                else
                    DEFAULT_APP_ID
            )!!

        )
    }


}