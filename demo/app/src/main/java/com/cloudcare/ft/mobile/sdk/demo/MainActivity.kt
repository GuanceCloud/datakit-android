package com.cloudcare.ft.mobile.sdk.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ft.sdk.FTLogger
import com.ft.sdk.garble.bean.LogData
import com.ft.sdk.garble.bean.Status
import com.ft.sdk.garble.http.RequestMethod
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val phonePermission = Manifest.permission.READ_PHONE_STATE
    private val cameraPermission = Manifest.permission.CAMERA
    private val findLocationPermission = Manifest.permission.ACCESS_FINE_LOCATION
    private var requestPermissions = arrayOf<String>()
    private val REQUEST_CODE = 0x001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(storagePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(storagePermission)
            }
            if (checkSelfPermission(phonePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(phonePermission)
            }
            if (checkSelfPermission(cameraPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(cameraPermission)
            }
            if (checkSelfPermission(findLocationPermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(findLocationPermission)
            }
            if (requestPermissions.isNotEmpty()) {
                requestPermissions(requestPermissions, REQUEST_CODE)
            }
        }

        findViewById<Button>(R.id.http_request_btn).setOnClickListener {
            Thread {
                val client: OkHttpClient = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                val builder: Request.Builder = Request.Builder().url("http://www.baidu.com")
                    .method(RequestMethod.GET.name, null)

                client.newCall(builder.build()).execute()
            }.start()


        }

        findViewById<Button>(R.id.write_log_btn).setOnClickListener {
            FTLogger.getInstance().logBackground("test", Status.INFO)


            FTLogger.getInstance().logBackground(mutableListOf(LogData("test1", Status.INFO)))
        }

        findViewById<Button>(R.id.dynamic_rum_tag_btn).setOnClickListener {
            DemoApplication.setDynamicParams(this, "set from dynamic")
            finish()

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //权限回调提示
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE) {
            var count = 0
            for (i in grantResults.indices) {
                if (permissions[i] == storagePermission && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    count += 1
                }
                if (permissions[i] == phonePermission && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    count += 2
                }
            }
            if (count > 0) {
                Toast.makeText(
                    this,
                    when (count) {
                        1 -> "你拒绝了存储权限"
                        2 -> "你拒绝了电话权限"
                        else -> "你拒绝了存储和电话权限"
                    },
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
