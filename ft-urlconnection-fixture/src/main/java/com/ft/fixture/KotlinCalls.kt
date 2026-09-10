package com.ft.fixture
import java.net.URL
import java.net.Proxy
object KotlinCalls {
    @JvmStatic fun open(url: URL) = url.openConnection()
    @JvmStatic fun open(url: URL, proxy: Proxy) = url.openConnection(proxy)
}
