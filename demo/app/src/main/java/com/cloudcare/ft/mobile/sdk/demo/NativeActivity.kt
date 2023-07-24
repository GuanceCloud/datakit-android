package com.cloudcare.ft.mobile.sdk.demo

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import com.ft.sdk.FTRUMConfig
import com.ft.sdk.FTSdk
import com.ft.sdk.FTTraceConfig

class NativeActivity : BaseActivity() {
    private val phonePermission = Manifest.permission.READ_PHONE_STATE
    private var requestPermissions = arrayOf<String>()
    private val REQUEST_CODE = 0x001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_native_view)

        //请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(phonePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(phonePermission)
            }
            if (requestPermissions.isNotEmpty()) {
                requestPermissions(requestPermissions, REQUEST_CODE)
            }
        }

        findViewById<Button>(R.id.dynamic_rum_tag_btn).setOnClickListener {
            DemoApplication.setDynamicParams(this, "set from dynamic")
            finish()

        }

        findViewById<Button>(R.id.manual_data_btn).setOnClickListener {
            FTSdk.initTraceWithConfig(
                FTTraceConfig()
                    .setEnableLinkRUMData(true)
            )

            FTSdk.initRUMWithConfig(
                FTRUMConfig()
                    .setRumAppId(BuildConfig.RUM_APP_ID)
                    .setEnableTrackAppCrash(true)
                    .setEnableTrackAppANR(true)
            )
            startActivity(Intent(this, ManualActivity::class.java))
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
                if (permissions[i] == phonePermission && grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    count += 1
                }
            }
            if (count > 0) {
                Toast.makeText(
                    this,
                    "你拒绝了电话权限",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
