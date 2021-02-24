package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class StandardScreenViewParams(
    val toolbar : ToolbarViewParams,
    val content : View,
    val statusBar : StatusBarViewParams,
    val miniPlayer : MiniPlayerViewParams
);

class StandardScreenView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : StandardScreenViewParams) : StandardScreenView
        {
            val instance = StandardScreenView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var toolbar : ToolbarView
    lateinit var contentContainer : LinearLayout
    lateinit var statusBar : StatusBarView
    lateinit var miniPlayer : MiniPlayerView

    fun init(params : StandardScreenViewParams)
    {
        toolbar = ToolbarView.newInstance(context, params.toolbar)
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

        statusBar = StatusBarView.newInstance(context, params.statusBar)
        statusBar.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        )

        miniPlayer = MiniPlayerView.newInstance(context, params.miniPlayer)
        miniPlayer.layoutParams = ConstraintLayout.LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

        addView(toolbar)
        addView(contentContainer)
        addView(statusBar)
        addView(miniPlayer)

        val constraints = ConstraintSet()
        constraints.clone(this)


        constraints.connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(contentContainer.id, ConstraintSet.TOP, toolbar.id, ConstraintSet.BOTTOM)
        constraints.connect(contentContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(contentContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(contentContainer.id, ConstraintSet.BOTTOM, statusBar.id, ConstraintSet.TOP)
//        constraints.constrainDefaultHeight(feed.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)

//        constraints.connect(statusBar.id, ConstraintSet.TOP, upsellFooter.id, ConstraintSet.BOTTOM)
        constraints.connect(statusBar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(statusBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(statusBar.id, ConstraintSet.BOTTOM, miniPlayer.id, ConstraintSet.TOP)


//        constraints.connect(miniPlayerView.id, ConstraintSet.TOP, statusBar.id, ConstraintSet.BOTTOM)
        constraints.connect(miniPlayer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(miniPlayer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(miniPlayer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        constraints.applyTo(this);
    }

    fun refresh(params : StandardScreenViewParams)
    {
        toolbar.refresh(params.toolbar)

        contentContainer.removeAllViews()
        contentContainer.addView(params.content)

        statusBar.refresh(params.statusBar)
        miniPlayer.refresh(params.miniPlayer)
    }
}