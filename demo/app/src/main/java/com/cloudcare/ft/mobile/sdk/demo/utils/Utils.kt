package com.cloudcare.ft.mobile.sdk.demo.utils

import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Patterns


class Utils {


    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val connectivityManager =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

            return when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        }

        fun isValidHttpUrl(url: String): Boolean {
            return Patterns.WEB_URL.matcher(url).matches()
        }

        fun copyFormClipBoard(context: Context): String {
            val clipboard: ClipboardManager? =
                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager?
            if (clipboard!!.hasPrimaryClip() && clipboard.primaryClipDescription!!.hasMimeType(
                    ClipDescription.MIMETYPE_TEXT_PLAIN
                )
            ) {
                val item = clipboard.primaryClip!!.getItemAt(0)
                return item.text.toString()
            }

            return "";

        }
    }


}