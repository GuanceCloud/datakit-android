package com.ft

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.security.InvalidParameterException
import java.util.*

/**
 * BY huangDianHua
 * DATE:2019-12-27 10:07
 * Description:
 */
class SyncDataUtils {
    companion object {
        /**
         * 登录接口需要的head
         */
        @JvmStatic
        val loginHead = hashMapOf(Pair("Content-Type", "application/json"))

        /**
         * 生成登录接口需要的body
         */
        @JvmStatic
        fun getLoginBody(context: Context): String {
            return JSONObject().apply {
                val account = AccountUtils.getProperty(context,AccountUtils.TEST_ACCOUNT)
                val pwd = AccountUtils.getProperty(context,AccountUtils.TEST_PWD)
                if (account == "null") {
                    throw InvalidParameterException("请先设置测试账号")
                    return@apply
                }
                if (pwd == "null") {
                    throw InvalidParameterException("请先设置测试账号密码")
                    return@apply
                }
                put("username", account)
                put("password", pwd)
                put("workspaceUUID", "qwe123123")
            }.toString()
        }


        /**
         * 获取查询数据接口的 head
         */
        @JvmStatic
        fun getQueryHead(token: String): HashMap<String, String> {
            return hashMapOf(Pair("Accept", "application/json, text/plain, */*"),
                    Pair("Content-Type", "application/json;charset=UTF-8"),
                    Pair("X-FT-Auth-Token", token))
        }

        /**
         * 获取查询接口的 body 部分
         */
        @JvmStatic
        fun buildPostBody(): String {
            return JSONObject().apply {
                put("qtype", "http")
                put("query", JSONObject().apply {
                    put("fields", JSONArray().apply {
                        put(JSONObject().apply {
                            put("name", "event")
                        })
                    })
                    put("filter", JSONObject().apply {
                        put("tags", JSONArray().apply {
                            put(JSONObject().apply {
                                put("condition", "")
                                put("name", "application_identifier")
                                put("operation", "=")
                                put("value", "com.ft")
                            })
                        })
                        put("time", JSONArray().apply {
                            put(System.currentTimeMillis() - 1000 * 60 * 3)
                            put(System.currentTimeMillis())
                        })
                    })
                    put("limit", 1000)
                    put("measurements", JSONArray().apply {
                        put("mobile_tracker")
                    })
                    put("offset", 0)
                    put("orderBy", JSONArray().apply {
                        put(JSONObject().apply {
                            put("name", "time")
                            put("method", "desc")
                        })
                    })
                    put("tz", "Asia/Shanghai")
                })
            }.toString()
        }
    }

}