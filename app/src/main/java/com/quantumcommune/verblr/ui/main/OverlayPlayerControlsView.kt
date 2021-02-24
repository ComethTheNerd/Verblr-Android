package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.ImageView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet

data class OverlayPlayerControlsViewParams(
    val leadingButtonIcon : Int,
    val onLeadingButtonClick : () -> Unit,
    val leadingButtonDisabled : Boolean = false,
    val centralButtonIcon : Int,
    val onCentralButtonClick : () -> Unit,
    val centralButtonDisabled : Boolean = false,
    val trailingButtonIcon : Int,
    val onTrailingButtonClick : () -> Unit,
    val trailingButtonDisabled : Boolean = false
);

class OverlayPlayerControlsView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : OverlayPlayerControlsViewParams) : OverlayPlayerControlsView
        {
            val instance = OverlayPlayerControlsView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }


    lateinit var leadingButton : ImageView
    lateinit var centralButton : ImageView
    lateinit var trailingButton : ImageView

    val leadingButtonDim = ViewUtils.toXScaledPX(32F)
    val centralButtonDim = ViewUtils.toXScaledPX(58F)
    val trailingButtonDim = leadingButtonDim

    fun init(params : OverlayPlayerControlsViewParams)
    {
        val marginHorizontal = VRBTheme.gutter * 4

        leadingButton = ImageView(context)
        leadingButton.id = View.generateViewId()
        leadingButton.layoutParams = ConstraintLayout.LayoutParams(
            leadingButtonDim,
            leadingButtonDim
        )

        centralButton = ImageView(context)
        centralButton.id = View.generateViewId()
        centralButton.layoutParams = ConstraintLayout.LayoutParams(
            centralButtonDim,
            centralButtonDim
        )

        trailingButton = ImageView(context)
        trailingButton.id = View.generateViewId()
        trailingButton.layoutParams = ConstraintLayout.LayoutParams(
            trailingButtonDim,
            trailingButtonDim
        )

        addView(leadingButton)
        addView(centralButton)
        addView(trailingButton)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(centralButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(centralButton.id, ConstraintSet.PARENT_ID)

        constraints.connect(leadingButton.id, ConstraintSet.END, centralButton.id, ConstraintSet.START)
        constraints.centerVertically(leadingButton.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(leadingButton.id, ConstraintSet.END, marginHorizontal)

        constraints.connect(trailingButton.id, ConstraintSet.START, centralButton.id, ConstraintSet.END)
        constraints.centerVertically(trailingButton.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(trailingButton.id, ConstraintSet.START, marginHorizontal)

        constraints.applyTo(this);
    }


    fun refresh(params : OverlayPlayerControlsViewParams)
    {
        leadingButton.setImageResource(params.leadingButtonIcon)

        if(params.leadingButtonDisabled)
        {
            leadingButton.setOnClickListener { }
        }
        else
        {
            leadingButton.setOnClickListener{ params.onLeadingButtonClick() }
        }


        centralButton.setImageResource(params.centralButtonIcon)

        if(params.centralButtonDisabled)
        {
            centralButton.setOnClickListener{ }
        }
        else
        {
            centralButton.setOnClickListener{ params.onCentralButtonClick() }
        }


        trailingButton.setImageResource(params.trailingButtonIcon)

        if(params.trailingButtonDisabled)
        {
            trailingButton.setOnClickListener {  }
        }
        else
        {
            trailingButton.setOnClickListener{ params.onTrailingButtonClick() }
        }
    }
}