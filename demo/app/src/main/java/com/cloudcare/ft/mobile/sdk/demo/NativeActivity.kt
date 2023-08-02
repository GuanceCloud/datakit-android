package com.cloudcare.ft.mobile.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.cloudcare.ft.mobile.sdk.demo.nativelib.NativeLib

class NativeActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        title = "Native View"
        setContentView(R.layout.activity_native_view)

        findViewById<Button>(R.id.native_dynamic_rum_tag_btn).setOnClickListener {
            val intent = Intent(this@NativeActivity, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            DemoApplication.setDynamicParams(this, "set from dynamic")
            DemoApplication.setSDK(this@NativeActivity)
        }

        findViewById<View>(R.id.native_crash_java_kotlin_data_btn).setOnClickListener { v: View? ->
            Thread { val i = 1 / 0 }.start()
        }
        findViewById<View>(R.id.native_crash_c_cpp_data_btn).setOnClickListener { v: View? ->
            NativeLib().crashTest()
        }


        findViewById<View>(R.id.native_long_task_data_btn).setOnClickListener { v: View? ->
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }

    }

}
