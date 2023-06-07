package com.cloudcare.ft.mobile.sdk.custom.okhttp

import com.ft.sdk.FTResourceEventListener
import okhttp3.Call
import okhttp3.EventListener

/**
 * 自定义 Okhttp EventListener，如果你的工程中不需要自定义，可以直接调用
 * @see com.ft.sdk.FTResourceEventListener.FTFactory()
 *
 */
class CustomEventListenerFactory : FTResourceEventListener.FTFactory() {
    override fun create(call: Call): EventListener {
        //在这里实现自定义方法
        return super.create(call)
    }
}