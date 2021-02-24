package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import java.util.*

data class ProductButtonViewParams(
    val paymentCadenceLabel : String,
    val priceLocalized : String? = null,
    val cta : TheButtonParams
);

class ProductButtonView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : ProductButtonViewParams) : ProductButtonView
        {
            val instance = ProductButtonView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var cadenceLabel : TextView
    lateinit var priceLabel : TextView
    lateinit var cta : TheButton

    fun init(params : ProductButtonViewParams)
    {
        val labels = LinearLayout(context)
        labels.id = View.generateViewId()
        labels.orientation = LinearLayout.HORIZONTAL
        labels.gravity = Gravity.CENTER_HORIZONTAL

        cadenceLabel = TextView(context)
        cadenceLabel.id = View.generateViewId()
        cadenceLabel.setTextColor(VRBTheme.COLOR_paid)
        cadenceLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.productCardLinkLabelFontSize)
        cadenceLabel.typeface = VRBTheme.TYPEFACE_semibold

        priceLabel = TextView(context)
        priceLabel.id = View.generateViewId()
        priceLabel.setTextColor(VRBTheme.COLOR_toolbarCTA)
        priceLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m14Size)
        priceLabel.typeface = VRBTheme.TYPEFACE_regular
        priceLabel.setPadding(VRBTheme.smallGutter, 0, 0, 0)

        cta = TheButton.newInstance(context, params.cta)
        cta.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        labels.addView(cadenceLabel)
        labels.addView(priceLabel)

        addView(labels)
        addView(cta)


        val constraints = ConstraintSet()
        constraints.clone(this)


        constraints.connect(labels.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(labels.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(labels.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(labels.id, ConstraintSet.END, VRBTheme.gutter)


        constraints.connect(cta.id, ConstraintSet.TOP, labels.id, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(cta.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(cta.id, ConstraintSet.TOP, VRBTheme.gutter / 2)

        constraints.applyTo(this);
    }

    fun refresh(params : ProductButtonViewParams)
    {
        cadenceLabel.text = params.paymentCadenceLabel.toUpperCase(Locale.ROOT)

        if(params.priceLocalized?.isNotEmpty() == true)
        {
            priceLabel.text = params.priceLocalized
            priceLabel.isVisible = true
            priceLabel.isGone = false
        }
        else
        {
            priceLabel.isVisible = false
            priceLabel.isGone = true
        }

        cta.refresh(params.cta)
    }

}