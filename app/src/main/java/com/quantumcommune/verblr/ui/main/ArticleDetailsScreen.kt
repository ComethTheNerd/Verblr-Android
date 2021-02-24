package com.quantumcommune.verblr.ui.main

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ScrollView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.app.ShareCompat
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.quantumcommune.verblr.*
import com.squareup.picasso.Picasso
import java.util.*

class ArticleDetailsScreen(val article : DATA_Article) : VRBScreen() {

    companion object {
        fun newInstance(article: DATA_Article) = ArticleDetailsScreen(article)
    }

    lateinit var standardScreen : StandardScreenView
    lateinit var topButton : TheButton

    val refURL = article.source.canonicalURL ?: article.source.contentURL
    val refDateUTC = article.details.modified ?: article.details.published ?: article.meta.creationUTC

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {


        var title : View? = null;

        val refDateLabel = StringUtils.toDateLabel(refDateUTC)


        if(refDateLabel != null)
        {
            title = TextView(context).apply {
                this.id = View.generateViewId()
                this.setLines(1)
                this.ellipsize = TextUtils.TruncateAt.END
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m15Size)
                this.typeface = VRBTheme.TYPEFACE_semibold
                this.setTextColor(VRBTheme.COLOR_contextualInfo)
                this.text = refDateLabel.toUpperCase(Locale.ROOT)
            }
        }

        val toolbarParams = ToolbarViewParams(
            leading = VRBLegacyViewFactory.headerIconButtonView(
                context,
                iconDrawable = R.drawable.chevron_left_subwhite
            )
            {
                screens?.back()
            },
            title = title,
            trailing = VRBLegacyViewFactory.headerIconButtonView(
                context,
                iconDrawable = R.drawable.share_subwhite
            )
            {
                val encodedArticleURL = StringUtils.urlEncoded(refURL);

                if(encodedArticleURL != null)
                {
                    val shareTitle = article.details.title ?: ""
                    val shareURL = "${getString(R.string.deep_link_protocol)}://${getString(R.string.deep_link_host)}${getString(R.string.deep_link_path)}?${getString(R.string.deep_link_param_name_action)}=${getString(R.string.deep_link_action_name_add)}&${getString(R.string.deep_link_param_name_url)}=${encodedArticleURL}";

                    // [dho] adapted from : https://stackoverflow.com/a/46394633/300037 - 17/06/20
                    ShareCompat.IntentBuilder.from(activity)
                        .setType("text/plain")
                        .setChooserTitle(shareTitle)
                        .setText(shareURL)
                        .startChooser();
                }
                else
                {
                    dialog?.alert(message = "Could not create share link");
                }
            }
        )

