package com.cloudcare.ft.mobile.sdk.demo

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.ft.sdk.FTTrack
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class MainActivity : AppCompatActivity() {
    private val storagePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
    private val phonePermission = Manifest.permission.READ_PHONE_STATE
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
            if(requestPermissions.isNotEmpty()) {
                requestPermissions(requestPermissions, REQUEST_CODE)
            }
        }
        try_btn.setOnClickListener {
            /*****************写入数据关键方法*******************/
            try {
                val tags = JSONObject()
                val values = JSONObject()
                tags.put("user", "jack")
                tags.put("do", "test")
                values.put("click", "try_btn")
                FTTrack.getInstance().track("sdk_demo", tags, values)
            } catch (e: JSONException) {
                e.printStackTrace()
            }

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //权限回调提示
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE){
            var count = 0
            for(i in grantResults.indices){
                if(permissions[i] == storagePermission && grantResults[i] == PackageManager.PERMISSION_DENIED){
                    count += 1
                }
                if(permissions[i] == phonePermission && grantResults[i] == PackageManager.PERMISSION_DENIED){
                    count += 2
                }
            }
            if(count > 0) {
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
