package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import com.quantumcommune.verblr.R

class VRBLegacyViewFactory {
    private constructor()
    companion object {
        fun headerIconButtonView(
            context : Context?,
            disabled : Boolean = false,
            iconDrawable : Int,
            action : (() -> Unit)? = null
        ) : View
        {
            val iconDim = VRBTheme.headerViewIconDim

            val button = ImageView(context)
            button.layoutParams = LinearLayout.LayoutParams(iconDim, iconDim)

            button.setImageResource(iconDrawable)
//           Picasso.get().load(iconDrawable).into(button)


            button.isEnabled = !disabled

            if(action != null)
            {
                button.setOnClickListener { action() }
            }

            return button
        }

        fun artworkView(context : Context?, artworkDim : Int) : ImageView {
            val artwork = ImageView(context)
            artwork.layoutParams = LinearLayout.LayoutParams(artworkDim, artworkDim)

            artwork.setImageResource(R.drawable.blank_article)

            return artwork;
        }
    }
}

