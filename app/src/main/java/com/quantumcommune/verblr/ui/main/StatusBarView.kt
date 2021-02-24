package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible

data class StatusBarViewParams(
    val icon : Int,
    val label : String,
    val action : (() -> Unit)?,
    val isHidden : Boolean = false
)

class StatusBarView : ConstraintLayout {

    companion object {

        fun newInstance(context : Context?, params : StatusBarViewParams) : StatusBarView
        {
            val instance = StatusBarView(context)
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

    val iconDim = ViewUtils.toPX(16F)

    fun init(params : StatusBarViewParams)
    {
        val marginHorizontal = VRBTheme.gutter / 2
        val marginVertical = VRBTheme.smallGutter * 2

        val topBorder = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_contentAccent
        ))
        topBorder.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

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
        label.setTextColor(VRBTheme.COLOR_toolbarCTA)
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m14Size)
        label.typeface = VRBTheme.TYPEFACE_semibold


        addView(topBorder)
        addView(icon)
        addView(label)

        val constraints = ConstraintSet()
        constraints.clone(this)


        constraints.connect(topBorder.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(topBorder.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(topBorder.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        constraints.connect(icon.id, ConstraintSet.TOP, topBorder.id, ConstraintSet.BOTTOM)
        constraints.connect(icon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(icon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(icon.id, ConstraintSet.END, label.id, ConstraintSet.START)
        constraints.setMargin(icon.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(icon.id, ConstraintSet.TOP, marginVertical)
        constraints.setMargin(icon.id, ConstraintSet.BOTTOM, marginVertical)


        constraints.connect(label.id, ConstraintSet.START, icon.id, ConstraintSet.END)
        constraints.connect(label.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.centerVertically(label.id, icon.id)
        constraints.setMargin(label.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(label.id, ConstraintSet.END, marginHorizontal)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_toolbarBG)
    }


    fun refresh(params : StatusBarViewParams) {
        icon.setImageResource(params.icon)

        label.text = params.label

        val action = params.action

        if(action != null)
        {
            setOnClickListener { action() }
        }
        else
        {
            setOnClickListener { }
        }

        if(params.isHidden)
        {
            // [dho] hide - 24/06/20
            isVisible = false
            isGone = true
        }
        else
        {
            // [dho] show - 24/06/20
            isGone = false
            isVisible = true
        }
    }
}
