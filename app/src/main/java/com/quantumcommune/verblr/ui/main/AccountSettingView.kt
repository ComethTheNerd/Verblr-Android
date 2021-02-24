package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.quantumcommune.verblr.R

data class AccountSettingViewParams(
    val icon : Int,
    val label : String,
    val disabled : Boolean = false,
    val action : () -> Unit
)

class AccountSettingView : ConstraintLayout {

    companion object {

        val COLOR_text = VRBTheme.COLOR_fontBody
        val COLOR_disabledText = VRBTheme.COLOR_fontDisabled

        fun newInstance(context : Context?, params : AccountSettingViewParams) : AccountSettingView
        {
            val instance = AccountSettingView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }



    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var icon: ImageView
    lateinit var label : TextView
    lateinit var chevron: ImageView

    val iconDim = ViewUtils.toPX(24F)
    val chevronDim = ViewUtils.toPX(18F)

    fun init(params : AccountSettingViewParams)
    {
        icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = ConstraintLayout.LayoutParams(
            iconDim,
            iconDim
        )

        label = TextView(context)
        label.id = View.generateViewId()
        label.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        label.setLines(1)
        label.ellipsize = TextUtils.TruncateAt.END
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        label.setTextColor(VRBTheme.COLOR_fontBody)

        chevron = ImageView(context)
        chevron.id = View.generateViewId()
        chevron.layoutParams = ConstraintLayout.LayoutParams(
            chevronDim,
            chevronDim
        )
        chevron.setImageResource(R.drawable.chevron_right_subwhite)

        addView(icon)
        addView(label)
        addView(chevron)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(icon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(icon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(icon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(icon.id, ConstraintSet.END, label.id, ConstraintSet.START)

//        constraints.connect(label.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//        constraints.connect(label.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(label.id, ConstraintSet.START, icon.id, ConstraintSet.END)
        constraints.connect(label.id, ConstraintSet.END, chevron.id, ConstraintSet.START)
        constraints.centerVertically(label.id, ConstraintSet.PARENT_ID)
//        constraints.constrainDefaultWidth(label.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
        constraints.setMargin(label.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(label.id, ConstraintSet.END, VRBTheme.gutter)

//        constraints.connect(chevron.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(chevron.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.connect(chevron.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(chevron.id, ConstraintSet.START, label.id, ConstraintSet.END)
        constraints.centerVertically(chevron.id, ConstraintSet.PARENT_ID)

        constraints.applyTo(this);
    }


    fun refresh(params : AccountSettingViewParams) {
        icon.setImageResource(params.icon)

        label.text = params.label

        if(params.disabled)
        {
            label.setTextColor(COLOR_disabledText)
            setOnClickListener { }
        }
        else
        {
            label.setTextColor(COLOR_text)
            setOnClickListener { params.action() }
        }
    }
}
