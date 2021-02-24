package com.quantumcommune.verblr.ui.main

import android.app.AlertDialog
import android.content.Context
import android.widget.Toast
import androidx.appcompat.view.ContextThemeWrapper
import com.quantumcommune.verblr.R
import com.quantumcommune.verblr.ThreadUtils

class Dialog(val context: Context) {

    companion object {
        // [dho] https://stackoverflow.com/a/7965192/300037
        val ToastDuration = 2000
    }

    fun alert(icon : Int? = null, title : String? = null, message : String, completion : (() -> Unit)? = null)
    {
        ThreadUtils.ensureMainThreadExec {
            val x = AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
                .setTitle(title ?: context.getString(R.string.alert_dialog_title))
                .setMessage(message)
                .setNeutralButton(R.string.alert_dialog_ok){ dialog, which ->
                    completion?.invoke()
                }
                .setIcon(icon ?: R.drawable.exclamationmark_circle_fill_contextualinfo)
                .show()
        }
    }

    fun confirm(title : String, message : String, completion : (result : Boolean) -> Unit)
    {
        ThreadUtils.ensureMainThreadExec {
            AlertDialog.Builder(ContextThemeWrapper(context, R.style.AlertDialogTheme))
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(R.string.alert_dialog_ok){ dialog, which ->
                    completion(true)
                }
                .setNeutralButton(R.string.alert_dialog_cancel) { dialog, which ->
                    completion(false)
                }
                .setIcon(R.drawable.exclamationmark_circle_fill_contextualinfo)
                .show()
        }
    }

    fun toast(message : String)
    {
        ThreadUtils.ensureMainThreadExec {
            // [dho] NOTE if changing duration, make sure we update the companion
            // field at top of class - 09/06/20
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }
    }

}