package com.cloudcare.ft.mobile.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.cloudcare.ft.mobile.sdk.demo.data.GC_SCHEME_URL
import com.cloudcare.ft.mobile.sdk.demo.http.HttpEngine
import com.cloudcare.ft.mobile.sdk.demo.manager.SettingConfigManager
import com.cloudcare.ft.mobile.sdk.demo.manager.SettingData
import com.cloudcare.ft.mobile.sdk.demo.utils.Utils
import com.cloudcare.ft.mobile.sdk.demo.utils.UtilsDialog
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.nio.charset.StandardCharsets


@DelicateCoroutinesApi
class SettingActivity : BaseActivity() {
    private var datakitAddressEt: TextInputEditText? = null
    private var demoAPIAddressEt: TextInputEditText? = null
    private var appIDEt: TextInputEditText? = null
    private var settingData: SettingData? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        setTitle(R.string.edit_setting)
        datakitAddressEt = findViewById(R.id.setting_datakit)
        demoAPIAddressEt = findViewById(R.id.setting_demo_api)
        appIDEt = findViewById(R.id.setting_app_id)

//        appIDEt?.setOnFocusChangeListener { _, _ ->
//            checkAppId()
//        }
//
//        datakitAddressEt?.setOnFocusChangeListener { _, _ ->
//            checkDatakitAddress()
//        }
//
//        demoAPIAddressEt?.setOnFocusChangeListener { _, _ ->
//            checkDemoAPIAddress()
//        }

        findViewById<Button>(R.id.setting_check).setOnClickListener {
            val data = SettingData(
                datakitAddressEt?.text.toString(),
                demoAPIAddressEt?.text.toString(),
                appIDEt?.text.toString()
            )
            checkAddressConnect(data) {
                Toast.makeText(
                    this@SettingActivity,
                    getString(R.string.setting_tip_connect_success),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        settingData = SettingConfigManager.readSetting()
        setSettingView(settingData!!)
    }

    private fun setSettingView(settingData: SettingData) {
        datakitAddressEt?.setText(settingData.datakitAddress)
        demoAPIAddressEt?.setText(settingData.demoApiAddress)
        appIDEt?.setText(settingData.appId)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.setting_menu, menu);
        return true
    }

    private fun checkAppId() {
        if (appIDEt?.text.isNullOrEmpty()) {
            appIDEt?.error = "app id 不为空"
        } else {
            appIDEt?.error = null
        }
    }

    private fun checkDatakitAddress() {
        if (!Utils.isValidHttpUrl(datakitAddressEt!!.text.toString())) {
            datakitAddressEt?.error = "非法地址"
        } else {
            datakitAddressEt?.error = null

        }
    }

    private fun checkDemoAPIAddress() {
        if (!Utils.isValidHttpUrl(demoAPIAddressEt!!.text.toString())) {
            demoAPIAddressEt?.error = "非法地址"
        } else {
            demoAPIAddressEt?.error = null
        }
    }

    private fun checkAllInput() {
        checkAppId()
        checkDatakitAddress()
        checkDemoAPIAddress()
    }


    @DelicateCoroutinesApi
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.setting_import_setting -> {
                val text = Utils.copyFormClipBoard(this)
                if (text.startsWith(GC_SCHEME_URL)) {
                    val data = text.substring(GC_SCHEME_URL.length)
                    val decodeByte = Base64.decode(data, Base64.DEFAULT)
                    val jsonString = String(decodeByte, StandardCharsets.UTF_8)
                    val settingData = SettingData.readFromJson(jsonString)
                    if (settingData != null) {
                        setSettingView(settingData)
                        this.settingData = settingData
                        Toast.makeText(
                            this,
                            getString(R.string.setting_import_success),
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {
                        Toast.makeText(
                            this,
                            getString(R.string.setting_import_fail),
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.setting_configure_not_found),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            R.id.setting_save -> {

                checkAllInput()
                if (!(appIDEt?.error.isNullOrEmpty()
                            && datakitAddressEt?.error.isNullOrEmpty()
                            && demoAPIAddressEt?.error.isNullOrEmpty())

                ) {
                    return true
                }

                val data = SettingData(
                    datakitAddressEt?.text.toString(),
                    demoAPIAddressEt?.text.toString(),
                    appIDEt?.text.toString()
                )

                checkAddressConnect(data) {
                    SettingConfigManager.saveSetting(data)

                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle(getString(R.string.tip))
                    builder.setMessage(getString(R.string.setting_tip_restart))

                    builder.setPositiveButton("OK") { _, _ ->
                        val intent = Intent(this@SettingActivity, MainActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)

                        DemoApplication.setSDK(this@SettingActivity)
                    }

                    val alertDialog: AlertDialog = builder.create()
                    alertDialog.show()
                }


            }

        }
        return super.onOptionsItemSelected(item)
    }

    private fun checkAddressConnect(settingData: SettingData, success: (() -> Unit)? = null) {
        UtilsDialog.showLoadingDialog(this@SettingActivity)
        GlobalScope.launch(Dispatchers.IO) {
            val datakitConnect = HttpEngine.datakitPing(settingData.datakitAddress)
            val apiConnect = HttpEngine.apiConnect(settingData.demoApiAddress)

            withContext(Dispatchers.Main) {
                if (datakitConnect.code != HttpURLConnection.HTTP_OK) {
                    datakitAddressEt?.error = datakitConnect.errorMessage
                }else{
                    datakitAddressEt?.error = null

                }

                if (apiConnect.code != HttpURLConnection.HTTP_OK) {
                    demoAPIAddressEt?.error = apiConnect.errorMessage
                }else{
                    demoAPIAddressEt?.error = null

                }
                UtilsDialog.hideLoadingDialog()

                if (!(demoAPIAddressEt?.error.isNullOrEmpty()
                            && datakitAddressEt?.error.isNullOrEmpty())
                ) {
                    return@withContext
                }


                success?.let {
                    it()
                }

            }
        }


    }
}