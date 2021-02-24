package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View


data class LinkViewParams(
    val label : String,
    val fontSize : Float = VRBTheme.m15Size,
    val action : (() -> Unit)? = null,
    val disabled : Boolean = false
)

class LinkView : androidx.appcompat.widget.AppCompatTextView {

    companion object {

        val COLOR_text = VRBTheme.COLOR_brandElectro
        val COLOR_disabledText = VRBTheme.COLOR_fontDisabled

        fun newInstance(context : Context?, params : LinkViewParams) : LinkView
        {
            val instance = LinkView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    fun init(params : LinkViewParams)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, params.fontSize)
        typeface = VRBTheme.TYPEFACE_semibold
        ellipsize = TextUtils.TruncateAt.END
        isAllCaps = false
        setSingleLine()
        // [dho] adapted from : https://stackoverflow.com/a/56445723 - 22/06/20
        firstBaselineToTopHeight = 0
        includeFontPadding = false
        setLineSpacing(0f,0f);
        setPadding(0, 0, 0, 0)
//        setBackgroundColor(Color.parseColor("#FF0000"))
    }


    fun refresh(params : LinkViewParams) {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, params.fontSize)
        text = params.label


        val action = params.action
        val isEnabled = action != null && !params.disabled

        if(isEnabled)
        {
            setTextColor(COLOR_text)
            setOnClickListener { action!!() }
        }
        else
        {
            setTextColor(COLOR_disabledText)
            setOnClickListener { }
        }
    }
}