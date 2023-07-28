package com.cloudcare.ft.mobile.sdk.demo

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.widget.ProgressBar
import com.cloudcare.ft.mobile.sdk.demo.manager.SettingConfigManager


class WebViewActivity : BaseActivity() {

    var webView: WebView? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_webview)

        val data = SettingConfigManager.readSetting()

        webView = findViewById<WebView>(R.id.webView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)

        // 配置 WebView 设置
        val webSettings: WebSettings = webView!!.settings
        webSettings.javaScriptEnabled = true // 启用 JavaScript

        // 设置 WebViewClient 以处理页面加载
        webView!!.webChromeClient = object : WebChromeClient() {
            override fun onReceivedTitle(view: WebView, title: String) {
                super.onReceivedTitle(view, title)
                setTitle(title)
            }

            override fun onProgressChanged(view: WebView, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.progress = newProgress
                if (newProgress == 100) {
                    progressBar.visibility = View.GONE
                } else {
                    progressBar.visibility = View.VISIBLE
                }
            }
        }

        // 加载网页
        webView!!.loadUrl("${data.demoApiAddress}?requestUrl=${data.getUserInfoUrl()}")

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.webview_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.webview_refresh -> {
                webView?.reload()
            }
        }

        return super.onOptionsItemSelected(item)
    }

}