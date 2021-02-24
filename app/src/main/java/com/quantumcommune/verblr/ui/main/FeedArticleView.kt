package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.view.View
import android.widget.LinearLayout

data class FeedArticleViewParams(
    val article : ArticleCardViewParams
);

class FeedArticleView : LinearLayout
{
    companion object {
        fun newInstance(context : Context?, params : FeedArticleViewParams) : FeedArticleView
        {
            val instance = FeedArticleView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
        id = View.generateViewId()
    }

    lateinit var article : ArticleCardView

    fun init(params : FeedArticleViewParams)
    {
        article = ArticleCardView.newInstance(context, params.article)

        article.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        addView(article)
    }

    fun refresh(params : FeedArticleViewParams) {
        article.refresh(params.article)
    }
}