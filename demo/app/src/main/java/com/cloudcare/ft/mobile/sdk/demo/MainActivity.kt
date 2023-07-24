package com.cloudcare.ft.mobile.sdk.demo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudcare.ft.mobile.sdk.demo.adapter.SimpleAdapter
import com.cloudcare.ft.mobile.sdk.demo.manager.AccountManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import kotlinx.coroutines.DelicateCoroutinesApi


class MainActivity : AppCompatActivity(), SimpleAdapter.OnItemClickListener,
    NavigationBarView.OnItemSelectedListener {

    private val dataList = listOf("Native View", "WebView")

    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                setUpView()
            }
        }

    @OptIn(DelicateCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        if (AccountManager.checkLogin()) {
            setUpView()
        } else {
            resultLauncher.launch(Intent(this@MainActivity, LoginActivity::class.java))
        }


    }

    private fun setUpView() {
        title = getString(R.string.main_index_home)
        setContentView(R.layout.activity_main)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = SimpleAdapter(dataList, this)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        bottomNavigationView.setOnItemSelectedListener(this)

    }

    override fun onItemClick(data: String) {
        when (data) {
            "Native View" -> {
                startActivity(Intent(this@MainActivity, NativeActivity::class.java))

            }

            "WebView" -> {
                startActivity(Intent(this@MainActivity, WebViewActivity::class.java))

            }

        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.navigation_home -> {
                title = getString(R.string.main_index_home)
                return true

            }

            R.id.navigation_profile -> {
                title = getString(R.string.main_index_mine)

                return true
            }
        }
        return false
    }


}