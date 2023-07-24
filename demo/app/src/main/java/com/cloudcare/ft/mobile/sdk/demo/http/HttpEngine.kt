package com.cloudcare.ft.mobile.sdk.demo.http

import com.cloudcare.ft.mobile.sdk.demo.utils.Utils
import com.ft.sdk.FTApplication
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
        const val ERROR_CODE_NET_WORK_ERROR = 101
    }

    var code: Int = ERROR_CODE_LOCAL

    init {
        code = returnResult.code
    }

    var errorMessage = "";

    internal fun parse() {
        if (code != ERROR_CODE_LOCAL) {
            if (returnResult.result != null) {
                val json = JSONObject(returnResult.result)
                if (code == HttpURLConnection.HTTP_OK) {
                    onHttpOk(json)
                } else {
                    onHttpError(json)
                }
            } else {
                code = ERROR_CODE_BODY_EMPTY
                errorMessage = "Body Empty"
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
    private const val API_USER_INFO: String = "$API_SEGMENT/user"

    private lateinit var apiAddress: String

    fun initAddress(url: String) {
        this.apiAddress = url;
    }

    fun login(user: String, password: String): ConnectData {
        val url = "$apiAddress$API_LOGIN"
        val json = """{"username": "$user", "password": "$password"}"""
        val builder: Request.Builder = Request.Builder().url(url)
            .method("post", json.toRequestBody("application/json".toMediaTypeOrNull()))
        return OkHttpClientInstance.get()
            .newCall(builder.build()).execute().convertFTData()
    }

    fun userinfo(): UserData {
        val url = "$apiAddress$API_USER_INFO"
        val builder: Request.Builder = Request.Builder().url(url).method("get", null);
        return request(builder.build())
    }

    private inline fun <reified T : BaseData> request(request: Request): T {
        if (!Utils.isNetworkAvailable(FTApplication.getApplication())) {
            return ReturnResult(BaseData.ERROR_CODE_NET_WORK_ERROR).convertFTData()
        }
        return OkHttpClientInstance.get().newCall(request).execute().convertFTData()
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



