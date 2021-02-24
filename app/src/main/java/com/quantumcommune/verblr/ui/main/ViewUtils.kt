package com.quantumcommune.verblr.ui.main

import android.content.res.Resources
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText


// [dho] adapted from https://stackoverflow.com/a/40569759/300037 - 24/05/20
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}




class ViewUtils {

    private constructor()
    {

    }

    companion object {
        // [dho] the screen size the app was developed against - 30/06/20
        val referenceWidthPX = 1080F
        val referenceHeightPX = 1794F

        val density = Resources.getSystem().displayMetrics.density ?: 1.0f

        // [dho] the actual screen size of the device - 30/06/20
        val screenWidthPX = Resources.getSystem().displayMetrics.widthPixels
        val screenHeightPX = Resources.getSystem().displayMetrics.heightPixels

        // [dho] scalars to apply to dimensions in order to fit the screen based on
        // the reference size - 30/06/20
        // [dho] NOTE capping it at 1.0F because otherwise font is too big...
        // TODO treat font scaling separate from layout scaling - 30/06/20
        val scalarX = if(screenWidthPX > referenceWidthPX) 1.0F/*screenWidthPX / referenceWidthPX*/ else referenceWidthPX / screenWidthPX
        val scalarY = if(screenHeightPX > referenceHeightPX) 1.0F/*screenHeightPX / referenceHeightPX*/ else referenceHeightPX / screenHeightPX

        // [dho] adapted from : https://stackoverflow.com/a/5255256/300037 - 21/05/20
        fun toPX(dps : Float) : Int = (dps * density + 0.5f).toInt()
        fun toXScaledPX(dps : Float) : Int = (toPX(dps) * scalarX + 0.5f).toInt()
        fun toYScaledPX(dps : Float) : Int = (toPX(dps) * scalarY  + 0.5f).toInt()

//        fun toFontScaledPX(dps : Int) : Int = toXScaledPX(dps)

        // [dho] adapted from : https://stackoverflow.com/a/56793843/300037 - 15/06/20
//        fun replaceView(existing: View, replacement: View) {
//            val parentView = existing.getParent() as ViewGroup ?: return
//            val idx = parentView.indexOfChild(existing)
//            parentView.removeViewAt(idx)
//            parentView.addView(replacement, idx)
//        }
    }
}