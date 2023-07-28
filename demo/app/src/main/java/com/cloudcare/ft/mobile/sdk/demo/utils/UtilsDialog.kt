package com.cloudcare.ft.mobile.sdk.demo.utils


import android.app.Dialog
import android.content.Context
import com.cloudcare.ft.mobile.sdk.demo.R


object UtilsDialog {
    private var loadingDialog: Dialog? = null
    fun showLoadingDialog(context: Context) {
        loadingDialog = Dialog(context, R.style.CustomDialogTheme)
        loadingDialog!!.setContentView(R.layout.dialog_loading)
        loadingDialog!!.setCancelable(false) // Prevent dialog dismissal on outside touch or back press
        loadingDialog!!.show()
    }

    fun hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog!!.isShowing) {
            loadingDialog?.dismiss()
        }
    }

    fun showToast(){
        
    }
}