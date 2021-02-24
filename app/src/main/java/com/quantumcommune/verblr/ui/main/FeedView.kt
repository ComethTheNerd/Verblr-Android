package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout
import android.widget.ListView
import com.quantumcommune.verblr.AdVendor


data class FeedViewParams(
    val horizontalPadding : Int = 0,
    val verticalPadding : Int = 0,
    val items : List<FeedItemParams<*>>,
    val emptyView : View? = null
);

data class FeedItemParams<T>(
    val id : String,
    val kind : FeedItemKind,
//    val hasAdBefore : Boolean = false,
    val ad : AdCardView? = null,
    val params : T
)

class FeedView : LinearLayout
{
    companion object {
//        val TAG_list = "FeedViewVendor.list"

        fun newInstance(context : Context?, ads : AdVendor, params : FeedViewParams) : FeedView
        {
            val instance = FeedView(context, ads)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?, ads : AdVendor) : super(context)
    {
        id = View.generateViewId()

        this.ads = ads
    }

    val ads : AdVendor

    lateinit var list : ListView
    var emptyView : View? = null

    fun init(params : FeedViewParams)
    {
//        val marginHorizontal = VRBTheme.smallGutter
//        val marginVertical = VRBTheme.smallGutter
//        val dividerHeight = VRBTheme.smallGutter

        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )

        list = ListView(context);

        list.isVerticalScrollBarEnabled = false
        list.isHorizontalScrollBarEnabled = false

        val listLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        listLayoutParams.setMargins(0,0,0,0)
        list.layoutParams = listLayoutParams

        list.divider = VRBTheme.GRAD_transparent

        // [dho] NOTE now applying the divider as padding to items directly
        // in the `FeedAdapter` so we do not get weird gaps if a view cannot be displayed
        // at that moment (eg. an ad placement has not loaded) - 27/06/20
//        list.dividerHeight = dividerHeight
        addView(list)
    }

    fun refresh(params : FeedViewParams)
    {
        if(emptyView != null && emptyView != params.emptyView)
        {
            removeView(emptyView)
            list.emptyView = null
        }

        if(params.emptyView != null)
        {
            emptyView = params.emptyView
            addView(emptyView)
            list.emptyView = params.emptyView
        }

        // [dho] adapted from : https://stackoverflow.com/a/46710968/300037 - 22/06/20
        list.clipToPadding = false
        list.setPadding(
            params.horizontalPadding,
            params.verticalPadding,
            params.horizontalPadding,
            params.verticalPadding
        )

//        list.removeAllViews() // [dho] NOT SUPPORTED! - 16/06/20
        list.adapter = FeedAdapter(context, ads, params);

    }
}