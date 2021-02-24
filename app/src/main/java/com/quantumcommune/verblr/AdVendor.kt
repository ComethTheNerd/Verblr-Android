package com.quantumcommune.verblr

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.formats.NativeAdOptions
import com.google.android.gms.ads.formats.UnifiedNativeAd
import java.util.*


val ONE_HOUR_MS = 3600000L

class AdVendor(val context : Context) {

    val mLock = Object()

    inner class CacheEntry(
        val placementID : String,
        val idx : Int,
        val ads : List<CacheEntry_AdInfo>
    )

    inner class CacheEntry_AdInfo(
        val timestamp : Long,
        val ad : UnifiedNativeAd
    )

    val cache = mutableMapOf<String, CacheEntry>()

    private val PlacementID_DEBUG = "ca-app-pub-3940256099942544/2247696110"

    // [dho] https://developers.google.com/admob/android/test-ads
    private fun getPlacementID(id : Int) =
        if(BuildConfig.DEBUG) "ca-app-pub-3940256099942544/2247696110" else context.getString(id)

    val PlacementID_Feed = getPlacementID(R.string.ad_feed_placement_id)
    val PlacementID_Gateway = getPlacementID(R.string.ad_gateway_placement_id)
    val PlacementID_Overlay = getPlacementID(R.string.ad_overlay_placement_id)

    val FetchMoreAdsBatchSize = 3;

    val MaxAdsPerRequest = 5

    val placementIDs = mapOf<String, Int>(
        PlacementID_Feed to MaxAdsPerRequest,
        PlacementID_Gateway to MaxAdsPerRequest,
        PlacementID_Overlay to MaxAdsPerRequest
    )

    init
    {
        var reqConfigBuilder = RequestConfiguration.Builder()

        if (BuildConfig.DEBUG) {
            val testDevices = listOf(AdRequest.DEVICE_ID_EMULATOR)

            reqConfigBuilder = reqConfigBuilder.setTestDeviceIds(testDevices)
        }

        MobileAds.setRequestConfiguration(reqConfigBuilder.build())
    }

    fun populateCache(completion: (err: Exception?) -> Unit)
    {
        placementIDs.forEach {
            loadFreshAds(placementID = it.key, count = it.value)
            {
                nativeAds, err ->

                if(err != null)
                {
                    Log.e("Ads.populateCache", "Exception for '${it.key} : ${err.localizedMessage}");
                }

                val now = Calendar.getInstance().timeInMillis
                val ads = nativeAds?.map { ad -> CacheEntry_AdInfo(timestamp = now, ad = ad) } ?: listOf()

                synchronized(mLock)
                {
                    cache[it.key] = CacheEntry(placementID = it.key, idx = 0, ads = ads)
                }

                if(cache.keys.count() == placementIDs.count())
                {
                    completion(null)
                }
            }
        }
    }

    private fun loadFreshAds(placementID : String, count : Int, completion : (ads : List<UnifiedNativeAd>?, err : Exception?) -> Unit) {

        val reqs = Math.ceil((count / MaxAdsPerRequest.toDouble())).toInt()

        if(reqs == 1)
        {
            return loadFreshAdsHelper(placementID, count, completion);
        }

        var responses = 0
        val lock = Object()

        val resultAds = mutableListOf<UnifiedNativeAd>()
        var resultErr : Exception? = null

        var totalAdsRequested = 0

        for(i in 0 until reqs)
        {
            val remainingToFetch = count - totalAdsRequested
            val amountToFetch = if(remainingToFetch > MaxAdsPerRequest) MaxAdsPerRequest else remainingToFetch

            totalAdsRequested += amountToFetch

            loadFreshAdsHelper(placementID, amountToFetch)
            {
                ads, err ->
                synchronized(lock)
                {
                    if(ads != null)
                    {
                        resultAds.addAll(ads)
                    }

                    if(err != null)
                    {
                        resultErr = err
                    }

                    if(++responses == reqs)
                    {
                        completion(resultAds, resultErr)
                    }
                }
            }
        }

    }

