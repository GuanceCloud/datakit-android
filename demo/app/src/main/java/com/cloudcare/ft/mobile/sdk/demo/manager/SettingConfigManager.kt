package com.cloudcare.ft.mobile.sdk.demo.manager

import android.content.Context
import com.cloudcare.ft.mobile.sdk.demo.BuildConfig
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_API_ADDRESS
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_APP_ID
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_DATAKIT_ADDRESS
import com.cloudcare.ft.mobile.sdk.demo.http.HttpEngine
import com.ft.sdk.FTApplication
import org.json.JSONObject

private const val PREFS_USER_DATA_NAME = "gc_demo_sdk_setting"
private const val KEY_DEMO_DATAKIT_ADDRESS = "datakitAddress"
private const val KEY_DEMO_API_ADDRESS = "demoApiAddress"
private const val KEY_DEMO_APP_ID = "demoAndroidAppId"

data class SettingData(val datakitAddress: String, val demoApiAddress: String, val appId: String) {
    fun getUserInfoUrl(): String {
        return demoApiAddress + HttpEngine.API_USER_INFO
    }

    companion object {
        fun readFromJson(jsonString: String): SettingData? {
            try {
                val json = JSONObject(jsonString)
                val datakitAddress = json.optString(KEY_DEMO_DATAKIT_ADDRESS, "")
                val demoApiAddress = json.optString(KEY_DEMO_API_ADDRESS, "")
                val appId = json.optString(KEY_DEMO_APP_ID, "")
                return SettingData(datakitAddress, demoApiAddress, appId)
            } catch (_: Exception) {


            }
            return null

        }
    }
}

object SettingConfigManager {

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