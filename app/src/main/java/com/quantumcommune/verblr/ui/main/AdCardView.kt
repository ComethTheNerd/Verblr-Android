package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.google.android.gms.ads.VideoController
import com.google.android.gms.ads.formats.MediaView
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.quantumcommune.verblr.R
import com.quantumcommune.verblr.StringUtils

data class AdCardViewParams(
    val ad : UnifiedNativeAd,
    val onComplete : (() -> Unit)? = null
);

class AdCardView : ConstraintLayout
{
    companion object {
        val maxAdvertiserLength = 50
        val maxStoreLength = 20
        val maxPriceLength = 10

        fun newInstance(context : Context?, params : AdCardViewParams) : AdCardView
        {
            val instance = AdCardView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var unifiedNativeAd : UnifiedNativeAdView

    lateinit var feedItem : ItemCardView
    lateinit var subBar : ConstraintLayout

//    lateinit var title : TextView
    lateinit var icon : ImageView
//    lateinit var body : TextView
    lateinit var price : TextView
    lateinit var store : TextView
    lateinit var advertiser : TextView
    lateinit var stars: RatingBarView
//    lateinit var cta : View
    lateinit var media : MediaView

    val iconDim = ViewUtils.toXScaledPX(20F)


    fun init(params : AdCardViewParams)
    {

        unifiedNativeAd = UnifiedNativeAdView(context)
        unifiedNativeAd.id = View.generateViewId()
        unifiedNativeAd.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
//
        val contentContainer = ConstraintLayout(context)
        contentContainer.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

//        title = TextView(context)
//        title.id = R.id.ad_headline
//
//        icon = ImageView(context)
//        icon.id = R.id.ad_app_icon
//
//        body = TextView(context)
//        body.id = R.id.ad_body
//
//        price = TextView(context)
//        price.id = R.id.ad_price
//
//        store = TextView(context)
//        store.id = R.id.ad_store
//
//        advertiser = TextView(context)
//        advertiser.id = R.id.ad_advertiser
//
//        stars = RatingBar(context)
//        stars.id = R.id.ad_store
//
//        cta = Button(context)
//        cta.id = R.id.ad_call_to_action

        media = MediaView(context)
        media.id = R.id.ad_media
        media.setImageScaleType(ImageView.ScaleType.CENTER_CROP)
        media.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        stars = RatingBarView.newInstance(context, convertToRatingBarViewParams(params));

        stars.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            VRBTheme.articleDetailContextualIconDim
        )


        feedItem = ItemCardView.newInstance(
            context,
            convertToFeedItemViewParams(params)
        )



        feedItem.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        initSubBar()

//        contentContainer.addView(title)
//        contentContainer.addView(icon)
//        contentContainer.addView(body)
//        contentContainer.addView(price)
//        contentContainer.addView(store)
//        contentContainer.addView(advertiser)
//        contentContainer.addView(stars)
        contentContainer.addView(feedItem)
        contentContainer.addView(subBar)


        val contentConstraints = ConstraintSet()
        contentConstraints.clone(contentContainer)

        contentConstraints.connect(feedItem.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        contentConstraints.connect(feedItem.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        contentConstraints.connect(feedItem.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        contentConstraints.connect(feedItem.id, ConstraintSet.BOTTOM, subBar.id, ConstraintSet.TOP)
        contentConstraints.connect(subBar.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        contentConstraints.connect(subBar.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        contentConstraints.connect(subBar.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        contentConstraints.applyTo(contentContainer);


        unifiedNativeAd.addView(contentContainer)
        unifiedNativeAd.headlineView = feedItem.title
        unifiedNativeAd.bodyView = feedItem.body
        unifiedNativeAd.callToActionView = feedItem.cta
        unifiedNativeAd.iconView = icon
        unifiedNativeAd.priceView = price
        unifiedNativeAd.starRatingView = stars
        unifiedNativeAd.storeView = store
        unifiedNativeAd.advertiserView = advertiser
        unifiedNativeAd.mediaView = media

        addView(unifiedNativeAd)
    }

    fun initSubBar()
    {
        subBar = ConstraintLayout(context)
        subBar.id = View.generateViewId()
        subBar.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        icon = ImageView(context)
        icon.id = View.generateViewId()
        icon.layoutParams = ConstraintLayout.LayoutParams(
            iconDim,
            iconDim
        )


        advertiser = TextView(context)
        advertiser.id = View.generateViewId()
        advertiser.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        advertiser.setLines(1)
        advertiser.ellipsize = TextUtils.TruncateAt.END
        advertiser.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m10Size)
        advertiser.setTextColor(VRBTheme.COLOR_fontBody)
        advertiser.typeface = VRBTheme.TYPEFACE_semibold



        price = TextView(context)
        price.id = View.generateViewId()
        price.ellipsize = TextUtils.TruncateAt.END
        price.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m9Size)
        price.setTextColor(VRBTheme.COLOR_contextualInfo)
        price.typeface = VRBTheme.TYPEFACE_regular

        store = TextView(context)
        store.id = View.generateViewId()
        store.ellipsize = TextUtils.TruncateAt.END
        store.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m9Size)
        store.setTextColor(VRBTheme.COLOR_contextualInfo)
        store.typeface = VRBTheme.TYPEFACE_regular


        subBar.addView(icon)
        subBar.addView(advertiser)
        subBar.addView(store)
        subBar.addView(price)

        val constraints = ConstraintSet()
        constraints.clone(subBar)

        constraints.connect(icon.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(icon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(icon.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(icon.id, ConstraintSet.END, advertiser.id, ConstraintSet.START)
        constraints.setMargin(icon.id, ConstraintSet.TOP, ItemCardView.marginVertical)
        constraints.setMargin(icon.id, ConstraintSet.BOTTOM, ItemCardView.marginVertical)
        constraints.setMargin(icon.id, ConstraintSet.START, ItemCardView.marginHorizontal)
        constraints.setMargin(icon.id, ConstraintSet.START, ItemCardView.marginHorizontal)


        constraints.centerVertically(advertiser.id, ConstraintSet.PARENT_ID)
//        constraints.connect(advertiser.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(advertiser.id, ConstraintSet.START, icon.id, ConstraintSet.END)
        constraints.connect(advertiser.id, ConstraintSet.END, store.id, ConstraintSet.START)
//        constraints.constrainDefaultWidth(advertiser.id, ConstraintSet.MATCH_CONSTRAINT_SPREAD)
        constraints.setMargin(advertiser.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(advertiser.id, ConstraintSet.TOP, ItemCardView.marginVertical)
        constraints.setMargin(advertiser.id, ConstraintSet.BOTTOM, ItemCardView.marginVertical)

        constraints.connect(price.id, ConstraintSet.BOTTOM, advertiser.id, ConstraintSet.BOTTOM)
        constraints.connect(price.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(price.id, ConstraintSet.END, ItemCardView.marginHorizontal)
//        constraints.connect(store.id, ConstraintSet.START, advertiser.id, ConstraintSet.END)

        constraints.connect(store.id, ConstraintSet.BOTTOM, price.id, ConstraintSet.BOTTOM)
        constraints.connect(store.id, ConstraintSet.END, price.id, ConstraintSet.START)
        constraints.setMargin(store.id, ConstraintSet.START, VRBTheme.gutter)
//        constraints.setMargin(store.id, ConstraintSet.END, VRBTheme.smallGutter)

        constraints.applyTo(subBar)

        subBar.setBackgroundColor(VRBTheme.COLOR_toolbarAccent)
//        subBar.background = FeedItemView.GRAD_background
    }

    fun refresh(params : AdCardViewParams)
    {
        val nativeAd = params.ad

        feedItem.refresh(convertToFeedItemViewParams(params))

        media.setMediaContent(nativeAd.mediaContent)

        if (nativeAd.icon == null) {
            icon.visibility = View.GONE
        } else {
            icon.setImageDrawable(nativeAd.icon.drawable)
            icon.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null  || nativeAd.advertiser.isEmpty()) {
            advertiser.visibility = View.INVISIBLE
        } else {
            advertiser.text = StringUtils.ellipsize(nativeAd.advertiser, maxAdvertiserLength)
            advertiser.visibility = View.VISIBLE
        }

        if (nativeAd.starRating == null) {
            stars.visibility = View.INVISIBLE
        } else {
            stars.refresh(convertToRatingBarViewParams(params))
            stars.visibility = View.VISIBLE
        }


        if (nativeAd.price == null || nativeAd.price.isEmpty()) {
            price.visibility = View.INVISIBLE
        }
        else {
            price.visibility = View.VISIBLE
            price.text = "(${StringUtils.ellipsize(nativeAd.price, maxPriceLength)})"
        }


        if (nativeAd.store == null || nativeAd.store.isEmpty()) {
            store.visibility = View.INVISIBLE
        } else {
            store.visibility = View.VISIBLE
            store.text = StringUtils.ellipsize(nativeAd.store, maxStoreLength)
        }

        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
        // have a video asset.
        val vc = nativeAd.videoController

        // Updates the UI to say whether or not this ad has a video asset.
        if (vc.hasVideoContent()) {
//            videostatus_text.text = String.format(
//                Locale.getDefault(),
//                "Video status: Ad contains a %.2f:1 video asset.",
//                vc.aspectRatio)



            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
            // VideoController will call methods on this object when events occur in the video
            // lifecycle.
            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
                override fun onVideoEnd() {
                    // Publishers should allow native ads to complete video playback before
                    // refreshing or replacing them with another ad in the same UI location.
//                    refresh_button.isEnabled = true
//                    videostatus_text.text = "Video status: Video playback has ended."
                    super.onVideoEnd()

                    params.onComplete?.invoke()
                }
            }
        }


        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        unifiedNativeAd.setNativeAd(nativeAd)
    }

    private fun convertToFeedItemViewParams(params : AdCardViewParams) : ItemCardViewParams {
        val nativeAd = params.ad

        return ItemCardViewParams(
            title = nativeAd.headline,
            body = nativeAd.body ?: "",
            artwork = media,
            topContextual = null,
            bottomContextual = stars,
            cta = ItemCardViewCTAParams(
                label =  StringUtils.toTitleCase(nativeAd.callToAction ?: ""),
                action = {},
                disabled = nativeAd.callToAction == null
            ),
            onClick = null
        )
    }

    private fun convertToRatingBarViewParams(params : AdCardViewParams) =
        RatingBarViewParams(
            rating = params.ad.starRating?.toFloat() ?: 0.0f,
            fullIcon = R.drawable.rating_full_pip_electro,
            emptyIcon = R.drawable.rating_empty_pip_electro
        )

}