package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import java.util.*

data class HeaderViewParams(
    val icon : Int? = null,
    val title : String,
    val strapline : String,
    val action : (() -> Unit)? = null
)

class HeaderView : ConstraintLayout {

    companion object {
        fun newInstance(context : Context?, params : HeaderViewParams) : HeaderView
        {
            val instance = HeaderView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }



    private constructor(context : Context?) : super(context) {
        id = View.generateViewId()
    }

    lateinit var icon: ImageView
    lateinit var title : TextView
    lateinit var iconAndTitleContainer : LinearLayout
    lateinit var strapline : TextView

    val iconDim = ViewUtils.toPX(30F)

    fun init(params : HeaderViewParams)
    {
        val margin = VRBTheme.gutter

        iconAndTitleContainer = LinearLayout(context)
        iconAndTitleContainer.id = View.generateViewId()
        iconAndTitleContainer.orientation = LinearLayout.HORIZONTAL
        iconAndTitleContainer.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        iconAndTitleContainer.gravity = Gravity.CENTER

        icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = ConstraintLayout.LayoutParams(
            iconDim,
            iconDim
        )
        icon.setPadding(0, 0,VRBTheme.smallGutter,0)

        title = TextView(context)
        title.id = View.generateViewId()
        title.setLines(1)
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageHeadingFontSize)
        title.setTextColor(VRBTheme.COLOR_fontTitleMain)
        title.typeface = VRBTheme.TYPEFACE_semibold

        strapline = TextView(context)
        strapline.id = View.generateViewId()
        strapline.setLines(1)
        strapline.ellipsize = TextUtils.TruncateAt.END
        strapline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageStraplineFontSize)
        strapline.setTextColor(VRBTheme.COLOR_fontBody)
        strapline.typeface = VRBTheme.TYPEFACE_regular

        iconAndTitleContainer.addView(icon)
        iconAndTitleContainer.addView(title)


        addView(iconAndTitleContainer)
        addView(strapline)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(iconAndTitleContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(iconAndTitleContainer.id, ConstraintSet.PARENT_ID)
        constraints.connect(iconAndTitleContainer.id, ConstraintSet.BOTTOM, strapline.id, ConstraintSet.TOP)
        constraints.setMargin(iconAndTitleContainer.id, ConstraintSet.START, margin)
        constraints.setMargin(iconAndTitleContainer.id, ConstraintSet.END, margin)
        constraints.setMargin(iconAndTitleContainer.id, ConstraintSet.TOP, margin)

        constraints.connect(strapline.id, ConstraintSet.TOP, iconAndTitleContainer.id, ConstraintSet.BOTTOM)
        constraints.connect(strapline.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(strapline.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(strapline.id, ConstraintSet.TOP, VRBTheme.smallGutter)
        constraints.setMargin(strapline.id, ConstraintSet.START, margin)
        constraints.setMargin(strapline.id, ConstraintSet.END, margin)
        constraints.setMargin(strapline.id, ConstraintSet.BOTTOM, margin)

        constraints.applyTo(this);
    }


    fun refresh(params : HeaderViewParams) {

        if(params.icon != null)
        {
            icon.setImageResource(params.icon)
            icon.isVisible = true
        }
        else
        {
            icon.isVisible = false
        }



        title.text = params.title.toLowerCase(Locale.ROOT)

//        if(strapline.text != null)
//        {
            strapline.text = params.strapline//.toLowerCase(Locale.ROOT)
//            strapline.isVisible = true
//            strapline.isGone = false
//        }
//        else
//        {
//            strapline.isVisible = false
//            strapline.isGone = true
//        }

        val clickHandler = params.action

        if(clickHandler != null)
        {
            setOnClickListener { clickHandler() }
        }
        else
        {
            setOnClickListener {  }
        }
    }
}