    private fun loadFreshAdsHelper(placementID : String, count : Int, completion : (ads : List<UnifiedNativeAd>?, err : Exception?) -> Unit)
    {
        // [dho] "Make all calls to the Mobile Ads SDK on the main thread." from https://developers.google.com/admob/android/native/start?hl=en-US#build_an_adloader - 15/06/20
        ThreadUtils.ensureMainThreadExec {
            var err : Exception? = null
            var ads = mutableListOf<UnifiedNativeAd>()

            val videoOptions = VideoOptions.Builder()
                .setStartMuted(true)
                .build()

            val nativeAdOptions = NativeAdOptions.Builder()
                // [dho] showing only square adverts so they fit with the album artwork aesthetic - 15/06/20
                .setMediaAspectRatio(NativeAdOptions.NATIVE_MEDIA_ASPECT_RATIO_SQUARE)
                .setVideoOptions(videoOptions)
                .build()

            // [dho] adapted from : https://developers.google.com/android/reference/com/google/android/gms/ads/AdListener - 15/06/20
            val adListener = object: com.google.android.gms.ads.AdListener() {
                override fun onAdClosed() {}

                override fun onAdFailedToLoad(errorCode: Int) {
                    // [dho] adapted from : https://developers.google.com/android/reference/com/google/android/gms/ads/AdListener#onAdFailedToLoad(int) - 15/06/20
                    val err = when(errorCode)
                    {
                        AdRequest.ERROR_CODE_INTERNAL_ERROR -> Exception("Internal error")
                        AdRequest.ERROR_CODE_INVALID_REQUEST -> Exception("Invalid request")
                        AdRequest.ERROR_CODE_NETWORK_ERROR -> Exception("Network error")
                        AdRequest.ERROR_CODE_NO_FILL -> Exception("No fill")
                        else -> Exception("Something went wrong")
                    }

                    completion(null, err);
                }

                override fun onAdLeftApplication() {}

                override fun onAdOpened() {}

                override fun onAdLoaded() {}

                override fun onAdClicked() {}

                override fun onAdImpression() {}
            }

            var adLoader : AdLoader? = null;

            adLoader = AdLoader.Builder(context, placementID)
                .forUnifiedNativeAd { ad : UnifiedNativeAd ->

                    ads.add(ad)

                    if (adLoader?.isLoading == true)
                    {
                        // The AdLoader is still loading ads.
                        // Expect more adLoaded or onAdFailedToLoad callbacks.
                    }
                    else
                    {
                        // The AdLoader has finished loading ads.
                        completion(ads, err)
                    }
                }
                .withAdListener(adListener)
                .withNativeAdOptions(nativeAdOptions)
                .build()

            val adRequest = AdRequest.Builder()
                .build()

            adLoader.loadAds(adRequest, count)
        }
    }

    fun getNextAdForPlacement(placementID: String) : UnifiedNativeAd?
    {
        return _getNextAdForPlacementHelper(placementID)?.ad
//        while(true)
//        {
//            val adInfo = _getNextAdForPlacementHelper(placementID)
//
//            if(adInfo != null)
//            {
//                val now = Calendar.getInstance().timeInMillis
//
//                if((now - adInfo.timestamp) >= ONE_HOUR_MS)
//                {
//                    doneWithAd(placementID, adInfo.ad);
//                }
//            }
//            else
//            {
//                break;
//            }
//        }
    }

    private fun _getNextAdForPlacementHelper(placementID: String) : CacheEntry_AdInfo?
    {
        var shouldLoadMoreAds = false
        var nextAdInfo : CacheEntry_AdInfo? = null;

        synchronized(mLock)
        {
            val entry = cache[placementID]

            if(entry?.ads?.isNotEmpty() == true) {
                val nextIdx = (entry.idx + 1) % entry.ads.count()
                nextAdInfo = entry.ads[nextIdx];

                val remaining = entry.ads.count() - (nextIdx + 1)
                shouldLoadMoreAds = remaining < FetchMoreAdsBatchSize + 1

                cache[placementID] = CacheEntry(placementID, nextIdx, entry.ads);
            }
        }

        if(shouldLoadMoreAds)
        {
            ThreadUtils.bgThreadExec {
                setTimeout({
                    loadFreshAds(placementID, FetchMoreAdsBatchSize)
                    {
                            nativeAds, err ->
                        if(nativeAds != null)
                        {
                            synchronized(mLock)
                            {
                                val entry = cache[placementID]!!;

                                val newAds = entry.ads.toMutableList()

                                val now = Calendar.getInstance().timeInMillis

                                newAds.addAll(
                                    nativeAds.map { ad -> CacheEntry_AdInfo(timestamp = now, ad = ad) }
                                )

                                cache[placementID] = CacheEntry(placementID, entry.idx, newAds);
                            }
                        }
                    }
                }, 0)
            }
        }

        return nextAdInfo
    }

    fun doneWithAd(placementID : String, ad : UnifiedNativeAd)
    {
        synchronized(mLock)
        {
            val entry = cache[placementID]

            if(entry != null)
            {
                val adIdx = entry.ads.indexOfFirst { it.ad == ad }

                if(adIdx > -1)
                {
                    val newIdx = if(adIdx < entry.idx) entry.idx - 1 else entry.idx
                    val newAds = entry.ads.filter { it.ad != ad }

                    cache[placementID] = CacheEntry(placementID, newIdx, newAds);
                }
            }
        }

        ad.destroy()
    }

    fun destroy() {
        synchronized(mLock)
        {
            cache.entries.forEach {
//                cache.remove(it.key) <- ConcurrentModificationException
                it.value.ads.forEach { adInfo -> adInfo.ad.destroy() }
            }
        }
    }
}