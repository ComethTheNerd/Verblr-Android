package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout

data class ProgressBarViewParams(
    val progressScalar : Float,
    val progressTint : Int,
    val progressBackgroundTint : Int
);

class ProgressBarView : LinearLayout
{
    companion object {
        fun newInstance(context : Context?, params : ProgressBarViewParams) : ProgressBarView
        {
            val instance = ProgressBarView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }


    lateinit var progress : LinearLayout

    fun init(params : ProgressBarViewParams)
    {
        orientation = LinearLayout.HORIZONTAL
        weightSum = 1.0f
        gravity = Gravity.START

        progress = LinearLayout(context)

        addView(progress)
    }

    fun refresh(params : ProgressBarViewParams) {
        setBackgroundColor(params.progressBackgroundTint)
        progress.setBackgroundColor(params.progressTint)
        progress.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT,
            params.progressScalar
        )

    }
}