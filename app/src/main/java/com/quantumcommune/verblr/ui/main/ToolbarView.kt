package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible

data class ToolbarViewParams(
    val leading : View? = null,
    val title : View? = null,
    val trailing : View? = null
)

open class ToolbarView : ConstraintLayout {

    companion object {

        fun newInstance(context : Context?, params : ToolbarViewParams) : ToolbarView
        {
            val instance = ToolbarView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var leadingContainer: LinearLayout
    lateinit var titleContainer : LinearLayout
    lateinit var trailingContainer: LinearLayout

    fun init(params : ToolbarViewParams)
    {
        val margin = VRBTheme.gutter

        leadingContainer = LinearLayout(context)
        leadingContainer.id = View.generateViewId()
        leadingContainer.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        leadingContainer.gravity = Gravity.LEFT


        titleContainer = LinearLayout(context)
        titleContainer.id = View.generateViewId()
        titleContainer.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        )
        titleContainer.gravity = Gravity.CENTER

        trailingContainer = LinearLayout(context)
        trailingContainer.id = View.generateViewId()
        trailingContainer.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        trailingContainer.gravity = Gravity.RIGHT

        addView(leadingContainer)
        addView(titleContainer)
        addView(trailingContainer)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(leadingContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(leadingContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(leadingContainer.id, ConstraintSet.END, titleContainer.id, ConstraintSet.START)
        constraints.centerVertically(leadingContainer.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(leadingContainer.id, ConstraintSet.START, margin)
        constraints.setMargin(leadingContainer.id, ConstraintSet.END, margin)
        constraints.setMargin(leadingContainer.id, ConstraintSet.TOP, margin)
        constraints.setMargin(leadingContainer.id, ConstraintSet.BOTTOM, margin)

        constraints.connect(trailingContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(trailingContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(trailingContainer.id, ConstraintSet.START, titleContainer.id, ConstraintSet.END)
        constraints.centerVertically(trailingContainer.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(trailingContainer.id, ConstraintSet.START, margin)
        constraints.setMargin(trailingContainer.id, ConstraintSet.END, margin)
        constraints.setMargin(trailingContainer.id, ConstraintSet.TOP, margin)
        constraints.setMargin(trailingContainer.id, ConstraintSet.BOTTOM, margin)

        constraints.connect(titleContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(titleContainer.id, ConstraintSet.PARENT_ID)
        constraints.centerVertically(titleContainer.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(titleContainer.id, ConstraintSet.TOP, margin)
        constraints.setMargin(titleContainer.id, ConstraintSet.BOTTOM, margin)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_toolbarBG)
    }


    fun refresh(params : ToolbarViewParams) {
        leadingContainer.removeAllViews()
        if(params.leading != null)
        {
            leadingContainer.addView(params.leading)
            leadingContainer.isVisible = true
            leadingContainer.isGone = false

        }
        else
        {
            leadingContainer.isVisible = false
            leadingContainer.isGone = true
        }


        titleContainer.removeAllViews()
        if(params.title != null)
        {
            titleContainer.addView(params.title)
            titleContainer.isVisible = true
            titleContainer.isGone = false

        }
        else
        {
            titleContainer.isVisible = false
            titleContainer.isGone = true
        }


        trailingContainer.removeAllViews()
        if(params.trailing != null)
        {
            trailingContainer.addView(params.trailing)
            trailingContainer.isVisible = true
            trailingContainer.isGone = false

        }
        else
        {
            trailingContainer.isVisible = false
            trailingContainer.isGone = true
        }
    }
}
