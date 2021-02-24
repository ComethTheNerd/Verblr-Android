package com.quantumcommune.verblr.ui.main

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import com.quantumcommune.verblr.R
import com.squareup.picasso.Picasso

class SplashScreen : Fragment() {

    companion object {
        fun newInstance() = SplashScreen()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        var linearLayout = LinearLayout(this.context)
        linearLayout.layoutParams = ViewGroup.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)
        linearLayout.gravity = Gravity.CENTER

        val logoDim = ViewUtils.toPX(80F)

        val logo = ImageView(context)
        logo.layoutParams = LinearLayout.LayoutParams(logoDim, logoDim)

        Picasso.get().load(R.drawable.splash).into(logo);

        linearLayout.addView(logo)

        linearLayout.setBackgroundColor(VRBTheme.COLOR_toolbarBG)

        return linearLayout
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }
}