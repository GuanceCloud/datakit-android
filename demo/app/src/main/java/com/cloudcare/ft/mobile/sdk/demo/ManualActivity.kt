package com.cloudcare.ft.mobile.sdk.demo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ft.sdk.FTRUMGlobalManager
import com.ft.sdk.FTTraceManager
import com.ft.sdk.garble.bean.AppState
import com.ft.sdk.garble.bean.ErrorType
import com.ft.sdk.garble.bean.NetStatusBean
import com.ft.sdk.garble.bean.ResourceParams
import com.ft.sdk.garble.http.RequestMethod
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class ManualActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        findViewById<Button>(R.id.manual_action_btn).setOnClickListener {
            FTRUMGlobalManager.get().startAction("[action button]", "click")
        }

        findViewById<Button>(R.id.manual_http_btn).setOnClickListener {
            Thread {
                val uuid = "key";
                val url = "https://www.guance.com"
                //获取 trace 头标识
                val headers = FTTraceManager.get().getTraceHeader(uuid, url)

                var response: Response?

                val client: OkHttpClient = OkHttpClient.Builder().addInterceptor { chain ->
                    //开始 resource
                    FTRUMGlobalManager.get().startResource(uuid)
                    val original = chain.request()

                    val requestBuilder = original.newBuilder()
                    for (key in headers.keys) {
                        requestBuilder.header(key!!, headers[key]!!)
                    }
                    val request = requestBuilder.build()

                    response = chain.proceed(request)

                    //结束 resource
                    FTRUMGlobalManager.get().stopResource(uuid)

                    if (response != null) {
                        val params = ResourceParams()
                        params.responseContentType = response!!.header("Content-Type")
                        params.responseConnection = response!!.header("Connection")
                        params.responseContentEncoding = response!!.header("Content-Encoding")
                        params.responseHeader = response!!.headers.toString()
                        params.requestHeader = request.headers.toString()
                        params.resourceStatus = response!!.code
                        params.resourceMethod = request.method
                        params.url = url
                        val netStatusBean = NetStatusBean()
                        //eventListener 中获取 connect 等时间数值

                        //发送 resource 指标数据
                        FTRUMGlobalManager.get().addResource(uuid, params, netStatusBean)

                        val requestHeaderMap = HashMap<String, String>()
                        val responseHeaderMap = HashMap<String, String>()
                        request.headers.forEach {
                            requestHeaderMap[it.first] = it.second
                        }
                        response!!.headers.forEach {
                            responseHeaderMap[it.first] = it.second

                        }
                        //发送 trace 数据
                        FTTraceManager.get().addTrace(
                            url,
                            request.method,
                            requestHeaderMap,
                            responseHeaderMap,
                            response!!.code,
                            ""
                        )
                    }

                    response!!
                }.build()

                val builder: Request.Builder = Request.Builder().url(url)
                    .method(RequestMethod.GET.name, null)

                client.newCall(builder.build()).execute()
            }.start()
        }
        findViewById<Button>(R.id.manual_error_btn).setOnClickListener {
            FTRUMGlobalManager.get()
                .addError("crash stack", "error msg", ErrorType.JAVA, AppState.RUN)
        }

        findViewById<Button>(R.id.manual_longtask_btn).setOnClickListener {
            FTRUMGlobalManager.get().addLongTask("long task", 6000000)
        }
    }

    override fun onResume() {
        super.onResume()
        FTRUMGlobalManager.get().startView("ManualActivity", "MainActivity");
    }

    override fun onPause() {
        super.onPause()
        FTRUMGlobalManager.get().stopView()
    }
}