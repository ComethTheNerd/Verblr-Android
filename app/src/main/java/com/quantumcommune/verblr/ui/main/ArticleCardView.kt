package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.quantumcommune.verblr.*
import com.squareup.picasso.Picasso

data class ArticleCardViewParams(
    val article : DATA_Article,
    val statusFlags : Int = 0,
    val onDetailsClick : () -> Unit,
    val onClick : () -> Unit
);

class ArticleCardView : ItemCardView
{
    companion object {
//        const val TAG_body = FeedItemView.TAG_body
//        const val TAG_artworkContainer = FeedItemView.TAG_artworkContainer
//        const val TAG_artwork = "FeedArticleViewVendor.artwork"
//        const val TAG_title = FeedItemView.TAG_title
//        const val TAG_wordCountContainer = "FeedArticleViewVendor.wordCountContainer"
//        const val TAG_wordCount = "FeedArticleViewVendor.wordCount"
//        const val TAG_statusIconsContainer = "FeedArticleViewVendor.statusIconsContainer"
//        const val TAG_cta = FeedItemView.TAG_cta

        fun newInstance(context : Context?, params : ArticleCardViewParams) : ArticleCardView
        {
            val instance = ArticleCardView(context)
            instance.init(params);
            instance.refresh(params)

            return instance;
        }
    }

    private constructor(context : Context?) : super(context)
    {
    }

    lateinit var artwork : ImageView
    lateinit var wordCountContainer : LinearLayout
    lateinit var wordCount : TextView
    lateinit var statusIconsContainer : LinearLayout
    lateinit var warningIcon : ImageView
    lateinit var spacer : View
    lateinit var availableOfflineIcon : ImageView

    fun init(params : ArticleCardViewParams)
    {
        artwork = VRBLegacyViewFactory.artworkView(context, artworkDim)
//        artwork.tag = TAG_artwork

        wordCountContainer = LinearLayout(context)
        wordCountContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
//        wordCountContainer.gravity = LinearLayout.
        wordCountContainer.orientation = LinearLayout.HORIZONTAL
        wordCountContainer.gravity = Gravity.CENTER_VERTICAL

        val bookIconDim = ViewUtils.toPX(VRBTheme.m12Size)
        val bookIcon = ImageView(context).apply {
            val layoutParams = LinearLayout.LayoutParams(bookIconDim, bookIconDim)
            layoutParams.setMargins(0, 0, 5, 0);
            this.layoutParams = layoutParams;

            this.setImageResource(R.drawable.book_fill_contextualinfo)
        }
        wordCountContainer.addView(bookIcon)

        wordCount = TextView(context)
        wordCount.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m12Size)
        wordCount.typeface = VRBTheme.TYPEFACE_semibold
        wordCount.setLines(1)
        wordCount.ellipsize = TextUtils.TruncateAt.END
        wordCount.setTextColor(VRBTheme.COLOR_contextualInfo)
//        wordCount.tag = TAG_wordCount

        wordCountContainer.addView(wordCount)


        statusIconsContainer = LinearLayout(context)
        statusIconsContainer.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        statusIconsContainer.orientation = LinearLayout.HORIZONTAL
        statusIconsContainer.gravity = Gravity.BOTTOM
//        statusIconsContainer.setBackgroundColor(VRBTheme.COLOR_contextualInfo)

        spacer = View(context)
        spacer.layoutParams = LinearLayout.LayoutParams(
            VRBTheme.smallGutter,
            VRBTheme.articleDetailContextualIconDim
        )

        warningIcon = ImageView(context)
        warningIcon.id = View.generateViewId()
        warningIcon.layoutParams = LinearLayout.LayoutParams(
            VRBTheme.articleDetailContextualIconDim,
            VRBTheme.articleDetailContextualIconDim
        )
        warningIcon.setImageResource(R.drawable.exclamationmark_circle_fill_electro)

        availableOfflineIcon = ImageView(context)
        availableOfflineIcon.id = View.generateViewId()
        availableOfflineIcon.layoutParams = LinearLayout.LayoutParams(
            VRBTheme.articleDetailContextualIconDim,
            VRBTheme.articleDetailContextualIconDim
        )
        availableOfflineIcon.setImageResource(R.drawable.downloaded_electro)

        statusIconsContainer.addView(warningIcon)
        statusIconsContainer.addView(spacer)
        statusIconsContainer.addView(availableOfflineIcon)


        super.init(
            convertToSuperParams(params)
        );
    }

    fun refresh(params : ArticleCardViewParams)
    {
        val article = params.article;

        artwork.setImageResource(R.drawable.blank_article)
        article.artwork.largeArtwork?.let {
                artworkURL -> Picasso.get().load(artworkURL).fit().centerCrop().into(artwork)
        }

        wordCount.text = StringUtils.toKCountLabel(article.content.effectiveWordCount);

        val showingOfflineIcon = updateStatusIconVisibility(params, availableOfflineIcon, ArticleStatusFlag.AVAILABLE_OFFLINE);
        val showingWarningIcon = updateStatusIconVisibility(params, warningIcon, ArticleStatusFlag.ANY_ISSUE);

        warningIcon.isVisible = false
        warningIcon.isGone = true

        val showSpacer = false//showingOfflineIcon && showingWarningIcon
        spacer.isVisible = showSpacer
        spacer.isGone = !showSpacer

        super.refresh(
            convertToSuperParams(params)
        );
    }


    private fun convertToSuperParams(params : ArticleCardViewParams) : ItemCardViewParams {
        val article = params.article

        val refDateUTC = article.details.modified ?: article.details.published ?: article.meta.creationUTC

        return ItemCardViewParams(
            title = StringUtils.toDateLabel(refDateUTC, relative = true),
            body = article.details.title ?: article.source.basisURL,
            artwork = artwork,
            cta = ItemCardViewCTAParams(
                label = "Details",
                action = params.onDetailsClick
            ),
            onClick = params.onClick,
            topContextual = wordCountContainer,
            bottomContextual = statusIconsContainer
        )
    }

    private fun updateStatusIconVisibility(params : ArticleCardViewParams, view : View, flag : ArticleStatusFlag) : Boolean
    {
        val visible = ArticleOps.hasStatusFlagBitsInCommon(params.statusFlags, flag)

        view.isVisible = visible
        view.isGone = !visible

        return visible
    }
}