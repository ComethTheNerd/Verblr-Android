package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import com.google.android.gms.ads.formats.UnifiedNativeAd

data class FeedAdViewParams(
    val ad : UnifiedNativeAd
);

class FeedAdView : LinearLayout
{
    companion object {
        fun newInstance(context : Context?, params : FeedAdViewParams) : FeedAdView
        {
            val instance = FeedAdView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var adCard : AdCardView

    fun init(params : FeedAdViewParams)
    {
        adCard = AdCardView.newInstance(context, convertToAdCardViewParams(params))

        addView(adCard)
    }

    fun refresh(params : FeedAdViewParams) {
        adCard.refresh(convertToAdCardViewParams(params))
    }

    private fun convertToAdCardViewParams(params : FeedAdViewParams) =
        AdCardViewParams(ad = params.ad)
}