        val t = topView()
        t.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_contentAccent
        ))
        divider.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
            HorizontalDividerView.DividerHeight
        )

        val b = bottomView()
        b.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        )

        val content = ConstraintLayout(context)

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(t)
        content.addView(divider)
        content.addView(b)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(t.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(t.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(t.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
//        constraints.setMargin(t.id, ConstraintSet.TOP, VRBTheme.gutter / 2)
//        constraints.setMargin(t.id, ConstraintSet.START, VRBTheme.gutter)
//        constraints.setMargin(t.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(divider.id, ConstraintSet.TOP, t.id, ConstraintSet.BOTTOM)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider.id, ConstraintSet.TOP, VRBTheme.maxiPlayerGutter)

        constraints.connect(b.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(b.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(b.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(b.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
//        constraints.setMargin(b.id, ConstraintSet.TOP, VRBTheme.gutter)
//        constraints.setMargin(b.id, ConstraintSet.START, VRBTheme.gutter)
//        constraints.setMargin(b.id, ConstraintSet.BOTTOM, VRBTheme.gutter)
//        constraints.setMargin(b.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.applyTo(content);

        val statusBarParams = (
            inferOverrideStatusBarParams() ?:
            inferStatusBarParams(viewModel.user_membership.value, viewModel.nav_statuses.value)
        )

        val miniPlayerParams = inferMiniPlayerParams(
            article = viewModel.player_article.value,
            state = viewModel.player_state.value,
            progress = viewModel.player_progress.value,
            buffState = viewModel.player_buff_state.value,
            buffProgress = viewModel.player_buff_progress.value
        );

        val standardScreenParams = StandardScreenViewParams(
            toolbarParams,
            content,
            statusBarParams,
            miniPlayerParams
        )

        standardScreen = StandardScreenView.newInstance(context, standardScreenParams)

        return standardScreen
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bindPresenceUpdates()
        bindMiniPlayerToStateUpdates(standardScreen.miniPlayer)
        bindTopButtonUpdates(topButton)


        val statusBar = standardScreen.statusBar
        val params = inferOverrideStatusBarParams()

        if(params != null)
        {
            statusBar.refresh(params)
        }
        else
        {
            bindStatusBarToStateUpdates(statusBar)
        }
    }

    // [dho] allows any article related status to take precedence on this screen
    // over what is actually in the statuses stack - 26/06/20
    private fun inferOverrideStatusBarParams() : StatusBarViewParams?
    {
        val overrideStatus = StatusUtils(context, dialog, overlays).inferArticleStatus(article)

        return if(overrideStatus != null)
        {
            StatusBarViewParams(
                icon = overrideStatus.icon ?: defaultStatusIcon,
                label = overrideStatus.label,
                action = overrideStatus.action
            )
        }
        else
        {
            null
        }
    }

    private fun topView() : View
    {
        val artwork = ImageView(context)
        artwork.id = View.generateViewId()
        artwork.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.articleDetailArtworkDim,
            VRBTheme.articleDetailArtworkDim
        )

        val title = TextView(context)
        title.id = View.generateViewId()
        title.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )
        title.textAlignment = View.TEXT_ALIGNMENT_CENTER
        title.maxLines = 3
        title.ellipsize = TextUtils.TruncateAt.END
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m16Size)
        title.setTextColor(VRBTheme.COLOR_fontTitleMain)
        title.typeface = VRBTheme.TYPEFACE_semibold

        topButton = TheButton.newInstance(context, inferTopButtonParams(
            creditChecking = isCreditChecking(viewModel.creditCheck_article.value),
            lap = getLAP(viewModel.user_lastAccessed.value)
        ))
        topButton.minWidth = VRBTheme.articleDetailArtworkDim

        val content = ConstraintLayout(context)
        content.id = View.generateViewId()

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        )

        content.addView(artwork)
        content.addView(title)
        content.addView(topButton)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(artwork.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(artwork.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(artwork.id, ConstraintSet.TOP, VRBTheme.gutter / 2)
        constraints.setMargin(artwork.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(artwork.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(title.id, ConstraintSet.TOP, artwork.id, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(title.id, ConstraintSet.PARENT_ID)
        constraints.connect(title.id, ConstraintSet.BOTTOM, topButton.id, ConstraintSet.TOP)
        constraints.setMargin(title.id, ConstraintSet.TOP, VRBTheme.gutter + VRBTheme.smallGutter)
        constraints.setMargin(title.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(title.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(topButton.id, ConstraintSet.TOP, title.id, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(topButton.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(topButton.id, ConstraintSet.TOP, VRBTheme.maxiPlayerGutter)
        constraints.setMargin(topButton.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(topButton.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.applyTo(content);

        content.setBackgroundColor(VRBTheme.COLOR_toolbarBG)




        val artworkURL = article?.artwork?.largeArtwork ?: article?.artwork?.standardArtwork ?: null;

        if(artworkURL != null)
        {
            Picasso.get().load(artworkURL).fit().centerCrop().into(artwork)
        }
        else
        {
            Picasso.get().load(R.drawable.blank_article).into(artwork)
        }

        title.text = article.details.title ?: article.source.basisURL

        return content;
    }

    private fun bottomView() : View
    {
        val descriptionHeading = TextView(context)
        descriptionHeading.id = View.generateViewId()
        descriptionHeading.maxLines = 1
        descriptionHeading.ellipsize = TextUtils.TruncateAt.END
        descriptionHeading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageHeadingFontSize)
        descriptionHeading.setTextColor(VRBTheme.COLOR_fontBody)
        descriptionHeading.text = "Description"
        descriptionHeading.typeface = VRBTheme.TYPEFACE_semibold

        val descriptionBody = TextView(context)
        descriptionBody.id = View.generateViewId()
        descriptionBody.maxLines = 8
        descriptionBody.ellipsize = TextUtils.TruncateAt.END
        descriptionBody.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageBodyFontSize)
        descriptionBody.typeface = VRBTheme.TYPEFACE_regular

        if(article.details.description != null)
        {
            descriptionBody.text = article.details.description
            descriptionBody.setTextColor(VRBTheme.COLOR_fontBody)
        }
        else
        {
            descriptionBody.text = "Not found"
            descriptionBody.setTextColor(VRBTheme.COLOR_contextualInfo)
        }


//        val sourceIconDim = ViewUtils.toXScaledPX(20)
//        val sourceIcon = ImageView(context)
//        sourceIcon.id = View.generateViewId()
//        sourceIcon.layoutParams = ConstraintLayout.LayoutParams(
//            sourceIconDim,
//            sourceIconDim
//        )
//        sourceIcon.setImageResource(R.drawable.link_electro)
//        sourceIcon.setOnClickListener { openURL(activity as Activity, refURL) }


        val sourceHeading = TextView(context)
        sourceHeading.id = View.generateViewId()
        sourceHeading.maxLines = 1
        sourceHeading.ellipsize = TextUtils.TruncateAt.END
        sourceHeading.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        sourceHeading.setTextColor(VRBTheme.COLOR_fontBody)
        sourceHeading.text = "source"
        sourceHeading.typeface = VRBTheme.TYPEFACE_semibold

        val sourceLink = TextView(context)
        sourceLink.id = View.generateViewId()
        sourceLink.maxLines = 1
        sourceLink.ellipsize = TextUtils.TruncateAt.END
        sourceLink.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        sourceLink.setTextColor(VRBTheme.COLOR_brandElectro)
        sourceLink.text = StringUtils.ellipsize(refURL, VRBTheme.detailsLinkMaxCharCount)
        sourceLink.setOnClickListener { openURL(activity as Activity, refURL) }
        sourceLink.typeface = VRBTheme.TYPEFACE_regular


        val divider1 = HorizontalDividerView.newInstance(context,
            HorizontalDividerViewParams(VRBTheme.COLOR_toolbarAccent)
        )
        divider1.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val authorAndOrganization = TextView(context)
        authorAndOrganization.id = View.generateViewId()
        authorAndOrganization.maxLines = 1
        authorAndOrganization.ellipsize = TextUtils.TruncateAt.END
        authorAndOrganization.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        authorAndOrganization.setTextColor(VRBTheme.COLOR_contextualInfo)
        authorAndOrganization.typeface = VRBTheme.TYPEFACE_semibold

        val authorName = article.details.author?.name
        val organizationName = article.details.organization?.name

        authorAndOrganization.text = (
            if(authorName?.isNotEmpty() == true)
            {
                if(organizationName?.isNotEmpty() == true &&
                        authorName.toLowerCase(Locale.ROOT) != organizationName.toLowerCase(Locale.ROOT))
                {
                    "$authorName ($organizationName)"
                }
                else
                {
                    authorName
                }
            }
            else if(organizationName?.isNotEmpty() == true)
            {
                organizationName
            }
            else
            {
                "respective authors"
            }
        )

        val copyrightYear = DateTimeUtils.getYear(refDateUTC)
        val copyrightYearLabel = if(copyrightYear != null) " $copyrightYear" else ""
        val copyright = TextView(context)
        copyright.id = View.generateViewId()
        copyright.maxLines = 1
        copyright.ellipsize = TextUtils.TruncateAt.END
        copyright.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        copyright.setTextColor(VRBTheme.COLOR_contextualInfo)
        copyright.text = "all rights reserved$copyrightYearLabel©"
        copyright.typeface = VRBTheme.TYPEFACE_regular

        val divider2 = HorizontalDividerView.newInstance(context,
            HorizontalDividerViewParams(VRBTheme.COLOR_toolbarAccent)
        )
        divider2.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val wordCountIcon = ImageView(context)
        wordCountIcon.id = View.generateViewId()
        wordCountIcon.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.contextualIconDim,
            VRBTheme.contextualIconDim
        )
        wordCountIcon.setImageResource(R.drawable.book_fill_contextualinfo)

        val kWordCount = StringUtils.toKCountLabel(article.content.effectiveWordCount)
        val wordCountLabel = TextView(context)
        wordCountLabel.id = View.generateViewId()
        wordCountLabel.maxLines = 1
        wordCountLabel.ellipsize = TextUtils.TruncateAt.END
        wordCountLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        wordCountLabel.setTextColor(VRBTheme.COLOR_fontBody)
        wordCountLabel.text = "$kWordCount words"
        wordCountLabel.typeface = VRBTheme.TYPEFACE_semibold


        val membership = viewModel.user_membership.value

        var bottomEndIcon = ImageView(context)
        bottomEndIcon.id = View.generateViewId()
        bottomEndIcon.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.contextualIconDim,
            VRBTheme.contextualIconDim
        )

        val bottomEndLabel = TextView(context)
        bottomEndLabel.id = View.generateViewId()
        bottomEndLabel.maxLines = 1
        bottomEndLabel.ellipsize = TextUtils.TruncateAt.END
        bottomEndLabel.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.detailPageMetaFontSize)
        bottomEndLabel.typeface = VRBTheme.TYPEFACE_semibold

        val isAvailableOffline = articleOps.isAvailableOffline(membership, article)

        if(isAvailableOffline)
        {
            bottomEndIcon.setImageResource(R.drawable.downloaded_contextualinfo)
            bottomEndLabel.text = "available offline"
            bottomEndLabel.setTextColor(VRBTheme.COLOR_fontBody)
        }
        else if (viewModel.show_upsells.value == true)
        {
            bottomEndIcon.setImageResource(R.drawable.star_fill_electro)

            bottomEndLabel.text = "want to listen offline?"
            bottomEndLabel.setTextColor(VRBTheme.COLOR_brandElectro)
            bottomEndLabel.setOnClickListener {
                overlays.push(
                    NavItem(
                        key = OverlayNav.KEY_upsellViewNavKey,
                        item = UpsellOverlay.newInstance()
                    ),
                    // [dho] TODO look into bug where sliding down an overlay
                    // displays the previous overlay in the stack that was slid
                    // down before... seems they are not being completely removed when
                    // slid down - 07/07/20
                    removeAllExisting = overlays::anyMatcher//upsellOverlayMatcher
                )
            }
        }
        else
        {
            bottomEndIcon.isVisible = false
            bottomEndLabel.isVisible = false
        }

        val content = ConstraintLayout(context)
        content.id = View.generateViewId()

//        content.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
//        )

        content.addView(descriptionHeading)
        content.addView(descriptionBody)
//        content.addView(sourceIcon)
        content.addView(sourceHeading)
        content.addView(sourceLink)
        content.addView(divider1)
        content.addView(authorAndOrganization)
        content.addView(copyright)
        content.addView(divider2)
        content.addView(wordCountIcon)
        content.addView(wordCountLabel)
        content.addView(bottomEndIcon)
        content.addView(bottomEndLabel)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(descriptionHeading.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(descriptionHeading.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        constraints.connect(descriptionBody.id, ConstraintSet.TOP, descriptionHeading.id, ConstraintSet.BOTTOM)
        constraints.connect(descriptionBody.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.setMargin(descriptionBody.id, ConstraintSet.TOP, VRBTheme.smallGutter)

        constraints.connect(sourceHeading.id, ConstraintSet.TOP, descriptionBody.id, ConstraintSet.BOTTOM)
        constraints.connect(sourceHeading.id, ConstraintSet.START, /*sourceIcon.id*/ ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.setMargin(sourceHeading.id, ConstraintSet.TOP, VRBTheme.gutter)
//
//        constraints.connect(sourceIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
//        constraints.connect(sourceIcon.id, ConstraintSet.TOP, sourceHeading.id, ConstraintSet.TOP)

        constraints.connect(sourceLink.id, ConstraintSet.TOP, sourceHeading.id, ConstraintSet.BOTTOM)
        constraints.connect(sourceLink.id, ConstraintSet.START, /*sourceIcon.id*/ ConstraintSet.PARENT_ID, ConstraintSet.START)

        constraints.connect(divider1.id, ConstraintSet.TOP, sourceLink.id, ConstraintSet.BOTTOM)
        constraints.connect(divider1.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider1.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider1.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.connect(authorAndOrganization.id, ConstraintSet.TOP, divider1.id, ConstraintSet.BOTTOM)
        constraints.connect(authorAndOrganization.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.setMargin(authorAndOrganization.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.connect(copyright.id, ConstraintSet.TOP, authorAndOrganization.id, ConstraintSet.BOTTOM)
        constraints.connect(copyright.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)

        constraints.connect(divider2.id, ConstraintSet.TOP, copyright.id, ConstraintSet.BOTTOM)
        constraints.connect(divider2.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider2.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider2.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.connect(wordCountIcon.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(wordCountIcon.id, ConstraintSet.TOP, divider2.id, ConstraintSet.BOTTOM)
        constraints.setMargin(wordCountIcon.id, ConstraintSet.TOP, VRBTheme.smallGutter * 2)

        constraints.centerVertically(wordCountLabel.id, wordCountIcon.id)
        constraints.connect(wordCountLabel.id, ConstraintSet.START, wordCountIcon.id, ConstraintSet.END)
        constraints.setMargin(wordCountLabel.id, ConstraintSet.START, VRBTheme.smallGutter)

        constraints.centerVertically(bottomEndLabel.id, wordCountIcon.id)
        constraints.connect(bottomEndLabel.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(bottomEndIcon.id, ConstraintSet.END, bottomEndLabel.id, ConstraintSet.START)
        constraints.centerVertically(bottomEndIcon.id, wordCountIcon.id)
        constraints.setMargin(bottomEndIcon.id, ConstraintSet.END, VRBTheme.smallGutter)

        constraints.applyTo(content);



        val scrollView = ScrollView(context);
        scrollView.id = View.generateViewId()
        scrollView.isVerticalScrollBarEnabled = false
        scrollView.isHorizontalScrollBarEnabled = false

        val scrollViewLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )
        scrollView.layoutParams = scrollViewLayoutParams
        scrollView.clipToPadding = false
        scrollView.setPadding(VRBTheme.gutter, VRBTheme.gutter, VRBTheme.gutter, VRBTheme.smallGutter)
        scrollView.setBackgroundColor(VRBTheme.COLOR_contentBG)
        scrollView.addView(content)

        return scrollView
    }

    private fun inferTopButtonParams(creditChecking : Boolean?, lap : Analytics_UserLastAccessedPerformance?) : TheButtonParams
    {
        Log.d("INFER", "Article ${article.details.title ?: ""} is loaded ? ${articleOps.isLoaded(player, article)}");

        if(creditChecking != true)
        {
            if(articleOps.isProcessing(article))
            {
                val isFetchingData = (
                        ipo.hasFlags(InProgressOperations.FLAG_fetchBundle) ||
                        ipo.hasFlags(InProgressOperations.FLAG_fetchLibrary)
                )

                val action = {
                    val filters = arrayOf(
                        BundleOps.FILTER_library,
                        BundleOps.FILTER_jobs,
                        BundleOps.FILTER_urls
                    )

                    api.fetchBundle(filters) {
                            data, err ->

                        if(err != null)
                        {
                            dialog?.toast(err.localizedMessage)
                        }
                        else if(data != null)
                        {
                            bundleOps?.consume(data!!, filters)
                            {
                                if(it != null)
                                {
                                    dialog?.toast(it.localizedMessage)
                                }
                            }
                        }
                    }
                }

                return TheButtonParams(
                    label = "refresh",
                    action = action,
                    disabled = isFetchingData
                )
            }
            else if(articleOps.isFailed(article))
            {
                val action = {
                    onPlaybackRequested()
                }

                return TheButtonParams(
                    label = "play",
                    action = action,
                    disabled = false
                )
            }
            else if(articleOps.isPlaying(player, article))
            {
                val action = {
                    player?.pause { err ->
                        if (err != null) dialog.alert(
                            message = err.localizedMessage ?: "Something went wrong"
                        )
                    }
                }

                return TheButtonParams(
                    label = "pause",
                    action = action,
                    disabled = false
                )
            }
            else if(articleOps.isLoaded(player, article))
            {
                val action = {
                    player?.play { err ->
                        if (err != null) dialog.alert(
                            message = err.localizedMessage ?: "Something went wrong"
                        )
                    }
                }

                return TheButtonParams(
                    label = "play",
                    action = action,
                    disabled = false
                )
            }
            else if(articleOps.isLoading(player, article))
            {
                // [dho] default initializers reflect this case already - 17/06/20
            }
            else
            {
                val label = if(lap != null) "continue" else "play"

                val action = {
                    onPlaybackRequested()
                }

                return TheButtonParams(
                    label = label,
                    action = action,
                    disabled = false
                )
            }
        }


        // [dho] default - 17/06/20
        return TheButtonParams(
            label = "loading",
            action = {},
            disabled = true
        )
    }

    private fun bindPresenceUpdates()
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.user_library.observe(this, Observer { library ->
                if(!library.articles.containsKey(article.id))
                {
                    // [dho] pop this screen if the article is no longer
                    // in the user library - 15/07/20
                    screens.removeAll {
                        it.key == screens.articleDetailsViewKey(article.id)
                    }
                }
            })
        }
    }

    protected fun bindTopButtonUpdates(topButton : TheButton)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.player_article.observe(this, Observer<DATA_Article> { article ->
                refreshTopButton(
                    topButton,
                    creditChecking = isCreditChecking(viewModel.creditCheck_article.value),
                    lap = getLAP(viewModel.user_lastAccessed.value)
                )
            })

            viewModel.player_state.observe(this, Observer<Int> {
                refreshTopButton(
                    topButton,
                    creditChecking = isCreditChecking(viewModel.creditCheck_article.value),
                    lap = getLAP(viewModel.user_lastAccessed.value)
                )
            })

            viewModel.creditCheck_article.observe(this, Observer<DATA_Article> { creditCheck_article ->
                refreshTopButton(
                    topButton,
                    creditChecking = isCreditChecking(creditCheck_article),
                    lap = getLAP(viewModel.user_lastAccessed.value)
                )
            })

            viewModel.user_lastAccessed.observe(this, Observer<DATA_UserLastAccessed> { lastAccessed ->
                refreshTopButton(
                    topButton,
                    creditChecking = isCreditChecking(viewModel.creditCheck_article.value),
                    lap = getLAP(lastAccessed)
                )
            })
        }
    }

    private fun refreshTopButton(button : TheButton, creditChecking: Boolean?, lap: Analytics_UserLastAccessedPerformance?)
    {
        val params = inferTopButtonParams(creditChecking, lap)

        button.refresh(params)
    }

    private fun isCreditChecking(creditCheck_article : DATA_Article?) = creditCheck_article?.id == article.id

    private fun getLAP(lastAccessed : DATA_UserLastAccessed?) : Analytics_UserLastAccessedPerformance?
        = lastAccessed?.performances?.entries?.firstOrNull {
                it -> it.value.articleID == article.id
        }?.value


    private fun onPlaybackRequested()
    {
        articleOps?.let {
            when {
                it.isFailed(article) -> {
                    dialog.confirm(getString(R.string.confirm_dialog_article_failed_title), getString(R.string.confirm_dialog_article_failed_message))
                    {
                            didConfirm ->

                        if(didConfirm) {
                            val url = article.source.canonicalURL ?: article.source.contentURL

                            activity?.let {
                                    it -> openURL(it as Activity, url)
                            }
                        }
                    }
                }
                it.isLoading(player, article) -> {
                    // do nothing
                }
                // [dho] it is NOT loading or waiting to be in a playable state
                // so let's see if it can just be played then - 09/02/20
                it.isLoaded(player, article) -> {
                    player.play(this::showAlertIfError);
                }
                it.isAvailableOffline(viewModel.user_membership.value, article) -> {
                    loadArticleAndPlay(article)
                }
                else -> {

                    // [dho] TODO credit check? - 17/07/20

                    loadArticleAndPlay(article)
                }

            }


        }
    }

    /*
    * function onArticleSelected(player : Player, article : DATA_Article)
    {
        if(Article_isLoadingOrWaitingToPlay(player, article))
        {

        }
        // [dho] it is NOT loading or waiting to be in a playable state
        // so let's see if it can just be played then - 09/02/20
        else if(Article_isLoaded(player, article))
        {
            player.play();
        }
        // [dho] is playable offline then skip credit check - 25/05/20
        else if(availableOffline /*Article_isAvailableOffline(article.id)*/)
        {
            loadArticleAndPlay();
        }
        else
        {
            player.pause();

            creditCheckInProgress = true;

            dataSource.performanceCreditCheck(article.id, (data, err) => {

                if(err !== null)
                {
                    globalAlert(message = err!.localizedDescription, () => {
                        creditCheckInProgress = false;
                    });
                }
                else
                {
                    if(data!.creditCheck.successful)
                    {
                        loadArticleAndPlay();
                    }
                    else
                    {
                        dataSource.getProducts((p, err) => {

                            if(err !== null)
                            {
                                globalAlert(message = err!.localizedDescription, () => {
                                    creditCheckInProgress = false;
                                });
                            }
                            else
                            {
                                creditCheckInProgress = false;
                                products = p
                                creditCheck = data!.creditCheck
                                resolutions = data!.resolutions
                                listenNowInterstitialMaximized = true
                            }
                        })
                    }
                }
            })
        }
    }


    * */
}