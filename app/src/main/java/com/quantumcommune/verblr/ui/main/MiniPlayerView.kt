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
import com.quantumcommune.verblr.R
import com.squareup.picasso.Picasso

data class MiniPlayerViewParams(
    val artworkURL : String?,
    val title : String,
    val buttonIcon : Int,
    val progressBar : ProgressBarViewParams,
    val onClick : () -> Unit,
    val onButtonClick : () -> Unit,
    val isHidden : Boolean = false
)

class MiniPlayerView : ConstraintLayout {

    companion object {
        fun newInstance(context : Context?, params : MiniPlayerViewParams) : MiniPlayerView
        {
            val instance = MiniPlayerView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var progressBar : ProgressBarView
    lateinit var artwork: ImageView
    lateinit var title : TextView
    lateinit var button : ImageView

    val artworkDim = ViewUtils.toPX(40F)
    val buttonDim = ViewUtils.toPX(30F)
    val progressBarHeight = ViewUtils.toPX(4F)

//    var mLastRefreshParams : MiniPlayerViewParams? = null
//
//    val lastRefreshParams : MiniPlayerViewParams?
//        get() = mLastRefreshParams

    fun init(params : MiniPlayerViewParams)
    {
        val marginHorizontalAndBottom = VRBTheme.gutter / 2
        val marginTop = marginHorizontalAndBottom//VRBTheme.smallGutter * 2
        val titleMarginHorizontal = ViewUtils.toXScaledPX(14F)

        refreshVisibility(params)

        artwork = ImageView(context)
        artwork.id = View.generateViewId()
        artwork.layoutParams = ConstraintLayout.LayoutParams(
            artworkDim,
            artworkDim
        )

        title = TextView(context)
        title.id = View.generateViewId()
        title.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        title.setLines(1)
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m15Size)
        title.setTextColor(VRBTheme.COLOR_toolbarCTA)
        title.typeface = VRBTheme.TYPEFACE_semibold

        button = ImageView(context)
        button.id = View.generateViewId()
        button.layoutParams = ConstraintLayout.LayoutParams(
            buttonDim,
            buttonDim
        )

        progressBar = ProgressBarView.newInstance(context, params.progressBar)
        progressBar.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            progressBarHeight
        )

        addView(progressBar)
        addView(artwork)
        addView(title)
        addView(button)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(progressBar.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(progressBar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(progressBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)



        constraints.connect(artwork.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(artwork.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(artwork.id, ConstraintSet.TOP, progressBar.id, ConstraintSet.BOTTOM)
        constraints.connect(artwork.id, ConstraintSet.END, title.id, ConstraintSet.START)
        constraints.setMargin(artwork.id, ConstraintSet.TOP, marginTop)
        constraints.setMargin(artwork.id, ConstraintSet.BOTTOM, marginHorizontalAndBottom)
        constraints.setMargin(artwork.id, ConstraintSet.START, marginHorizontalAndBottom)

        constraints.connect(title.id, ConstraintSet.START, artwork.id, ConstraintSet.END)
        constraints.connect(title.id, ConstraintSet.END, button.id, ConstraintSet.START)
        constraints.centerVertically(title.id, artwork.id)
        constraints.setMargin(title.id, ConstraintSet.START, titleMarginHorizontal)
        constraints.setMargin(title.id, ConstraintSet.END, titleMarginHorizontal)

        constraints.connect(button.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(button.id, ConstraintSet.START, title.id, ConstraintSet.END)
        constraints.centerVertically(button.id, artwork.id)
        constraints.setMargin(button.id, ConstraintSet.END, marginHorizontalAndBottom)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_toolbarBG)
    }


    fun refresh(params : MiniPlayerViewParams) {

        progressBar.refresh(params.progressBar)

        val artworkURL = params.artworkURL

        if(artworkURL != null)
        {
            Picasso.get().load(artworkURL).fit().centerCrop().into(artwork)
        }
        else
        {
            Picasso.get().load(R.drawable.blank_article).into(artwork)
        }

        title.text = params.title

        val buttonClickHandler = params.onButtonClick
        button.setOnClickListener { buttonClickHandler() }

        button.setImageResource(params.buttonIcon)

        val clickHandler = params.onClick
        setOnClickListener { clickHandler() }

        refreshVisibility(params)

//        mLastRefreshParams = params;
    }

    fun refreshVisibility(params : MiniPlayerViewParams)
    {
        if(params.isHidden)
        {
            // [dho] hide - 23/05/20
            isVisible = false
            isGone = true
        }
        else
        {
            // [dho] show - 23/05/20
            isGone = false
            isVisible = true
        }
    }
}
