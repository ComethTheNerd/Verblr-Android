package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class ContextualIconTextViewParams(
    val icon : Int,
    val label : String,
    val textColor : Int = VRBTheme.COLOR_contextualInfo
);

class ContextualIconTextView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : ContextualIconTextViewParams) : ContextualIconTextView
        {
            val instance = ContextualIconTextView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var icon : ImageView
    lateinit var label : TextView

    fun init(params : ContextualIconTextViewParams)
    {
        icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.contextualIconDim,
            VRBTheme.contextualIconDim
        )

        label = TextView(context)
        label.id = View.generateViewId()
        label.maxLines = 1
        label.ellipsize = TextUtils.TruncateAt.END
        label.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        label.typeface = VRBTheme.TYPEFACE_semibold


        addView(icon)
        addView(label)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(icon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(icon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        constraints.connect(label.id, ConstraintSet.START, icon.id, ConstraintSet.END)
        constraints.centerVertically(label.id, icon.id)
        constraints.setMargin(label.id, ConstraintSet.START, VRBTheme.smallGutter / 2)

        constraints.applyTo(this);
    }

    fun refresh(params : ContextualIconTextViewParams)
    {
        icon.setImageResource(params.icon)
        label.text = params.label
        label.setTextColor(params.textColor)
    }
}