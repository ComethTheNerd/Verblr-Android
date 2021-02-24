package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.quantumcommune.verblr.R
import com.squareup.picasso.Picasso

data class OverlayPlayerViewParams(
    val artworkURL : String?,
//    val meta : OverlayPlayerMetaViewParams,
    val title : String?,
    val author : String?,
    val organization : String?,
    val onDetailsClick : () -> Unit,
    val seekBar: LabeledSeekBarViewParams,
    val controls : OverlayPlayerControlsViewParams
);

class OverlayPlayerView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : OverlayPlayerViewParams) : OverlayPlayerView
        {
            val instance = OverlayPlayerView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }


    lateinit var artwork : ImageView
    lateinit var title : TextView
    lateinit var authorAndOrganization : TextView
//    lateinit var author : TextView
//    lateinit var organization: TextView
    lateinit var requestDetailsCTA : ImageView
//    lateinit var meta : OverlayPlayerMetaView
    lateinit var seekBar : LabeledSeekBarView
    lateinit var controls : OverlayPlayerControlsView

    val requestDetailsCTADim = VRBTheme.headerViewIconDim

//    val maxArtworkDim = ViewUtils.toXScaledPX(200F)

    fun init(params : OverlayPlayerViewParams)
    {
        artwork = ImageView(context)
        artwork.id = View.generateViewId()
        artwork.layoutParams = LayoutParams(
            0,0
        )

        val meta = initMeta(params)
        meta.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        requestDetailsCTA = ImageView(context)
        requestDetailsCTA.id = View.generateViewId()
        requestDetailsCTA.layoutParams = ConstraintLayout.LayoutParams(
            requestDetailsCTADim,
            requestDetailsCTADim
        )
        requestDetailsCTA.setImageResource(R.drawable.chevron_right_electro)

        seekBar = LabeledSeekBarView.newInstance(context, params.seekBar)
        seekBar.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        controls = OverlayPlayerControlsView.newInstance(context, params.controls)
        controls.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        addView(artwork)
        addView(meta)
        addView(requestDetailsCTA)
        addView(seekBar)
        addView(controls)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(artwork.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(artwork.id, ConstraintSet.PARENT_ID)
//        constraints.connect(artwork.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//        constraints.connect(artwork.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(artwork.id, ConstraintSet.BOTTOM, meta.id, ConstraintSet.TOP)
//        constraints.connect(artwork.id, ConstraintSet.BOTTOM, title.id, ConstraintSet.TOP)
        constraints.setDimensionRatio(artwork.id, "1:1")
        constraints.constrainMaxWidth(artwork.id, (ViewUtils.referenceWidthPX - VRBTheme.gutter * 2).toInt())
        constraints.setMargin(artwork.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(artwork.id, ConstraintSet.END, VRBTheme.gutter)
        constraints.setMargin(artwork.id, ConstraintSet.BOTTOM, VRBTheme.maxiPlayerGutter)


        constraints.connect(meta.id, ConstraintSet.BOTTOM, seekBar.id, ConstraintSet.TOP)
        constraints.connect(meta.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(meta.id, ConstraintSet.END, requestDetailsCTA.id, ConstraintSet.START)
//        constraints.setMargin(meta.id, ConstraintSet.TOP, VRBTheme.maxiPlayerGutter)
        constraints.setMargin(meta.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(meta.id, ConstraintSet.END, VRBTheme.gutter)
        constraints.setMargin(meta.id, ConstraintSet.BOTTOM, VRBTheme.maxiPlayerGutter)


//        constraints.connect(requestDetailsCTA.id, ConstraintSet.START, meta.id, ConstraintSet.END)
        constraints.connect(requestDetailsCTA.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.centerVertically(requestDetailsCTA.id, meta.id)
        constraints.setMargin(requestDetailsCTA.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(seekBar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(seekBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(seekBar.id, ConstraintSet.BOTTOM, controls.id, ConstraintSet.TOP)
//        constraints.setMargin(seekBar.id, ConstraintSet.TOP, VRBTheme.maxiPlayerGutter)
        constraints.setMargin(seekBar.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(seekBar.id, ConstraintSet.END, VRBTheme.gutter)
        constraints.setMargin(seekBar.id, ConstraintSet.BOTTOM, VRBTheme.gutter)

        constraints.connect(controls.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(controls.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(controls.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.setMargin(controls.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(controls.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(controls.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.applyTo(this);
    }


    fun refresh(params : OverlayPlayerViewParams)
    {
        if(params.artworkURL != null)
        {
            Picasso.get().load(params.artworkURL).fit().centerCrop().into(artwork)
        }
        else
        {
            Picasso.get().load(R.drawable.blank_article).into(artwork)
        }

        if(params.title != null)
        {
            title.text = params.title
            title.isVisible = true
        }
        else
        {
            title.isVisible = false
        }

        var t = ""
        var v = false
        if(params.author != null)
        {
            t += params.author
            v = true
        }
        else
        {
            v = false
        }

        if(params.organization != null)
        {
            t += (
                if(params.author?.isNotEmpty() == true)
                    " (${params.organization})"
                else params.organization
            )

            v = true
        }
        else
        {
            v = false
        }

        authorAndOrganization.text = t
        authorAndOrganization.isVisible = v


        requestDetailsCTA.setOnClickListener { params.onDetailsClick() }

        seekBar.refresh(params.seekBar)
        controls.refresh(params.controls)
    }

    private fun initMeta(params : OverlayPlayerViewParams) : ConstraintLayout
    {
        val constraintLayout = ConstraintLayout(context)
        constraintLayout.id = View.generateViewId()

        title = TextView(context)
        title.id = View.generateViewId()
        title.setLines(1)
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextColor(VRBTheme.COLOR_toolbarCTA)
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerTitleFontSize)
        title.typeface = VRBTheme.TYPEFACE_bold
        title.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        title.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT_SPREAD,
            LayoutParams.WRAP_CONTENT
        )

        authorAndOrganization = TextView(context)
        authorAndOrganization.id = View.generateViewId()
        authorAndOrganization.setLines(1)
        authorAndOrganization.ellipsize = TextUtils.TruncateAt.END
        authorAndOrganization.setTextColor(VRBTheme.COLOR_toolbarCTA)
        authorAndOrganization.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerOwnerFontSize)
        authorAndOrganization.typeface = VRBTheme.TYPEFACE_regular
        authorAndOrganization.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
        authorAndOrganization.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT_SPREAD,
            LayoutParams.WRAP_CONTENT
        )

//        organization = TextView(context)
//        organization.id = View.generateViewId()
//        organization.setLines(1)
//        organization.ellipsize = TextUtils.TruncateAt.END
//        organization.setTextColor(VRBTheme.COLOR_toolbarCTA)
//        organization.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerOwnerFontSize)
//        organization.typeface = VRBTheme.TYPEFACE_regular
////        organization.layoutParams = LayoutParams(
////            LayoutParams.MATCH_CONSTRAINT,
////            LayoutParams.WRAP_CONTENT
////        )


        constraintLayout.addView(title)
        constraintLayout.addView(authorAndOrganization)
//        constraintLayout.addView(organization)

        val constraints = ConstraintSet()
        constraints.clone(constraintLayout)

        constraints.connect(title.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(title.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(title.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(authorAndOrganization.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(authorAndOrganization.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
        constraints.connect(authorAndOrganization.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


//        constraints.connect(organization.id, ConstraintSet.START, author.id, ConstraintSet.END)
//        constraints.connect(organization.id, ConstraintSet.TOP, author.id, ConstraintSet.TOP)

        constraints.applyTo(constraintLayout);

        return constraintLayout
    }


}