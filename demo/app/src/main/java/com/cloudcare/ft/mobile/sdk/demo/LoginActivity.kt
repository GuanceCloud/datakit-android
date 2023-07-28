package com.cloudcare.ft.mobile.sdk.demo

import android.content.Intent
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_PASSWORD
import com.cloudcare.ft.mobile.sdk.demo.data.DEFAULT_USER_NAME
import com.cloudcare.ft.mobile.sdk.demo.manager.AccountManager
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class LoginActivity : AppCompatActivity(), AccountManager.Callback {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        val usernameEt = findViewById<TextInputEditText>(R.id.login_username)
        val passwordEt = findViewById<TextInputEditText>(R.id.login_password)

        findViewById<Button>(R.id.login_btn).setOnClickListener {
            AccountManager.login(usernameEt.text.toString(), passwordEt.text.toString(), this)
        }
        supportActionBar?.hide()

        usernameEt.setText(DEFAULT_USER_NAME)
        passwordEt.setText(DEFAULT_PASSWORD)


        val editBtn = findViewById<TextView>(R.id.login_setting_btn)

        val spannableString = SpannableString(getString(R.string.edit_setting))

        val underlineSpan = UnderlineSpan()

        spannableString.setSpan(
            underlineSpan,
            0,
            spannableString.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        editBtn.text = spannableString

        editBtn.setOnClickListener {
            startActivity(Intent(this, SettingActivity::class.java))
        }
    }


    override fun success(success: Boolean) {
        if (success) {
            setResult(RESULT_OK)
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }


}