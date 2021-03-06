package com.cloudcare.ft.mobile.sdk.demo

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.ft.sdk.FTRUMGlobalManager
import com.ft.sdk.FTTraceManager
import com.ft.sdk.garble.bean.AppState
import com.ft.sdk.garble.bean.ErrorType
import com.ft.sdk.garble.bean.NetStatusBean
import com.ft.sdk.garble.bean.ResourceParams
import com.ft.sdk.garble.http.RequestMethod
import com.ft.sdk.garble.utils.LogUtils
import com.ft.sdk.garble.utils.Utils
import okhttp3.*
import okhttp3.EventListener
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Proxy
import java.util.*
import kotlin.collections.HashMap


class ManualActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        findViewById<Button>(R.id.manual_action_btn).setOnClickListener {
            FTRUMGlobalManager.get().startAction("[action button]", "click")
        }

        findViewById<Button>(R.id.manual_http_btn).setOnClickListener {
            Thread {
                val uuid = UUID.randomUUID().toString()
                val url = "https://www.guance.com"
                //获取 trace 头标识
                val headers = FTTraceManager.get().getTraceHeader(uuid, url)

                var response: Response?

                val params = ResourceParams()
                val netStatusBean = NetStatusBean()

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

                        params.responseContentType = response!!.header("Content-Type")
                        params.responseConnection = response!!.header("Connection")
                        params.responseContentEncoding = response!!.header("Content-Encoding")
                        params.responseHeader = response!!.headers.toString()
                        params.requestHeader = request.headers.toString()
                        params.resourceStatus = response!!.code
                        params.resourceMethod = request.method
                        params.url = url
                        FTRUMGlobalManager.get().addResource(uuid, params, netStatusBean)

                        val requestHeaderMap = HashMap<String, String>()
                        val responseHeaderMap = HashMap<String, String>()
                        request.headers.forEach {
                            requestHeaderMap[it.first] = it.second
                        }
                        response!!.headers.forEach {
                            responseHeaderMap[it.first] = it.second

                        }

                    }
                    response!!
                }.eventListener(object : EventListener() {
                    override fun callEnd(call: Call) {
                        super.callEnd(call)
                        //发送 resource 指标数据
                        FTRUMGlobalManager.get().addResource(uuid, params, netStatusBean)

                    }

                    override fun callStart(call: Call) {
                        super.callStart(call)
                        netStatusBean.fetchStartTime = Utils.getCurrentNanoTime()
                    }

                    override fun responseHeadersStart(call: Call) {
                        super.responseHeadersStart(call)
                        netStatusBean.responseStartTime = Utils.getCurrentNanoTime()

                    }

                    override fun responseBodyEnd(call: Call, byteCount: Long) {
                        super.responseBodyEnd(call, byteCount)
                        netStatusBean.responseEndTime = Utils.getCurrentNanoTime()

                    }

                    override fun dnsEnd(
                        call: Call,
                        domainName: String,
                        inetAddressList: List<InetAddress>
                    ) {
                        super.dnsEnd(call, domainName, inetAddressList)
                        netStatusBean.dnsStartTime = Utils.getCurrentNanoTime()

                    }

                    override fun dnsStart(call: Call, domainName: String) {
                        super.dnsStart(call, domainName)
                        netStatusBean.dnsEndTime = Utils.getCurrentNanoTime()

                    }

                    override fun secureConnectStart(call: Call) {
                        super.secureConnectStart(call)
                        netStatusBean.sslEndTime = Utils.getCurrentNanoTime()

                    }

                    override fun secureConnectEnd(call: Call, handshake: Handshake?) {
                        super.secureConnectEnd(call, handshake)
                        netStatusBean.sslStartTime = Utils.getCurrentNanoTime()

                    }

                    override fun connectStart(
                        call: Call,
                        inetSocketAddress: InetSocketAddress,
                        proxy: Proxy
                    ) {
                        super.connectStart(call, inetSocketAddress, proxy)
                        netStatusBean.tcpStartTime = Utils.getCurrentNanoTime()

                    }

                    override fun connectEnd(
                        call: Call,
                        inetSocketAddress: InetSocketAddress,
                        proxy: Proxy,
                        protocol: Protocol?
                    ) {
                        super.connectEnd(call, inetSocketAddress, proxy, protocol)
                        netStatusBean.tcpEndTime = Utils.getCurrentNanoTime()

                    }
                }).build()

                val builder: Request.Builder = Request.Builder().url(url)
                    .method(RequestMethod.GET.name, null)

                val res: Response = client.newCall(builder.build()).execute()
                LogUtils.i("log", res.body?.string() + "")

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
        FTRUMGlobalManager.get().startView("ManualActivity");
    }

    override fun onPause() {
        super.onPause()
        FTRUMGlobalManager.get().stopView()
    }
}