//package com.quantumcommune.verblr.ui.main
//
//import android.graphics.Color
//import android.os.Bundle
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.FrameLayout
//import android.widget.LinearLayout
//import android.widget.TextView
//
//class StoreFragment : VRBScreen() {
//
//    companion object {
//        fun newInstance() = StoreFragment()
//    }
//
//    private lateinit var textView : TextView
//
//    private lateinit var miniPlayerView : View
//    private lateinit var maxiPlayerView : View
//
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
//                              savedInstanceState: Bundle?): View {
//
//        var linearLayout = LinearLayout(this.context)
//        linearLayout.layoutParams = ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
//        linearLayout.orientation = LinearLayout.VERTICAL
//
//
//        textView = TextView(this.context)
//        textView.text = "Store";
//
//        linearLayout.addView(textView)
//
//        val button = Button(this.context)
//        button.text = "Click me to go home"
//
//        button.setOnClickListener {
//            screens?.clear()
//            overlays?.clear()
//        }
//
//        linearLayout.addView(button)
//
//        val playerArticle = viewModel.player_article.value
//
//        maxiPlayerView = android.widget.TextView(context)?.apply {
//            this.text = "I am maxiplayer"
//        }
//        miniPlayerView = XXXXXVRBView.miniPlayerView(context, playerArticle)
//
//
//        linearLayout.setBackgroundColor(Color.parseColor("#00FF00"))
//
//
//        return linearLayout
//    }
//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
////        bindMiniPlayerViewToPlayerUpdates(miniPlayerView, maxiPlayerView)
//    }
//
//}