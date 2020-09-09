package com.ft.utils

import android.content.Context
import com.ft.AccountUtils
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
                val account = AccountUtils.getProperty(context, AccountUtils.TEST_ACCOUNT)
                val pwd = AccountUtils.getProperty(context, AccountUtils.TEST_PWD)
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
         * 获取 LOG 查询接口的 body 部分
         */
        @JvmStatic
        fun buildLogBody(): String {
            return JSONObject().apply {
                put("queries", JSONArray().apply {
                    put(JSONObject().apply {
                        put("measurements", JSONArray().apply {
                            put("logging")
                        })
                        put("fields", JSONArray())
                        put("filter", JSONObject().apply {
                            put("tags", JSONArray())
                        })
                        put("groupBy", JSONArray())
                        put("orderBy", JSONArray().apply {
                            put(JSONObject().apply {
                                put("name", "__timestampMs")
                                put("method", "desc")
                            })
                        })
                        put("limit", 50)
                        put("offset", 0)
                        put("intervalTime", "1d")
                        put("timeRange", JSONArray().apply {
                            put(System.currentTimeMillis() - 1000 * 60 * 2)
                            put(System.currentTimeMillis())
                        })
                    })
                })
            }.toString()
        }

        /**
         * 获取 Object 查询接口的 body 部分
         */
        @JvmStatic
        fun buildObjectBody(content:String): String {
            return JSONObject().apply {
                put("queries", JSONArray().apply {
                    put(JSONObject().apply {
                        put("index", "object")
                        put("body", JSONObject().apply {
                            put("aggs", JSONObject().apply {
                                put("groupitem",JSONObject().apply {
                                    put("terms",JSONObject().apply {
                                        put("field","__tags.__class.keyword")
                                        put("size",5000)
                                    })
                                    put("aggs",JSONObject().apply {
                                        put("object_count",JSONObject().apply {
                                            put("value_count",JSONObject().apply {
                                                put("field","__name.keyword")
                                            })
                                        })
                                    })
                                })
                            })
                            put("sort",JSONArray().apply {
                                put(JSONObject().apply {
                                    put("__tags.__class.keyword", JSONObject().apply {
                                        put("order", "asc")
                                    })
                                })
                            })
                            put("size",0)
                            put("query",JSONObject().apply {
                                put("regexp",JSONObject().apply {
                                    put("__tags.__class.keyword",".*$content.*")
                                })
                            })
                        })
                    })
                })
            }.toString()
        }

        /**
         * 获取 Track 查询接口的 body 部分
         */
        @JvmStatic
        fun buildTrackBody(measurement:String):String{
            return JSONObject().apply {
                put("query",JSONObject().apply {
                    put("measurements",JSONArray().apply {
                        put(measurement)
                    })
                })
            }.toString()
        }
    }

}