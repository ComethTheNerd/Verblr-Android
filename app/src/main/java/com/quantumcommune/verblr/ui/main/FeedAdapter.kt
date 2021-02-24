package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.BaseAdapter
import com.quantumcommune.verblr.AdVendor

class FeedAdapter(private val context : Context?,
                  private val ads : AdVendor,
                  private val params : FeedViewParams
) : BaseAdapter()
{
    private val ids = params.items.associateBy({ it.id }, { nextID() })

    companion object {
        private var idSeed : Long = 0

        @Synchronized fun nextID() : Long = ++idSeed
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val item = params.items[position]

        val view = if(item.kind == FeedItemKind.AD)
        {
            // [dho] prebaked to try and minimize the lag of loading them - 19/06/20
            item.params as? FeedAdView
        }
        else if(item.kind == FeedItemKind.ARTICLE)
        {
            val params = FeedArticleViewParams(article = item.params as ArticleCardViewParams)

            when(convertView)
            {
                is FeedArticleView -> {
                    convertView.refresh(params)
                    convertView
                }
                else -> FeedArticleView.newInstance(context, params)
            }
        }
        else
        {
//            throw Exception("Unsupported feed item kind '${item.kind.name}'")
            null
        }

        if(view != null)
        {
            val paddingTop = if(position > 0) VRBTheme.smallGutter else 0

            view.setPadding(0, paddingTop,0,0)

            return view;
        }
        else
        {
            return ViewStub(context)
        }
    }

    override fun getItem(position: Int): Any = params.items[position]

    override fun getItemId(position: Int): Long =
        ids[params.items[position].id] ?: error("ID could not be retrieved from library feed")

    override fun getCount(): Int = params.items.count()
}