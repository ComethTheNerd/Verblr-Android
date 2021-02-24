package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible

data class AdWrapperViewParams(
    val content : View,
    val adCard : AdCardView?
);

class AdWrapperView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : AdWrapperViewParams) : AdWrapperView
        {
            val instance = AdWrapperView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var contentContainer : LinearLayout
    lateinit var adContainer : LinearLayout
    lateinit var divider : HorizontalDividerView

    fun init(params : AdWrapperViewParams)
    {
        contentContainer = LinearLayout(context)
        contentContainer.id = View.generateViewId()
        contentContainer.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_CONSTRAINT_SPREAD
        )

        divider = HorizontalDividerView.newInstance(context,
            HorizontalDividerViewParams(color = VRBTheme.COLOR_toolbarAccent)
        )
        divider.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

        adContainer = LinearLayout(context)
        adContainer.id = View.generateViewId()
        adContainer.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        addView(contentContainer)
        addView(divider)
        addView(adContainer)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(contentContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(contentContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(contentContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(contentContainer.id, ConstraintSet.BOTTOM, divider.id, ConstraintSet.TOP)

        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(divider.id, ConstraintSet.BOTTOM, adContainer.id, ConstraintSet.TOP)
        constraints.setMargin(divider.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.connect(adContainer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(adContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(adContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        constraints.applyTo(this);
    }

    fun refresh(params : AdWrapperViewParams)
    {
        contentContainer.removeAllViews()
        contentContainer.addView(params.content)

        adContainer.removeAllViews()
        val adCard = params.adCard
        if(adCard != null)
        {
            divider.isVisible = true
            divider.isGone = false
            adContainer.addView(adCard)
        }
        else
        {
            divider.isVisible = false
            divider.isGone = true
        }
    }
}