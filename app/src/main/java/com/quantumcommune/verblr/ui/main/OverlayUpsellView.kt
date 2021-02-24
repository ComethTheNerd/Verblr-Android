package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.quantumcommune.verblr.R

data class OverlayUpsellViewParams(
    val subscribeMonthlyCTA : ProductButtonViewParams,
    val subscribeYearlyCTA : ProductButtonViewParams
);

private val PERK_newFeatures = "exclusive access to new features"
private val PERK_noAds = "no ads"
private val PERK_offlinePlayback = "offline playback"
private val PERK_faster = "faster article processing"
private val PERK_cancelAnytime = "cancel anytime"

class OverlayUpsellView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : OverlayUpsellViewParams) : OverlayUpsellView
        {
            val instance = OverlayUpsellView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    val iconDim = ViewUtils.toPX(50F)

    lateinit var monthlyProductButton : ProductButtonView
    lateinit var yearlyProductButton : ProductButtonView

    private val horizontalMargin = VRBTheme.gutter * 2


    fun init(params : OverlayUpsellViewParams)
    {

        val icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = LayoutParams(
            iconDim,
            iconDim
        )
        icon.setImageResource(R.drawable.logo_electro)

        val title = TextView(context)
        title.id = View.generateViewId()
        title.text = "PREMIUM"
        title.setTextColor(VRBTheme.COLOR_brandElectro)
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        title.letterSpacing = VRBTheme.impactTitleLetterSpacing
        title.typeface = VRBTheme.TYPEFACE_semibold

//        val strapline = TextView(context)
//        strapline.id = View.generateViewId()
//        strapline.setTextColor(VRBTheme.COLOR_fontBody)
//        strapline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
//        strapline.typeface = VRBTheme.TYPEFACE_regular
//        strapline.text = "subscribe today and enjoy"


        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_brandElectro
        ))
        divider.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )


        val sp1_starIcon = createSellingPointIcon()
        val sp1_label = createSellingPointLabel(PERK_noAds)

        val sp2_starIcon = createSellingPointIcon()
        val sp2_label = createSellingPointLabel(PERK_offlinePlayback)

        val sp3_starIcon = createSellingPointIcon()
        val sp3_label = createSellingPointLabel(PERK_faster)

        val sp4_starIcon = createSellingPointIcon()
        val sp4_label = createSellingPointLabel(PERK_newFeatures)

        val sp5_starIcon = createSellingPointIcon()
        val sp5_label = createSellingPointLabel(PERK_cancelAnytime)


        val buttonPanel = initButtonPanel(params)
        buttonPanel.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        addView(icon)
        addView(title)
