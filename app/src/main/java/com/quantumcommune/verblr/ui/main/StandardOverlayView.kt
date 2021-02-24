package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class StandardOverlayViewParams(
    val toolbarParams : ToolbarViewParams,
    val content : View
);

open class StandardOverlayView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : StandardOverlayViewParams) : StandardOverlayView
        {
            val instance = StandardOverlayView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    protected constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var toolbar : ToolbarView
    lateinit var contentContainer : LinearLayout

    fun init(params : StandardOverlayViewParams)
    {
        toolbar = ToolbarView.newInstance(context, params.toolbarParams)
        toolbar.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

        contentContainer = LinearLayout(context)
        contentContainer.id = View.generateViewId()
        contentContainer.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_CONSTRAINT
        )

        addView(toolbar)
        addView(contentContainer)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(contentContainer.id, ConstraintSet.TOP, toolbar.id, ConstraintSet.BOTTOM)
        constraints.connect(contentContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(contentContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(contentContainer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//        constraints.constrainDefaultHeight(feed.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_toolbarBG)
    }

    fun refresh(params : StandardOverlayViewParams)
    {
        toolbar.refresh(params.toolbarParams)

        contentContainer.removeAllViews()
        contentContainer.addView(params.content)
    }
}