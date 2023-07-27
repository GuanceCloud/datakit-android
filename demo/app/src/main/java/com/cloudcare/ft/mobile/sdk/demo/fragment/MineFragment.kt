package com.cloudcare.ft.mobile.sdk.demo.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.cloudcare.ft.mobile.sdk.demo.MainActivity
import com.cloudcare.ft.mobile.sdk.demo.R
import com.cloudcare.ft.mobile.sdk.demo.SettingActivity
import com.cloudcare.ft.mobile.sdk.demo.manager.AccountManager
import com.ft.sdk.FTLogger
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MineFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_item_mine, container, false)
        setUpView(view)
        return view

    }

    private fun setUpView(view: View) {
        view.findViewById<Button>(R.id.mine_edit_setting).setOnClickListener {
            startActivity(Intent(context, SettingActivity::class.java))
        }

        bindUserView(view)

        view.findViewById<Button>(R.id.mine_logout).setOnClickListener {
            AccountManager.logout()
            (context as MainActivity).goToLogin()
        }

        val refreshView: SwipeRefreshLayout = view.findViewById(R.id.mine_refresh_layout)
        refreshView.setOnRefreshListener {
            AccountManager.getUserInfo {

                bindUserView(view)

                refreshView.isRefreshing = false

            }
        }
    }

    private fun bindUserView(view: View) {
        val userdata = AccountManager.userData

        val avatarIv = view.findViewById<ImageView>(R.id.mine_avatar)
        Picasso.get()
            .load(userdata?.avatar)
//            .placeholder(R.drawable.placeholder) // Optional placeholder image while loading
//            .error(R.drawable.error_image) // Optional error image if the loading fails
            .into(avatarIv)
        val userNameTv = view.findViewById<TextView>(R.id.mine_username)
        userNameTv.text = userdata?.username
        val emailTv = view.findViewById<TextView>(R.id.mine_email)
        emailTv.text = userdata?.email
    }


}