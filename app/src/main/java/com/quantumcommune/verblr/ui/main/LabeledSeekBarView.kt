package com.quantumcommune.verblr.ui.main

import android.R
import android.content.Context
import android.content.res.ColorStateList
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet


typealias LabeledSeekBarViewOnSeekHandler = (scalar : Float, userDriven : Boolean) -> Unit

data class LabeledSeekBarViewParams(
    val fromLabel : String,
    val toLabel : String,
    // [dho] DISABLED API Level 26, we'll just use default 0 - 100 and provide scalar in handler - 19/06/20
//    val min : Int = 0,
    // [dho] DISABLED API Level 26, we'll just use default 0 - 100 and provide scalar in handler - 19/06/20
//    val max : Int = 100
    val value : Float,
    val onSeek : LabeledSeekBarViewOnSeekHandler,
//    val progressTint : Int,
//    val progressBackgroundTint : Int,
    val onScrubStart : () -> Unit,
    val onScrubEnd : () -> Unit,
    val disabled : Boolean = false
);

class LabeledSeekBarView : ConstraintLayout {
    companion object {

        val COLOR_seekBarThumb = VRBTheme.COLOR_progressBarCompleted
        val COLOR_seekBarProgressCompleted = VRBTheme.COLOR_progressBarCompleted
        val COLOR_seekBarProgressRemaining = VRBTheme.COLOR_progressBarRemaining
        val COLOR_disabledSeekBarThumb = VRBTheme.COLOR_fontDisabled
        val COLOR_disabledSeekBarProgress = VRBTheme.COLOR_disabled

        fun newInstance(
            context: Context?,
            params: LabeledSeekBarViewParams
        ): LabeledSeekBarView {
            val instance = LabeledSeekBarView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context: Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var fromLabel: TextView
    lateinit var toLabel: TextView
    lateinit var seekBar: SeekBar

    private var lastParams : LabeledSeekBarViewParams? = null
    private var isScrubbing : Boolean = false

    fun init(params: LabeledSeekBarViewParams) {
        seekBar = SeekBar(context)
        seekBar.id = View.generateViewId()
        seekBar.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        // [dho] adapted from : https://stackoverflow.com/a/17788095/300037 - 23/06/20
        val states = arrayOf(
            intArrayOf(R.attr.state_enabled), // enabled
            intArrayOf(-R.attr.state_enabled) // disabled
        )

        val thumbTintListColors = intArrayOf(
            COLOR_seekBarThumb,
            COLOR_disabledSeekBarThumb
        )

        val progressTintListColors = intArrayOf(
            COLOR_seekBarProgressCompleted,
            COLOR_disabledSeekBarProgress
        )

        val progressBackgroundTintListColors = intArrayOf(
            COLOR_seekBarProgressRemaining,
            COLOR_disabledSeekBarProgress
        )

        seekBar.thumbOffset = 0
//        inset = seekBar.thumbOffset

        seekBar.thumbTintList = ColorStateList(states, thumbTintListColors)
        seekBar.progressTintList = ColorStateList(states, progressTintListColors)
        seekBar.progressBackgroundTintList = ColorStateList(states, progressBackgroundTintListColors)
        // [dho] so the thumb is not cropped off at the start and end - 23/06/20
//        seekBar.setPadding(inset, 0, inset, 0)

        fromLabel = TextView(context)
        fromLabel.id = View.generateViewId()
        fromLabel.setLines(1)
        fromLabel.ellipsize = TextUtils.TruncateAt.END
        fromLabel.setTextColor(VRBTheme.COLOR_contextualInfo)
        fromLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerProgressLabelFontSize)
        fromLabel.typeface = VRBTheme.TYPEFACE_semibold
//        fromLabel.setPadding(inset, 0,0,0)

        toLabel = TextView(context)
        toLabel.id = View.generateViewId()
        toLabel.setLines(1)
        toLabel.ellipsize = TextUtils.TruncateAt.END
        toLabel.setTextColor(VRBTheme.COLOR_contextualInfo)
        toLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerProgressLabelFontSize)
        toLabel.typeface = VRBTheme.TYPEFACE_semibold
//        toLabel.setPadding(0,0, inset,0)

        addView(seekBar)
        addView(fromLabel)
        addView(toLabel)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(
            seekBar.id,
            ConstraintSet.TOP,
            ConstraintSet.PARENT_ID,
            ConstraintSet.TOP
        )
        constraints.connect(
            seekBar.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraints.connect(
            seekBar.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )

        constraints.connect(
            fromLabel.id,
            ConstraintSet.START,
            ConstraintSet.PARENT_ID,
            ConstraintSet.START
        )
        constraints.connect(fromLabel.id, ConstraintSet.TOP, seekBar.id, ConstraintSet.BOTTOM)

        constraints.connect(
            toLabel.id,
            ConstraintSet.END,
            ConstraintSet.PARENT_ID,
            ConstraintSet.END
        )
        constraints.connect(toLabel.id, ConstraintSet.TOP, seekBar.id, ConstraintSet.BOTTOM)

        constraints.applyTo(this);
    }


    fun refresh(params: LabeledSeekBarViewParams)
    {
        fromLabel.text = params.fromLabel
        toLabel.text = params.toLabel

        if(!isScrubbing)
        {
            val intValue = (params.value * 100).toInt()

            if (seekBar.progress != intValue) {
                seekBar.progress = intValue
            }
        }

        seekBar.isEnabled = !params.disabled

        if(lastParams == null)
        {
            seekBar.setOnSeekBarChangeListener(createOnSeekBarChangeListener())
        }

        lastParams = params
    }

    private fun createOnSeekBarChangeListener() =
        object : SeekBar.OnSeekBarChangeListener {
            /**
             * Notification that the progress level has changed. Clients can use the fromUser parameter
             * to distinguish user-initiated changes from those that occurred programmatically.
             *
             * @param seekBar The SeekBar whose progress has changed
             * @param progress The current progress level. This will be in the range min..max where min
             * and max were set by [ProgressBar.setMin] and
             * [ProgressBar.setMax], respectively. (The default values for
             * min is 0 and max is 100.)
             * @param fromUser True if the progress change was initiated by the user.
             */
            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                val scalar = progress / 100.0f
                lastParams?.onSeek?.invoke(scalar, fromUser);
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isScrubbing = true
                lastParams?.onScrubStart?.invoke()
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isScrubbing = false
                lastParams?.onScrubEnd?.invoke()
            }
        }
}