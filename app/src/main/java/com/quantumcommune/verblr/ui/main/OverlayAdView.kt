//package com.quantumcommune.verblr.ui.main
//
//import android.content.Context
//import android.graphics.Color
//import android.view.View
//import android.view.ViewGroup
//import androidx.constraintlayout.widget.ConstraintLayout
//import androidx.constraintlayout.widget.ConstraintSet
//import com.google.android.gms.ads.formats.UnifiedNativeAd
//
//data class OverlayAdViewParams(
//    val ad : UnifiedNativeAd,
//    val onComplete : (() -> Unit)? = null
//);
//
//class OverlayAdView : ConstraintLayout
//{
//    companion object {
//        fun newInstance(context : Context?, params : OverlayAdViewParams) : OverlayAdView
//        {
//            val instance = OverlayAdView(context)
//            instance.init(params);
//            instance.refresh(params)
//
//            return instance;
//        }
//    }
//
//    private constructor(context : Context?) : super(context)
//    {
//        id = View.generateViewId()
//    }
//
//    private var lastParams : OverlayAdViewParams? = null
//
//    lateinit var adCard : AdCardView
//
////    lateinit var unifiedNativeAd : UnifiedNativeAdView
////
////    lateinit var title : TextView
////    lateinit var icon : ImageView
////    lateinit var body : TextView
////    lateinit var price : TextView
////    lateinit var store : TextView
////    lateinit var advertiser : TextView
////    lateinit var stars: RatingBar
////    lateinit var cta : View
////    lateinit var media : MediaView
//    lateinit var skipAdButton : TheButton
//
//    fun init(params : OverlayAdViewParams)
//    {
//        lastParams = params
//
//        adCard = AdCardView.newInstance(context, AdCardViewParams(ad = params.ad, onComplete = params.onComplete))
//
////
////        unifiedNativeAd = UnifiedNativeAdView(context)
////        unifiedNativeAd.id = View.generateViewId()
////        unifiedNativeAd.layoutParams = ConstraintLayout.LayoutParams(
////            ConstraintLayout.LayoutParams.MATCH_PARENT,
////            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
////        )
//
//        val content = ConstraintLayout(context)
//        content.layoutParams = ViewGroup.LayoutParams(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
////
////        title = TextView(context)
////        title.id = R.id.ad_headline
////
////        icon = ImageView(context)
////        icon.id = R.id.ad_app_icon
////
////        body = TextView(context)
////        body.id = R.id.ad_body
////
////        price = TextView(context)
////        price.id = R.id.ad_price
////
////        store = TextView(context)
////        store.id = R.id.ad_store
////
////        advertiser = TextView(context)
////        advertiser.id = R.id.ad_advertiser
////
////        stars = RatingBar(context)
////        stars.id = R.id.ad_store
////
////        cta = Button(context)
////        cta.id = R.id.ad_call_to_action
////
////        media = MediaView(context)
////        media.id = R.id.ad_media
////
////
////        content.addView(title)
////        content.addView(icon)
////        content.addView(body)
////        content.addView(price)
////        content.addView(store)
////        content.addView(advertiser)
////        content.addView(stars)
////        content.addView(cta)
////        content.addView(media)
////
////
////        val contentConstraints = ConstraintSet()
////        contentConstraints.clone(content)
////
////        contentConstraints.connect(title.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
////        contentConstraints.connect(title.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
////        contentConstraints.connect(title.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
////
////        contentConstraints.connect(media.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
////        contentConstraints.connect(media.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
////        contentConstraints.connect(media.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
////        contentConstraints.connect(media.id, ConstraintSet.BOTTOM, body.id, ConstraintSet.TOP)
////
////        contentConstraints.connect(body.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
////        contentConstraints.connect(body.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
////        contentConstraints.connect(body.id, ConstraintSet.BOTTOM, stars.id, ConstraintSet.TOP)
////
////        contentConstraints.connect(stars.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
////        contentConstraints.connect(stars.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
////        contentConstraints.connect(stars.id, ConstraintSet.BOTTOM, cta.id, ConstraintSet.TOP)
////
////
////        contentConstraints.connect(cta.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
////        contentConstraints.connect(cta.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
////        contentConstraints.connect(cta.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
////
////
////        contentConstraints.applyTo(content);
////
////        content.setBackgroundColor(Color.parseColor("#42F488"))
////
////
////        unifiedNativeAd.addView(content)
////        unifiedNativeAd.headlineView = title
////        unifiedNativeAd.bodyView = body
////        unifiedNativeAd.callToActionView = cta
////        unifiedNativeAd.iconView = icon
////        unifiedNativeAd.priceView = price
////        unifiedNativeAd.starRatingView = stars
////        unifiedNativeAd.storeView = store
////        unifiedNativeAd.advertiserView = advertiser
////        unifiedNativeAd.mediaView = media
////
////
////        unifiedNativeAd.setBackgroundColor(Color.parseColor("#FF0000"))
////
//
//        skipAdButton = TheButton.newInstance(
//            context,
//            TheButtonParams(
//                label = "Skip Ad",
//                disabled = false,
//                action = this::onSkipAdClick
//            )
//        )
//
//        addView(adCard)
//        addView(skipAdButton)
//
//
//        val constraints = ConstraintSet()
//        constraints.clone(this)
//
//        constraints.connect(adCard.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//        constraints.connect(adCard.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//        constraints.connect(adCard.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.connect(adCard.id, ConstraintSet.BOTTOM, skipAdButton.id, ConstraintSet.TOP)
//
//        constraints.connect(skipAdButton.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//        constraints.connect(skipAdButton.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//
//
//        constraints.applyTo(this)
//    }
//
//
//    private fun onSkipAdClick()
//    {
//        try {
//            lastParams?.ad?.videoController?.pause()
//        }
//        catch(err : Exception)
//        {
//
//        }
//
//        lastParams?.onComplete?.invoke()
//    }
//
//    fun refresh(params : OverlayAdViewParams)
//    {
//        lastParams = params
//
//        adCard.refresh(
//            AdCardViewParams(ad = params.ad, onComplete = params.onComplete)
//        )
////
////        val nativeAd = params.ad
////
////        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
////        title.text = nativeAd.headline
////
////        media.setMediaContent(nativeAd.mediaContent)
////
////        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
////        // check before trying to display them.
////        if (nativeAd.body == null) {
////            body.visibility = View.INVISIBLE
////        } else {
////            body.visibility = View.VISIBLE
////            body.text = nativeAd.body
////        }
////
////        if (nativeAd.callToAction == null) {
////            cta.visibility = View.INVISIBLE
////        } else {
////            cta.visibility = View.VISIBLE
////            cta.text = nativeAd.callToAction
////        }
////
////        if (nativeAd.icon == null) {
////            icon.visibility = View.GONE
////        } else {
////            icon.setImageDrawable(nativeAd.icon.drawable)
////            icon.visibility = View.VISIBLE
////        }
////
////        if (nativeAd.price == null) {
////            price.visibility = View.INVISIBLE
////        } else {
////            price.visibility = View.VISIBLE
////            price.text = nativeAd.price
////        }
////
////        if (nativeAd.store == null) {
////            store.visibility = View.INVISIBLE
////        } else {
////            store.visibility = View.VISIBLE
////            store.text = nativeAd.store
////        }
////
////        if (nativeAd.starRating == null) {
////            stars.visibility = View.INVISIBLE
////        } else {
////            stars.rating = nativeAd.starRating!!.toFloat()
////            stars.visibility = View.VISIBLE
////        }
////
////        if (nativeAd.advertiser == null) {
////            advertiser.visibility = View.INVISIBLE
////        } else {
////            advertiser.text = nativeAd.advertiser
////            advertiser.visibility = View.VISIBLE
////        }
////
////        // This method tells the Google Mobile Ads SDK that you have finished populating your
////        // native ad view with this native ad.
////        unifiedNativeAd.setNativeAd(nativeAd)
////
////        // Get the video controller for the ad. One will always be provided, even if the ad doesn't
////        // have a video asset.
////        val vc = nativeAd.videoController
////
////        // Updates the UI to say whether or not this ad has a video asset.
////        if (vc.hasVideoContent()) {
//////            videostatus_text.text = String.format(
//////                Locale.getDefault(),
//////                "Video status: Ad contains a %.2f:1 video asset.",
//////                vc.aspectRatio)
////
////            // Create a new VideoLifecycleCallbacks object and pass it to the VideoController. The
////            // VideoController will call methods on this object when events occur in the video
////            // lifecycle.
////            vc.videoLifecycleCallbacks = object : VideoController.VideoLifecycleCallbacks() {
////                override fun onVideoEnd() {
////                    // Publishers should allow native ads to complete video playback before
////                    // refreshing or replacing them with another ad in the same UI location.
//////                    refresh_button.isEnabled = true
//////                    videostatus_text.text = "Video status: Video playback has ended."
////                    super.onVideoEnd()
////
////                    params.onComplete()
////                }
////            }
////        }
//    }
//}