//        addView(strapline)
        addView(divider)
        addView(sp1_starIcon)
        addView(sp1_label)
        addView(sp2_starIcon)
        addView(sp2_label)
        addView(sp3_starIcon)
        addView(sp3_label)
        addView(sp4_starIcon)
        addView(sp4_label)
        addView(sp5_starIcon)
        addView(sp5_label)
        addView(buttonPanel)


        val constraints = ConstraintSet()
        constraints.clone(this)



        constraints.centerHorizontally(icon.id, ConstraintSet.PARENT_ID)
        constraints.connect(icon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.centerHorizontally(title.id, ConstraintSet.PARENT_ID)
        constraints.connect(title.id, ConstraintSet.TOP, icon.id, ConstraintSet.BOTTOM)
        constraints.setMargin(title.id, ConstraintSet.TOP, VRBTheme.gutter / 2)

//        constraints.centerHorizontally(strapline.id, ConstraintSet.PARENT_ID)
//        constraints.connect(strapline.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
//        constraints.setMargin(strapline.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.centerHorizontally(divider.id, ConstraintSet.PARENT_ID)
        constraints.connect(divider.id, ConstraintSet.TOP, /*strapline.id*/title.id, ConstraintSet.BOTTOM)

        constraints.setMargin(divider.id, ConstraintSet.TOP, VRBTheme.maxiPlayerGutter)
        constraints.setMargin(divider.id, ConstraintSet.START, horizontalMargin)
        constraints.setMargin(divider.id, ConstraintSet.END, horizontalMargin)


        constraints.connect(sp1_starIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        addSellingPointIconMargins(constraints, sp1_starIcon)

        constraints.connect(sp1_label.id, ConstraintSet.START, sp1_starIcon.id, ConstraintSet.END)
        constraints.connect(sp1_label.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp1_label)

        constraints.centerVertically(sp1_starIcon.id, sp1_label.id)

        constraints.connect(sp2_starIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp2_starIcon)

        constraints.connect(sp2_label.id, ConstraintSet.START, sp2_starIcon.id, ConstraintSet.END)
        constraints.connect(sp2_label.id, ConstraintSet.TOP, sp1_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp2_label)

        constraints.centerVertically(sp2_starIcon.id, sp2_label.id)


        constraints.connect(sp3_starIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp3_starIcon)

        constraints.connect(sp3_label.id, ConstraintSet.START, sp3_starIcon.id, ConstraintSet.END)
        constraints.connect(sp3_label.id, ConstraintSet.TOP, sp2_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp3_label)

        constraints.centerVertically(sp3_starIcon.id, sp3_label.id)


        constraints.connect(sp4_starIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp4_starIcon)

        constraints.connect(sp4_label.id, ConstraintSet.START, sp4_starIcon.id, ConstraintSet.END)
        constraints.connect(sp4_label.id, ConstraintSet.TOP, sp3_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp4_label)

        constraints.centerVertically(sp4_starIcon.id, sp4_label.id)


        constraints.connect(sp5_starIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp5_starIcon)

        constraints.connect(sp5_label.id, ConstraintSet.START, sp5_starIcon.id, ConstraintSet.END)
        constraints.connect(sp5_label.id, ConstraintSet.TOP, sp4_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp5_label)

        constraints.centerVertically(sp5_starIcon.id, sp5_label.id)


        constraints.centerHorizontally(buttonPanel.id, ConstraintSet.PARENT_ID)
        constraints.connect(buttonPanel.id, ConstraintSet.TOP, sp5_label.id, ConstraintSet.BOTTOM)
        constraints.setMargin(buttonPanel.id, ConstraintSet.TOP, VRBTheme.gutter * 3)

        constraints.applyTo(this);
    }


    fun refresh(params : OverlayUpsellViewParams)
    {
        monthlyProductButton.refresh(params.subscribeMonthlyCTA)
        yearlyProductButton.refresh(params.subscribeYearlyCTA)
    }


    private fun initButtonPanel(params : OverlayUpsellViewParams) : ConstraintLayout
    {
        val buttonPanel = ConstraintLayout(context)
        buttonPanel.id = View.generateViewId()

        monthlyProductButton = ProductButtonView.newInstance(context, params.subscribeMonthlyCTA)
        monthlyProductButton.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        yearlyProductButton = ProductButtonView.newInstance(context, params.subscribeYearlyCTA)
        yearlyProductButton.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        //        constraints.createHorizontalChain(
//            ConstraintSet.PARENT_ID, ConstraintSet.LEFT,
//            ConstraintSet.PARENT_ID, ConstraintSet.RIGHT,
//            intArrayOf(monthlyProductButton.id, yearlyProductButton.id),
//            null,
//            ConstraintSet.CHAIN_PACKED
//        )
//


        buttonPanel.addView(monthlyProductButton)
        buttonPanel.addView(yearlyProductButton)

//        val tv = TextView(context)
//        tv.id = View.generateViewId()
//        tv.text = "Yes sir"

//        buttonPanel.addView(tv)

        val constraints = ConstraintSet()
        constraints.clone(buttonPanel)

//        constraints.connect(tv.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//        constraints.connect(tv.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)


        constraints.connect(monthlyProductButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(monthlyProductButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(yearlyProductButton.id, ConstraintSet.START, monthlyProductButton.id, ConstraintSet.END)
        constraints.connect(yearlyProductButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.setMargin(yearlyProductButton.id, ConstraintSet.START, VRBTheme.gutter * 2)



        constraints.applyTo(buttonPanel);


        return buttonPanel
    }


    private fun createSellingPointIcon() =
        ImageView(context).apply {
            this.id = View.generateViewId()
            this.layoutParams = ConstraintLayout.LayoutParams(
                VRBTheme.contextualIconDim,
                VRBTheme.contextualIconDim
            )
            this.setImageResource(R.drawable.star_fill_electro)
        }

    private fun createSellingPointLabel(text : String) =
        TextView(context).apply {
            this.id = View.generateViewId()
            this.text = text
            this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
            this.setTextColor(VRBTheme.COLOR_fontBody)
            this.typeface = VRBTheme.TYPEFACE_regular
        }

    private fun addSellingPointIconMargins(constraints : ConstraintSet, icon : ImageView)
    {
        constraints.setMargin(icon.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(icon.id, ConstraintSet.START, horizontalMargin)
    }

    private fun addSellingPointLabelMargins(constraints : ConstraintSet, label : TextView)
    {
        constraints.setMargin(label.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(label.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(label.id, ConstraintSet.END, horizontalMargin)
    }
}

