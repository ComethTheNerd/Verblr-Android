package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.quantumcommune.verblr.R

data class UpsellBannerViewParams(
    val onClick : () -> Unit
)

class UpsellBannerView : ConstraintLayout {

    companion object {
        fun newInstance(context : Context?, params : UpsellBannerViewParams) : UpsellBannerView
        {
            val instance = UpsellBannerView(context)
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
//    lateinit var iconAndTitleContainer : LinearLayout
    lateinit var strapline : TextView

    val iconDim = ViewUtils.toPX(VRBTheme.m13Size)

    fun init(params : UpsellBannerViewParams)
    {
        val marginHorizontal = VRBTheme.gutter / 2
        val marginVertical = VRBTheme.smallGutter * 2

        val starStart = createStar()
        val starEnd = createStar()
        val tt = initText()
//        tt.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
//            ConstraintLayout.LayoutParams.WRAP_CONTENT
//        )

        addView(starStart)
//        addView(iconAndTitleContainer)
        addView(tt)
        addView(starEnd)

        val constraints = ConstraintSet()
        constraints.clone(this)

        constraints.connect(starStart.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.centerVertically(starStart.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(starStart.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(starStart.id, ConstraintSet.END, marginHorizontal)
        constraints.setMargin(starStart.id, ConstraintSet.TOP, marginVertical)
        constraints.setMargin(starStart.id, ConstraintSet.BOTTOM, marginVertical)

        constraints.centerVertically(tt.id, ConstraintSet.PARENT_ID)
        constraints.centerHorizontally(tt.id, ConstraintSet.PARENT_ID)
//        constraints.connect(tt.id, ConstraintSet.START, starStart.id, ConstraintSet.END)
//        constraints.connect(tt.id, ConstraintSet.END, starEnd.id, ConstraintSet.START)
        constraints.setMargin(tt.id, ConstraintSet.START, VRBTheme.gutter / 2)
        constraints.setMargin(tt.id, ConstraintSet.END, VRBTheme.gutter / 2)

        constraints.connect(starEnd.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.centerVertically(starEnd.id, starStart.id)
        constraints.setMargin(starEnd.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(starEnd.id, ConstraintSet.END, marginHorizontal)
        constraints.setMargin(starEnd.id, ConstraintSet.TOP, marginVertical)
        constraints.setMargin(starEnd.id, ConstraintSet.BOTTOM, marginVertical)

//        constraints.connect(iconAndTitleContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//        constraints.centerHorizontally(iconAndTitleContainer.id, ConstraintSet.PARENT_ID)
//        constraints.connect(iconAndTitleContainer.id, ConstraintSet.BOTTOM, strapline.id, ConstraintSet.TOP)
//        constraints.setMargin(iconAndTitleContainer.id, ConstraintSet.TOP, margin)
//        constraints.setMargin(iconAndTitleContainer.id, ConstraintSet.BOTTOM, 5)

//        constraints.connect(strapline.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//        constraints.centerHorizontally(strapline.id, ConstraintSet.PARENT_ID)
//        constraints.setMargin(strapline.id, ConstraintSet.BOTTOM, margin)


        constraints.applyTo(this);


        setBackgroundColor(VRBTheme.COLOR_toolbarAccent)
    }

    private fun initText() : ConstraintLayout
    {
        val constraintLayout = ConstraintLayout(context)
        constraintLayout.id = View.generateViewId()

        icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = ConstraintLayout.LayoutParams(
            iconDim,
            iconDim
        )
        icon.setImageResource(R.drawable.logo_electro)
//        icon.setPadding(0, 0, VRBTheme.smallGutter, 0)

        title = TextView(context)
        title.id = View.generateViewId()
        title.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.WRAP_CONTENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        title.setLines(1)
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m13Size)
        title.setTextColor(VRBTheme.COLOR_brandElectro)
        title.typeface = VRBTheme.TYPEFACE_semibold
        title.letterSpacing = VRBTheme.impactTitleLetterSpacing / 2
        title.text = "PREMIUM"
        title.setPadding(ViewUtils.toXScaledPX(4.0F), 0, 0, 0)

        strapline = TextView(context)
        strapline.id = View.generateViewId()
        strapline.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        strapline.setLines(1)
        strapline.ellipsize = TextUtils.TruncateAt.END
        strapline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m13Size)
        strapline.setTextColor(VRBTheme.COLOR_fontBody)
        strapline.typeface = VRBTheme.TYPEFACE_regular
        strapline.text = " is here! tap to find out more..."


//        iconAndTitleContainer.addView(icon)
//        iconAndTitleContainer.addView(title)


//        addView(iconAndTitleContainer)
        constraintLayout.addView(icon)
        constraintLayout.addView(title)
        constraintLayout.addView(strapline)


        val constraints = ConstraintSet()
        constraints.clone(constraintLayout)


        constraints.centerVertically(icon.id, ConstraintSet.PARENT_ID)
        constraints.connect(icon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        constraints.centerVertically(title.id, ConstraintSet.PARENT_ID)
        constraints.connect(title.id, ConstraintSet.START, icon.id, ConstraintSet.END)

        constraints.connect(strapline.id, ConstraintSet.BOTTOM, title.id, ConstraintSet.BOTTOM)
        constraints.connect(strapline.id, ConstraintSet.START, title.id, ConstraintSet.END)
//        constraints.setMargin(strapline.id, ConstraintSet.START, VRBTheme.smallGutter / 2)


        constraints.applyTo(constraintLayout);

        return constraintLayout
    }


    fun refresh(params : UpsellBannerViewParams) {
        val clickHandler = params.onClick
        setOnClickListener { clickHandler() }
    }

    private fun createStar() = ImageView(context).apply {
        this.id = View.generateViewId()
        this.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.contextualIconDim,
            VRBTheme.contextualIconDim
        )
        this.setImageResource(R.drawable.star_fill_electro)
    }
}
