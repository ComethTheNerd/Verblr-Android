package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.LinearLayout


data class HorizontalDividerViewParams(
    val color : Int
)

class HorizontalDividerView : LinearLayout {

    companion object {
        val DividerHeight = ViewUtils.toYScaledPX(1F)

        fun newInstance(context : Context?, params : HorizontalDividerViewParams) : HorizontalDividerView
        {
            val instance = HorizontalDividerView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var divider : View

    fun init(params : HorizontalDividerViewParams)
    {
        divider = View(context).apply {
            this.id = View.generateViewId()
            this.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                DividerHeight
            )
            this.minimumHeight = DividerHeight
        }

        addView(divider)
    }

    fun refresh(params : HorizontalDividerViewParams) {
        divider.setBackgroundColor(params.color)
    }
}