package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class StandardAdSupportedOverlayViewParams(
    val toolbarParams : ToolbarViewParams,
    val content : View,
    val adParams : AdCardViewParams//,
//    val showUpsell : Boolean,
//    val onUpsellClick : () -> Unit
);

class StandardAdSupportedOverlayView : StandardOverlayView
{
    companion object {
        fun newInstance(context : Context?, params : StandardAdSupportedOverlayViewParams) : StandardAdSupportedOverlayView
        {
            val instance = StandardAdSupportedOverlayView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var ad : AdCardView
    lateinit var adAndContent : ConstraintLayout
    lateinit var nonAdContentContainer : LinearLayout

//    lateinit var upsell : ContextualIconTextView

    fun init(params : StandardAdSupportedOverlayViewParams)
    {
        adAndContent = ConstraintLayout(context)
        adAndContent.id = View.generateViewId()
        adAndContent.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT
        )

//        upsell = ContextualIconTextView.newInstance(context, ContextualIconTextViewParams(
//            icon = R.drawable.star_fill_electro,
//            label = "remove ads",
//            textColor = VRBTheme.COLOR_brandElectro
//        ))

        ad = AdCardView.newInstance(context, params.adParams)
        ad.layoutParams = ConstraintLayout.LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )

        nonAdContentContainer = LinearLayout(context)
        nonAdContentContainer.id = View.generateViewId()
        nonAdContentContainer.layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_CONSTRAINT_SPREAD
        )

        adAndContent.addView(nonAdContentContainer)
//        adAndContent.addView(upsell)
        adAndContent.addView(ad)

        val constraints = ConstraintSet()
        constraints.clone(adAndContent)

        constraints.connect(nonAdContentContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(nonAdContentContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(nonAdContentContainer.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.connect(nonAdContentContainer.id, ConstraintSet.BOTTOM, upsell.id, ConstraintSet.TOP)
        constraints.connect(nonAdContentContainer.id, ConstraintSet.BOTTOM, ad.id, ConstraintSet.TOP)
        constraints.setMargin(nonAdContentContainer.id, ConstraintSet.BOTTOM, VRBTheme.smallGutter)

//        constraints.connect(upsell.id, ConstraintSet.BOTTOM, ad.id, ConstraintSet.TOP)
//        constraints.centerHorizontally(upsell.id, ConstraintSet.PARENT_ID)
//        constraints.setMargin(upsell.id, ConstraintSet.BOTTOM, VRBTheme.smallGutter)

        constraints.connect(ad.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(ad.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(ad.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.applyTo(adAndContent);

        super.init(convertToSuperParams(params))
    }

    fun refresh(params : StandardAdSupportedOverlayViewParams) {
        super.refresh(convertToSuperParams(params))

        nonAdContentContainer.removeAllViews()
        nonAdContentContainer.addView(params.content)

//        upsell.setOnClickListener { params.onUpsellClick() }
//
//        upsell.isVisible = params.showUpsell
//        upsell.isGone = !params.showUpsell

        ad.refresh(params.adParams)
    }

    private fun convertToSuperParams(params : StandardAdSupportedOverlayViewParams) = StandardOverlayViewParams(
        toolbarParams = params.toolbarParams,
        content = adAndContent
    )
}