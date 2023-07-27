package com.cloudcare.ft.mobile.sdk.demo.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cloudcare.ft.mobile.sdk.demo.NativeActivity
import com.cloudcare.ft.mobile.sdk.demo.R
import com.cloudcare.ft.mobile.sdk.demo.WebViewActivity
import com.cloudcare.ft.mobile.sdk.demo.adapter.SimpleAdapter

class HomeFragment : Fragment(), SimpleAdapter.OnItemClickListener {

    private val dataList = listOf("Native View", "WebView")

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val rootView = inflater.inflate(
            R.layout.fragment_item_index, container, false
        )
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = SimpleAdapter(dataList, this)
        return rootView
    }

    override fun onItemClick(data: String) {
        when (data) {
            "Native View" -> {
                startActivity(Intent(context, NativeActivity::class.java))

            }

            "WebView" -> {
                startActivity(Intent(context, WebViewActivity::class.java))

            }

        }
    }
}