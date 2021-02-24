package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.util.TypedValue
import android.view.View
import java.util.*


data class TextInputViewParams(
    val type : Int? = null,
    val label : String,
    val initialValue : String? = null,
    val onChange : ((String) -> Unit)? = null,
    val disabled : Boolean = false
)

class TextInputView : androidx.appcompat.widget.AppCompatEditText {

    companion object {
        // [dho] https://developer.android.com/reference/android/text/InputType - 23/06/20
        val defaultType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS

        val COLOR_text = VRBTheme.COLOR_fontBody
        val COLOR_disabledText = VRBTheme.COLOR_fontDisabled
        val COLOR_hint = VRBTheme.COLOR_brandElectro

        fun newInstance(context : Context?, params : TextInputViewParams) : TextInputView
        {
            val instance = TextInputView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private var lastOnChangeHandler : ((String) -> Unit)? = null


    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    fun init(params : TextInputViewParams)
    {
        isAllCaps = false

        setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        typeface = VRBTheme.TYPEFACE_regular

        setLines(1)
        setHintTextColor(COLOR_hint)
        // [dho] remove underbar - 20/06/20
        setBackgroundResource(android.R.color.transparent)
        // [dho] adapted from : https://stackoverflow.com/a/59573021/300037 - 20/06/20
        error = null

        if(params.initialValue != null)
        {
            setText(params.initialValue)
        }
    }

    fun refresh(params : TextInputViewParams) {
        inputType = params.type ?: defaultType

        hint = params.label.toLowerCase(Locale.ROOT)
        // [dho] adapted from : https://stackoverflow.com/a/59573021/300037 - 20/06/20
        error = null

        isEnabled = !params.disabled

        if(isEnabled)
        {
            setTextColor(COLOR_text)
        }
        else
        {
            setTextColor(COLOR_disabledText)
        }

        val onChangeHandler = params.onChange

        if(onChangeHandler != lastOnChangeHandler)
        {
            afterTextChanged(onChangeHandler ?: {})
            lastOnChangeHandler = params.onChange
        }
    }
}