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
import com.quantumcommune.verblr.R
import com.quantumcommune.verblr.openURL

data class LibraryHowToViewParams(
    val x : Int? = null
);

class LibraryHowToView : ConstraintLayout
{
    companion object {
        fun newInstance(context : Context?, params : LibraryHowToViewParams) : LibraryHowToView
        {
            val instance = LibraryHowToView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }


    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    val iconDim = ViewUtils.toPX(20F)

    private val horizontalMargin = VRBTheme.gutter * 2

    private val labelMargin = ViewUtils.toXScaledPX(4F)

    fun init(params : LibraryHowToViewParams)
    {

//        val icon = ImageView(context)
//        icon.id = View.generateViewId()
//        icon.layoutParams = LayoutParams(
//            iconDim,
//            iconDim
//        )
//        icon.setImageResource(R.drawable.logo_electro)

        val title = TextView(context)
        title.id = View.generateViewId()
        title.text = "WELCOME"
        title.setTextColor(VRBTheme.COLOR_fontTitleMain)
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        title.letterSpacing = VRBTheme.impactTitleLetterSpacing
        title.typeface = VRBTheme.TYPEFACE_semibold

        val strapline = TextView(context)
        strapline.id = View.generateViewId()
        strapline.setTextColor(VRBTheme.COLOR_fontBody)
        strapline.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        strapline.typeface = VRBTheme.TYPEFACE_regular
        strapline.text = "let's get your library set up"


        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_divider
        ))
        divider.layoutParams = LayoutParams(
            LayoutParams.MATCH_CONSTRAINT,
            LayoutParams.WRAP_CONTENT
        )


        val sp1_sellingPointIcon = createSellingPointIcon(R.drawable.one_fill_subwhite)
        val sp1_label = X123(
            createSellingPointLabel("go to an article in").apply {
                this.setPadding(0,0, labelMargin + labelMargin / 4, 0)
            },
            LinkView.newInstance(context, LinkViewParams(
                fontSize = VRBTheme.m17Size,
                label = "Chrome",
                action = { openURL(context, "https://google.com")}
            ))
        )

        val sp2_sellingPointIcon = createSellingPointIcon(R.drawable.two_fill_subwhite)
        val sp2_label = X123(
            createSellingPointLabel("open the menu").apply {
                this.setPadding(0,0, labelMargin / 2, 0)
            },
            ImageView(context).apply {
                this.id = View.generateViewId()
                this.setImageResource(R.drawable.menu_subwhite)
                this.layoutParams = LinearLayout.LayoutParams(
                    iconDim,
                    iconDim
                )
            },
            createSellingPointLabel("and tap").apply {
                this.setPadding(labelMargin / 2,0, labelMargin, 0)
            },
            TextView(context).apply {
                this.id = View.generateViewId()
                this.text = "Share..."
                this.typeface = VRBTheme.TYPEFACE_semibold
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
                this.setTextColor(VRBTheme.COLOR_fontBody)
            }
        )

        val sp3_sellingPointIcon = createSellingPointIcon(R.drawable.three_fill_subwhite)
        val sp3_label = X123(
            createSellingPointLabel("choose the").apply{
                this.setPadding(0,0, labelMargin + labelMargin / 2, 0)
            },
            ImageView(context).apply {
                this.id = View.generateViewId()
                this.setImageResource(R.drawable.logo_mono)
                this.layoutParams = LinearLayout.LayoutParams(
                    iconDim,
                    iconDim
                )
            },
            createSellingPointLabel("app").apply{
                this.setPadding(labelMargin + labelMargin / 2,0, 0, 0)
            }
        )

        val sp4_sellingPointIcon = createSellingPointIcon(R.drawable.four_fill_subwhite)
        val sp4_label = createSellingPointLabel("come back to listen!")
//
//        val sp5_sellingPointIcon = createSellingPointIcon(R.drawable.five_fill_subwhite)
//        val sp5_label = createSellingPointLabel("debug time")




//        addView(icon)
        addView(title)
        addView(strapline)
        addView(divider)
        addView(sp1_sellingPointIcon)
        addView(sp1_label)
        addView(sp2_sellingPointIcon)
        addView(sp2_label)
        addView(sp3_sellingPointIcon)
        addView(sp3_label)
        addView(sp4_sellingPointIcon)
        addView(sp4_label)


