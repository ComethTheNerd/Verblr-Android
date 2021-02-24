package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.quantumcommune.verblr.R
import com.quantumcommune.verblr.openURL

data class LibraryEmptyViewParams(
    val cta : TheButtonParams,
    val ctaStrapline : String,
    val showCTA : Boolean
);



class LibraryEmptyView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : LibraryEmptyViewParams) : LibraryEmptyView
        {
            val instance = LibraryEmptyView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var cta : TheButton
    lateinit var ctaStrapline : TextView

    fun init(params : LibraryEmptyViewParams)
    {

//        val icon = ImageView(context)
//        icon.id = View.generateViewId()
//        icon.layoutParams = LayoutParams(
//            iconDim,
//            iconDim
//        )
//        icon.setImageResource(R.drawable.logo_electro)
        val howToView = LibraryHowToView.newInstance(context, LibraryHowToViewParams())
        howToView.setPadding(0, VRBTheme.gutter, 0, VRBTheme.gutter)

        cta = TheButton.newInstance(context, params.cta)
        ctaStrapline = TextView(context)
        ctaStrapline.id = View.generateViewId()
        ctaStrapline.setTextColor(VRBTheme.COLOR_contextualInfo)
        ctaStrapline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m13Size)
        ctaStrapline.typeface = VRBTheme.TYPEFACE_regular

        addView(howToView)
        addView(cta)
        addView(ctaStrapline)


        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(howToView.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(howToView.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.constrainWidth(howToView.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
        constraints.connect(howToView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//        constraints.connect(howToView.id, ConstraintSet.BOTTOM, cta.id, ConstraintSet.TOP)

        constraints.centerHorizontally(cta.id, ConstraintSet.PARENT_ID)
        constraints.connect(cta.id, ConstraintSet.BOTTOM, ctaStrapline.id, ConstraintSet.TOP)
        constraints.setMargin(cta.id, ConstraintSet.BOTTOM, (VRBTheme.smallGutter / 2) * 3)

        constraints.centerHorizontally(ctaStrapline.id, ConstraintSet.PARENT_ID)
        constraints.connect(ctaStrapline.id, ConstraintSet.BOTTOM,ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.setMargin(ctaStrapline.id, ConstraintSet.BOTTOM, VRBTheme.gutter)
//

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_contentBG)
    }

    fun refresh(params : LibraryEmptyViewParams)
    {
        if(params.showCTA) {
            cta.refresh(params.cta)
            ctaStrapline.text = params.ctaStrapline

            cta.isVisible = true
            cta.isGone = false

            ctaStrapline.isVisible = true
            ctaStrapline.isGone = false
        }
        else
        {
            cta.isVisible = false
            cta.isGone = true

            ctaStrapline.isVisible = false
            ctaStrapline.isGone = true
        }
    }


}

