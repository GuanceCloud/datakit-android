package com.cloudcare.ft.mobile.sdk.demo.http

import android.util.Log
import com.cloudcare.ft.mobile.sdk.demo.utils.Utils
import com.ft.sdk.FTApplication
import com.ft.sdk.FTLogger
import com.ft.sdk.FTRUMGlobalManager
import com.ft.sdk.garble.bean.AppState
import com.ft.sdk.garble.bean.ErrorType
import com.ft.sdk.garble.bean.Status
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.net.HttpURLConnection

abstract class BaseData(private val returnResult: ReturnResult) {
    companion object {
        const val ERROR_CODE_LOCAL = 0
        const val ERROR_CODE_BODY_EMPTY = 100
        const val ERROR_CODE_NET_WORK_NO_AVAILABLE = 101
        const val ERROR_CODE_NET_WORK_CONNECT_ERROR = 102
    }

    var code: Int = ERROR_CODE_LOCAL

    init {
        code = returnResult.code
    }

    var errorMessage = "";

    internal fun parse() {
        if (code != ERROR_CODE_LOCAL) {
            if (returnResult.result != null) {
                try {
                    val json = JSONObject(returnResult.result)
                    if (code == HttpURLConnection.HTTP_OK) {
                        onHttpOk(json)
                    } else {
                        when (code) {
                            ERROR_CODE_NET_WORK_NO_AVAILABLE -> {
                                onHttpError(JSONObject("{\"error\":\"network no available\"}"))
                            }

                            ERROR_CODE_NET_WORK_CONNECT_ERROR -> {
                                onHttpError(JSONObject("{\"error\":\"connect error:1\"}"))
                            }

                            else -> {
                                onHttpError(json)
                            }
                        }
                    }
                } catch (_: Exception) {
                    onHttpError(JSONObject("{\"error\":\"connect error:0\"}"))
                    FTLogger.getInstance()
                        .logBackground("JSON Parse Error with:${returnResult.result}", Status.ERROR)
                }

            } else {
                code = ERROR_CODE_BODY_EMPTY
                errorMessage = "connect error:3"
            }
        }
    }

    abstract fun onHttpOk(json: JSONObject)

    fun onHttpError(json: JSONObject) {
        errorMessage = json.optString("error")
    }
}

class ConnectData(result: ReturnResult) : BaseData(result) {
    override fun onHttpOk(json: JSONObject) {
    }
}

class UserData(result: ReturnResult) : BaseData(result) {
    var username: String? = ""
    var email: String? = ""
    var avatar: String? = ""

    constructor() : this(ReturnResult(ERROR_CODE_LOCAL))

    override fun onHttpOk(json: JSONObject) {
        this.username = json.optString("username")
        this.email = json.optString("email")
        this.avatar = json.optString("avatar")
    }
}


class ReturnResult(val code: Int, val result: String? = null)


object HttpEngine {

    private const val API_SEGMENT = "/api"
    private const val API_LOGIN: String = "$API_SEGMENT/login"
    const val API_USER_INFO: String = "$API_SEGMENT/user"
    private const val API_CONNECT: String = "/connect"
    private const val API_DATAKIT_PING: String = "/v1/ping"

    private lateinit var apiAddress: String

    fun initAPIAddress(url: String) {
        this.apiAddress = url;
    }

    fun login(user: String, password: String): ConnectData {
        val url = "$apiAddress$API_LOGIN"
        val json = """{"username": "$user", "password": "$password"}"""
        val builder: Request.Builder = Request.Builder().url(url)
            .method("post", json.toRequestBody("application/json".toMediaTypeOrNull()))
        return request(builder.build())
    }

    fun userinfo(): UserData {
        val url = "$apiAddress$API_USER_INFO"
        val builder: Request.Builder = Request.Builder().url(url)
        return request(builder.build())
    }

    fun datakitPing(datakitUrl: String): ConnectData {
        val url = "$datakitUrl$API_DATAKIT_PING"
        val builder: Request.Builder = Request.Builder().url(url)
        return request(builder.build())
    }

    fun apiConnect(demoAPIUrl: String): ConnectData {
        val url = "$demoAPIUrl$API_CONNECT"
        val builder: Request.Builder = Request.Builder().url(url)
        return request(builder.build())
    }


    private inline fun <reified T : BaseData> request(request: Request): T {
        if (!Utils.isNetworkAvailable(FTApplication.getApplication())) {
            return ReturnResult(BaseData.ERROR_CODE_NET_WORK_NO_AVAILABLE).convertFTData()
        }
        try {
            return OkHttpClientInstance.get().newCall(request).execute().convertFTData()

        } catch (e: Exception) {
            FTRUMGlobalManager.get()
                .addError(
                    Log.getStackTraceString(e),
                    e.message,
                    ErrorType.JAVA,
                    AppState.RUN
                )
        }

        return ReturnResult(BaseData.ERROR_CODE_NET_WORK_CONNECT_ERROR).convertFTData()


    }


    private inline fun <reified T : BaseData> Response.convertFTData(): T {
        val result = ReturnResult(this.code, this.body?.string())
        val data = Activator.createInstance(T::class.java, result)
        data.parse()
        return data
    }

    private inline fun <reified T : BaseData> ReturnResult.convertFTData(): T {
        val data = Activator.createInstance(T::class.java, this)
        data.parse()
        return data
    }


}


object Activator {
    fun <T : Any> createInstance(type: Class<T>, returnResult: ReturnResult): T {
        val constructor = type.getDeclaredConstructor(ReturnResult::class.java)
        return constructor.newInstance(returnResult)
    }
}