        val constraints = ConstraintSet()
        constraints.clone(this)


//
//        constraints.centerHorizontally(icon.id, ConstraintSet.PARENT_ID)
//        constraints.connect(icon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.centerHorizontally(title.id, ConstraintSet.PARENT_ID)
        constraints.connect(title.id, ConstraintSet.TOP, /*icon.id*/ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.setMargin(title.id, ConstraintSet.TOP, VRBTheme.gutter / 2)

        constraints.centerHorizontally(strapline.id, ConstraintSet.PARENT_ID)
        constraints.connect(strapline.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
        constraints.setMargin(strapline.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.centerHorizontally(divider.id, ConstraintSet.PARENT_ID)
        constraints.connect(divider.id, ConstraintSet.TOP, strapline.id, ConstraintSet.BOTTOM)

        constraints.setMargin(divider.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(divider.id, ConstraintSet.START, horizontalMargin)
        constraints.setMargin(divider.id, ConstraintSet.END, horizontalMargin)


        constraints.connect(sp1_sellingPointIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        addSellingPointIconMargins(constraints, sp1_sellingPointIcon)

        constraints.connect(sp1_label.id, ConstraintSet.START, sp1_sellingPointIcon.id, ConstraintSet.END)
        constraints.connect(sp1_label.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp1_label)

        constraints.centerVertically(sp1_sellingPointIcon.id, sp1_label.id)

        constraints.connect(sp2_sellingPointIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp2_sellingPointIcon)

        constraints.connect(sp2_label.id, ConstraintSet.START, sp2_sellingPointIcon.id, ConstraintSet.END)
        constraints.connect(sp2_label.id, ConstraintSet.TOP, sp1_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp2_label)

        constraints.centerVertically(sp2_sellingPointIcon.id, sp2_label.id)


        constraints.connect(sp3_sellingPointIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp3_sellingPointIcon)

        constraints.connect(sp3_label.id, ConstraintSet.START, sp3_sellingPointIcon.id, ConstraintSet.END)
        constraints.connect(sp3_label.id, ConstraintSet.TOP, sp2_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp3_label)

        constraints.centerVertically(sp3_sellingPointIcon.id, sp3_label.id)


        constraints.connect(sp4_sellingPointIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        addSellingPointIconMargins(constraints, sp4_sellingPointIcon)

        constraints.connect(sp4_label.id, ConstraintSet.START, sp4_sellingPointIcon.id, ConstraintSet.END)
        constraints.connect(sp4_label.id, ConstraintSet.TOP, sp3_label.id, ConstraintSet.BOTTOM)
        addSellingPointLabelMargins(constraints, sp4_label)

        constraints.centerVertically(sp4_sellingPointIcon.id, sp4_label.id)

        constraints.applyTo(this);

        setBackgroundColor(VRBTheme.COLOR_contentBG)
    }

    fun refresh(params : LibraryHowToViewParams)
    {

    }

    private fun X123(vararg views : View)
        = LinearLayout(context).apply {
            this.id = View.generateViewId()
            this.orientation = LinearLayout.HORIZONTAL
            this.layoutParams = ConstraintLayout.LayoutParams(
                ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
                ConstraintLayout.LayoutParams.WRAP_CONTENT
            )
            this.gravity = Gravity.CENTER_VERTICAL

            views.forEach { this.addView(it) }
        }

    private fun createSellingPointIcon(icon : Int) =
        ImageView(context).apply {
            this.id = View.generateViewId()
            this.layoutParams = ConstraintLayout.LayoutParams(
                iconDim,
                iconDim
            )
            this.setImageResource(icon)
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

    private fun addSellingPointLabelMargins(constraints : ConstraintSet, label : View)
    {
        constraints.setMargin(label.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(label.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(label.id, ConstraintSet.END, horizontalMargin)
    }
}

