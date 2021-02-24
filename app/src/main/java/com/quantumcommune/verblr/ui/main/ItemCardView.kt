package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import java.util.*


data class ItemCardViewParams(
    val title : String?,
    val body : String,
    val artwork : View? = null,
    val topContextual : View? = null,
    val bottomContextual : View? = null,
    val cta : ItemCardViewCTAParams,
    val onClick : (() -> Unit)? = null
);

data class ItemCardViewCTAParams(
    val label : String,
    val action : () -> Unit,
    val disabled : Boolean = false
)

open class ItemCardView : ConstraintLayout
{
    companion object {
        // [dho] adapted from : https://stackoverflow.com/a/17670161 - 22/06/20
        val GRAD_background = GradientDrawable().apply {
            this.setColor(VRBTheme.COLOR_contentBG)
            this.cornerRadius = 0f
            this.setStroke(1, VRBTheme.COLOR_contentAccent)
        }

        val marginHorizontal = VRBTheme.gutter / 2
        val marginVertical = VRBTheme.gutter / 2

        val artworkDim : Int
            get() = VRBTheme.articleListArtworkDim


//        const val TAG_body = "FeedItemViewVendor.body"
//        const val TAG_artworkContainer = "FeedItemViewVendor.artworkContainer"
//        const val TAG_title = "FeedItemViewVendor.title"
//        const val TAG_topContextual = "FeedItemViewVendor.topContextual"
//        const val TAG_bottomContextual = "FeedItemViewVendor.bottomContextual"
//        const val TAG_cta = "FeedItemViewVendor.details"

        fun newInstance(context : Context, params : ItemCardViewParams) : ItemCardView
        {
            val instance = ItemCardView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    protected constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }



    lateinit var title : TextView
    lateinit var body : TextView
    lateinit var artworkContainer : LinearLayout
    lateinit var topContextual : LinearLayout
    lateinit var bottomContextual : LinearLayout
    lateinit var cta : LinkView


    fun init(params : ItemCardViewParams)
    {

//        val containerHeight = artworkDim + marginVertical * 2

        artworkContainer = LinearLayout(context)
        artworkContainer.id = View.generateViewId()
        artworkContainer.layoutParams = LinearLayout.LayoutParams(
            artworkDim,
            artworkDim
        );

        title = TextView(context)
        title.id = View.generateViewId()
        title.setLines(1)
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m12Size)
        title.typeface = VRBTheme.TYPEFACE_semibold
        title.setTextColor(VRBTheme.COLOR_contextualInfo)
        title.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        topContextual = LinearLayout(context)
        topContextual.id = View.generateViewId()
        topContextual.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        body = TextView(context)
        body.id = View.generateViewId()
        body.maxLines = 2
        body.ellipsize = TextUtils.TruncateAt.END
        body.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        body.setTextColor(VRBTheme.COLOR_fontTitleMain)
        body.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
        )

        cta = LinkView.newInstance(context, convertToLinkViewParams(params.cta))

        bottomContextual = LinearLayout(context)
        bottomContextual.id = View.generateViewId()
        bottomContextual.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        addView(artworkContainer)
        addView(title)
        addView(body)
        addView(cta)
        addView(topContextual)
        addView(bottomContextual)


        val constraints = ConstraintSet()
        constraints.clone(this)


        constraints.setMargin(artworkContainer.id, ConstraintSet.START, marginHorizontal)

        constraints.setMargin(artworkContainer.id, ConstraintSet.TOP, marginVertical)
        constraints.setMargin(artworkContainer.id, ConstraintSet.BOTTOM, marginVertical)

        constraints.connect(artworkContainer.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(artworkContainer.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(artworkContainer.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)

        constraints.connect(title.id, ConstraintSet.START, artworkContainer.id, ConstraintSet.END)
        constraints.connect(title.id, ConstraintSet.TOP, artworkContainer.id, ConstraintSet.TOP)
        constraints.connect(title.id, ConstraintSet.END, topContextual.id, ConstraintSet.START)

        constraints.setMargin(title.id, ConstraintSet.START, VRBTheme.spacingBetweenArtworkAndMetaInList)


        constraints.connect(topContextual.id, ConstraintSet.END, body.id, ConstraintSet.END)
        constraints.connect(topContextual.id, ConstraintSet.TOP, title.id, ConstraintSet.TOP)
        constraints.connect(topContextual.id, ConstraintSet.BOTTOM, title.id, ConstraintSet.BOTTOM)


        constraints.connect(body.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
        constraints.connect(body.id, ConstraintSet.BOTTOM, cta.id, ConstraintSet.TOP)
        constraints.connect(body.id, ConstraintSet.START, title.id, ConstraintSet.START)
        constraints.connect(body.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(body.id, ConstraintSet.TOP, VRBTheme.smallGutter)

        constraints.setMargin(body.id, ConstraintSet.END, marginHorizontal)

        constraints.connect(cta.id, ConstraintSet.START, title.id, ConstraintSet.START)
        constraints.connect(cta.id, ConstraintSet.BOTTOM, artworkContainer.id, ConstraintSet.BOTTOM)

        constraints.connect(bottomContextual.id, ConstraintSet.END, body.id, ConstraintSet.END)
        constraints.connect(bottomContextual.id, ConstraintSet.BOTTOM, artworkContainer.id, ConstraintSet.BOTTOM)
//        constraints.connect(bottomContextual.id, ConstraintSet.TOP, cta.id, ConstraintSet.TOP)

        constraints.applyTo(this);

        background = GRAD_background
    }

    fun refresh(params : ItemCardViewParams)
    {
        if(params.title != null)
        {
            title.text = params.title.toUpperCase(Locale.ROOT)
            title.isVisible = true
        }
        else
        {
            title.isVisible = false
        }

        body.text = params.body

        artworkContainer.removeAllViews()
        params.artwork?.let { artworkContainer.addView(it) }


        topContextual.removeAllViews()
        params.topContextual?.let {
            topContextual.addView(it)
        }


        bottomContextual.removeAllViews()
        params.bottomContextual?.let {
            bottomContextual.addView(it)
        }

        cta.refresh(convertToLinkViewParams(params.cta));

        setOnClickListener {  }
        params.onClick?.let {
            onClick -> setOnClickListener { onClick() }
        }
    }


    private fun convertToLinkViewParams(params : ItemCardViewCTAParams) =
        LinkViewParams(
            label = params.label,
            action = params.action,
            disabled = params.disabled
        )
}