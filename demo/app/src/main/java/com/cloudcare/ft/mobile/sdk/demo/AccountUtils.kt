package com.cloudcare.ft.mobile.sdk.demo

import android.content.Context
import java.io.IOException
import java.io.InputStream
import java.util.*

object AccountUtils {
    const val ACCESS_SERVER_URL = "ACCESS_SERVER_URL"
    const val RUM_APP_ID = "RUM_APP_ID"

    /**
     * 本地配置文件中读取登录的账户数据
     */
    fun getProperty(context: Context, key: String): String {
        //testaccount.properties 该文件需要在资源目录assets中创建。如果本地没有需要自行创建，
        //文件的内容为
        //TEST_ACCOUNT = 你的账号
        //TEST_PWD = 你的账号密码
        val properties = Properties()
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open("testaccount.properties")
            properties.load(inputStream)
            val en = properties.propertyNames()
            while (en.hasMoreElements()) {
                val keyVar = en.nextElement() as String
                if (key.contains(keyVar)) {
                    return properties.getProperty(key)
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return ""
    }
}