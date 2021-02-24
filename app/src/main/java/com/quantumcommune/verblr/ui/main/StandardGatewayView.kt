package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class StandardGatewayViewParams(
    val toolbar : ToolbarViewParams,
    val header : HeaderViewParams,
    val content : View
);

class StandardGatewayView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : StandardGatewayViewParams) : StandardGatewayView
        {
            val instance = StandardGatewayView(context)
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
    lateinit var header : HeaderView
    lateinit var contentContainer : LinearLayout

    fun init(params : StandardGatewayViewParams)
    {
        toolbar = ToolbarView.newInstance(context, params.toolbar)
        toolbar.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

        header = HeaderView.newInstance(context, params.header)
        header.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        contentContainer = LinearLayout(context)
        contentContainer.id = View.generateViewId()
        contentContainer.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_CONSTRAINT
        )

        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_toolbarAccent
        ))
        divider.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )


        addView(toolbar)
        addView(divider)
        addView(header)
        addView(contentContainer)

        val constraints = ConstraintSet()
        constraints.clone(this)


        constraints.connect(toolbar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(toolbar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(toolbar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(divider.id, ConstraintSet.TOP, toolbar.id, ConstraintSet.BOTTOM)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.setMargin(divider.id, ConstraintSet.START, VRBTheme.gutter)
//        constraints.setMargin(divider.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(header.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(header.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(header.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(contentContainer.id, ConstraintSet.TOP, header.id, ConstraintSet.BOTTOM)
        constraints.connect(contentContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(contentContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(contentContainer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_contentBG)
    }

    fun refresh(params : StandardGatewayViewParams)
    {
        toolbar.refresh(params.toolbar)

        header.refresh(params.header)

        contentContainer.removeAllViews()
        contentContainer.addView(params.content)
    }
}