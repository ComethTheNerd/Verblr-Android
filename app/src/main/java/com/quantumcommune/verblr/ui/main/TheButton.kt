package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import java.util.*


data class TheButtonParams(
    val label : String,
    val disabled : Boolean = false,
    val action : (() -> Unit)? = null
)

class TheButton : androidx.appcompat.widget.AppCompatTextView {

    companion object {

        val COLOR_text = VRBTheme.COLOR_toolbarAccent
        val COLOR_disabledText = VRBTheme.COLOR_fontDisabled
        val COLOR_bg = VRBTheme.COLOR_brandElectro
        val COLOR_disabledBG = VRBTheme.COLOR_disabled

        fun newInstance(context : Context?, params : TheButtonParams) : TheButton
        {
            val instance = TheButton(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    fun init(params : TheButtonParams)
    {
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.buttonFontSize)
        setLines(1)
//        minWidth = VRBTheme.minArticleDetailArtworkDim
        setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.buttonFontSize)
        typeface = VRBTheme.TYPEFACE_semibold
        ellipsize = TextUtils.TruncateAt.END
        isAllCaps = false
        setSingleLine()
        // [dho] adapted from : https://stackoverflow.com/a/56445723 - 22/06/20
        firstBaselineToTopHeight = 0
        includeFontPadding = false
        setLineSpacing(0f,0f);
        textAlignment = View.TEXT_ALIGNMENT_CENTER
        setPadding(VRBTheme.gutter, VRBTheme.smallGutter, VRBTheme.gutter, VRBTheme.smallGutter)
    }

    fun refresh(params : TheButtonParams) {
        text = params.label.toLowerCase(Locale.ROOT)

        val action = params.action
        val isEnabled = action != null && !params.disabled

        if(isEnabled)
        {
            setBackgroundColor(COLOR_bg)
            setTextColor(COLOR_text)
            setOnClickListener { action!!() }
        }
        else
        {
            setBackgroundColor(COLOR_disabledBG)
            setTextColor(COLOR_disabledText)
            setOnClickListener { }
        }
    }

    //        detailsButton.text = "Details"
//    cta.setTextColor(VRBTheme.COLOR_brandElectro)

}