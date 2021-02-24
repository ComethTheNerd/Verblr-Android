//package com.quantumcommune.verblr.ui.main
//
//import android.content.DialogInterface
//import android.os.Bundle
//import android.view.View
//import android.view.ViewStub
//import android.widget.FrameLayout
//import android.widget.LinearLayout
//import com.google.android.gms.ads.formats.UnifiedNativeAd
//import com.google.android.material.bottomsheet.BottomSheetBehavior
//
//
///**
// * [dho] TODO
// *
// *  - be able to navigate the underlying view
// *  - display the PlayerFragment via activity and view model rather than directly
// *  - move mini player ui to this fragment
// *  - remove bind mini player events from VRBFragment
// *
// * 06/06/20
// *
// */
//
//
///*
//*
//*
//*
//* */
//
//data class AdSupportedPlayerOverlayParams(
//    val showAdAndThen : (() -> Unit)? = null
//)
//
//
//class AdSupportedPlayerOverlay(val params : AdSupportedPlayerOverlayParams) : VRBOverlay() {
//
//    companion object {
//        fun newInstance(params: AdSupportedPlayerOverlayParams = AdSupportedPlayerOverlayParams()) = AdSupportedPlayerOverlay(params)
//    }
//
//    private var loadedAd : UnifiedNativeAd? = null
//
//    enum class State
//    {
//        INIT,
////        LOADING_AD,
//        SHOW_AD,
//        SHOW_PLAYER
//    }
//
//    private var currentState = State.INIT
//        set(value) {
//            field = value
//            requestContentChange()
//        }
//
//
//    override fun onCreateContentView(): View? {
//
//        val view : View? = when(currentState)
//        {
//            State.INIT -> {
//                if(params.showAdAndThen != null)
//                {
//                    ads.getNextAdForPlacement(ads.PlacementID_Overlay)
//                    { it ->
//
//                        if(!isHidden)
//                        {
//                            loadedAd = it
//
//                            if(loadedAd != null)
//                            {
//                                currentState = State.SHOW_AD
//                            }
//                            else
//                            {
//                                currentState = State.SHOW_PLAYER
//                            }
//                        }
//                    }
//                }
//                else
//                {
//                    currentState = State.SHOW_PLAYER
//                }
//
//                ViewStub(context)
//            }
//            State.SHOW_AD -> {
//                val a = loadedAd as? UnifiedNativeAd
//                if(a != null)
//                {
//                    val params = OverlayAdViewParams(a)
//                    {
//                        if(!isHidden)
//                        {
//                            currentState = State.SHOW_PLAYER
//
//                            params.showAdAndThen?.invoke()
//                        }
//                    }
//
//                    OverlayAdView.newInstance(context, params)
//                }
//                else
//                {
//                    currentState = State.SHOW_PLAYER
//
//                    params.showAdAndThen?.invoke()
//
//                    null
//                }
//            }
//            else -> {
//                val params = OverlayPlayerViewParams(x = 2)
//
//                OverlayPlayerView.newInstance(context, params)
//            }
//        }
//
//        view?.layoutParams = LinearLayout.LayoutParams(
//            FrameLayout.LayoutParams.MATCH_PARENT,
//            FrameLayout.LayoutParams.MATCH_PARENT
//        )
//
//        view?.setBackgroundColor(VRBTheme.COLOR_brandElectro)
//
//        return view
//    }
//
//
//    override fun onStateChanged(state: Int) {
//        super.onStateChanged(state)
//
//        if(state == BottomSheetBehavior.STATE_HIDDEN)
//        {
//            cleanupAd()
//        }
//    }
//
//    override fun onCancel(dialog: DialogInterface?) {
//        super.onCancel(dialog)
//
//        cleanupAd()
//    }
//
//    private fun cleanupAd()
//    {
//        try {
//            loadedAd?.videoController?.stop()
//        }
//        catch(err : Exception )
//        {
//
//        }
//
//        loadedAd = null
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//    }
//
//}