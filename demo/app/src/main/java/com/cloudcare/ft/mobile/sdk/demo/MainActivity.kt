package com.cloudcare.ft.mobile.sdk.demo

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.cloudcare.ft.mobile.sdk.demo.adapter.ViewPagerAdapter
import com.cloudcare.ft.mobile.sdk.demo.fragment.HomeFragment
import com.cloudcare.ft.mobile.sdk.demo.fragment.MineFragment
import com.cloudcare.ft.mobile.sdk.demo.manager.AccountManager
import com.ft.sdk.FTLogger
import com.ft.sdk.garble.bean.Status
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.DelicateCoroutinesApi


@DelicateCoroutinesApi
class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setUpView()
            }
        }
    private val phonePermission = Manifest.permission.READ_PHONE_STATE
    private var requestPermissions = arrayOf<String>()
    private val REQUEST_CODE = 0x001

    private var viewPager: ViewPager2? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (AccountManager.checkLogin()) {
            setUpView()
            FTLogger.getInstance().logBackground("Account Exists", Status.INFO)
        } else {
            goToLogin()
        }
    }

    fun goToLogin() {
        resultLauncher.launch(Intent(this@MainActivity, LoginActivity::class.java))
    }

    private fun setUpView() {
        title = getString(R.string.main_index_home)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener(this)

        viewPager = findViewById(R.id.viewPager)

        val fragments: MutableList<Fragment> = ArrayList()
        fragments.add(HomeFragment())
        fragments.add(MineFragment())

        val adapter = ViewPagerAdapter(this, fragments)
        viewPager?.adapter = adapter
        viewPager?.registerOnPageChangeCallback(object : OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        title = getString(R.string.main_index_home)
                        bottomNavigationView.selectedItemId = R.id.navigation_home
                    }

                    1 -> {
                        title = getString(R.string.main_index_mine)
                        bottomNavigationView.selectedItemId = R.id.navigation_profile
                    }

                }

            }

        })

        //请求权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(phonePermission) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions = requestPermissions.plus(phonePermission)
            }
            if (requestPermissions.isNotEmpty()) {
                requestPermissions(requestPermissions, REQUEST_CODE)
            }
        }

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                viewPager?.currentItem = 0
                return true

            }

            R.id.navigation_profile -> {
                viewPager?.currentItem = 1
                return true
            }
        }
        return false
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