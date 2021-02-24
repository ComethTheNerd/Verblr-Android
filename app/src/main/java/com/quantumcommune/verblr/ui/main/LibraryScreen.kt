package com.quantumcommune.verblr.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import com.quantumcommune.verblr.*
import kotlin.random.Random

class LibraryScreen : VRBScreen() {

    companion object {
        fun newInstance() = LibraryScreen()
    }

    lateinit var standardScreen : StandardScreenView
    lateinit var upsellBanner : UpsellBannerView
    lateinit var endOfListDivider : HorizontalDividerView
    lateinit var feed : FeedView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val toolbarParams = ToolbarViewParams(
            leading = toolbarRefreshButtonView(arrayOf(
                BundleOps.FILTER_library,
                BundleOps.FILTER_jobs,
                BundleOps.FILTER_urls
            ), this::onRefreshComplete),
            title = toolbarTitleLogoView(),
            trailing = VRBLegacyViewFactory.headerIconButtonView(
                context,
                iconDrawable = R.drawable.gear_subwhite
            )
            {
                screens.push(
                    NavItem(
                        key = ScreenNav.KEY_accountViewNavKey,
//                        title = "settings",
                        item = AccountScreen.newInstance()
                    )
                )
            }
        )

        val articles = viewModel.user_library.value?.articles?.values?.toList() ?: listOf()
        val includeAds = viewModel.show_ads.value ?: false
        val userMembership = viewModel.user_membership.value

        feed = FeedView.newInstance(context, ads, inferFeedViewParams(articles, includeAds, userMembership))
        feed.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
        )
        feed.background = VRBTheme.GRAD_standard

        upsellBanner = UpsellBannerView.newInstance(context, UpsellBannerViewParams {

            overlays.push(
                NavItem(
                    key = OverlayNav.KEY_upsellViewNavKey,
                    item = UpsellOverlay.newInstance()
                ),
                removeAllExisting = overlays::upsellOverlayMatcher
            )

//            screens.push(
//                NavItem(
//                    key = ScreensNav.KEY_storeViewNavKey,
//                    fragment = StoreFragment.newInstance()
//                )
//            )
        })
        upsellBanner.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        refreshUpsellBanner(upsellBanner, viewModel.show_upsells.value)

        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_toolbarAccent
        ))
        divider.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        endOfListDivider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_toolbarAccent
        ))
        endOfListDivider.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        val content = ConstraintLayout(context)

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(upsellBanner)
        content.addView(divider)
        content.addView(feed)
        content.addView(endOfListDivider)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(upsellBanner.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(upsellBanner.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(upsellBanner.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)

        constraints.connect(divider.id, ConstraintSet.TOP, upsellBanner.id, ConstraintSet.BOTTOM)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)

        constraints.connect(feed.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(feed.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(feed.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(feed.id, ConstraintSet.BOTTOM, endOfListDivider.id, ConstraintSet.TOP)
//        constraints.setMargin(feed.id, ConstraintSet.START, feedMarginHorizontal)
//        constraints.setMargin(feed.id, ConstraintSet.END, feedMarginHorizontal)

        constraints.connect(endOfListDivider.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.connect(endOfListDivider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(endOfListDivider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)


        constraints.applyTo(content);

        val statusBarParams = inferStatusBarParams(viewModel.user_membership.value, viewModel.nav_statuses.value)

        val miniPlayerParams = inferMiniPlayerParams(
            article = viewModel.player_article.value,
            state = viewModel.player_state.value,
            progress = viewModel.player_progress.value,
            buffState = viewModel.player_buff_state.value,
            buffProgress = viewModel.player_buff_progress.value
        )

        val standardScreenParams = StandardScreenViewParams(
            toolbarParams,
            content,
            statusBarParams,
            miniPlayerParams
        )

        standardScreen = StandardScreenView.newInstance(context, standardScreenParams)

        standardScreen.background = VRBTheme.GRAD_standard

        return standardScreen
    }

    private fun onRefreshComplete(bundle : DATA_Bundle?, err : Exception?)
    {
        if(err != null)
        {
            showToastIfError(err)
        }
//        else
//        {
//            val articles = (bundle?.library?.articles ?: viewModel.user_library.value?.articles)?.values?.toList() ?: listOf()
//
//            refreshFeedView(articles)
//        }
    }

//    private fun refreshFeedView(articles : List<DATA_Article>)
//    {
//        feed.refresh(inferFeedViewParams(articles));
//    }

    private fun inferEmptyViewParams(userMembership: DATA_UserMembership?) : LibraryEmptyViewParams
    {
        val showCTA = membershipOps.isNullOrAnon(userMembership)

        return LibraryEmptyViewParams(
            cta = TheButtonParams(
                label = "create account",
                action = this::showAuthGateway
            ),
            ctaStrapline = "register free for unlimited access",
            showCTA = showCTA
        )
    }

    private fun showAuthGateway()
    {
        screens?.push(
            NavItem(
                key = ScreenNav.KEY_authGatewayViewNavKey,
                item = AuthGatewayScreen.newInstance()
            ),
            removeAllExisting = screens::authGatewayViewMatcher
        )
    }

    private fun inferFeedViewParams(articles : List<DATA_Article>, includeAds : Boolean, userMembership : DATA_UserMembership?) : FeedViewParams
    {
        val feedPaddingVertical = VRBTheme.smallGutter
        val feedPaddingHorizontal = VRBTheme.smallGutter

        val sortedArticles =  ArticleOps.sort(
            articles,
            localCache,
            viewModel.user_lastAccessed.value
        )

        return FeedViewParams(
            horizontalPadding = feedPaddingHorizontal,
            verticalPadding = feedPaddingVertical,
            items = getFeedItems(sortedArticles, includeAds),
            emptyView = LibraryEmptyView.newInstance(context, inferEmptyViewParams(userMembership)).apply {
                this.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )
            }
        )
    }

    private fun getFeedItems(articles : List<DATA_Article>, includeAds : Boolean) : List<FeedItemParams<*>>
    {
        return if(!includeAds) {
            articles.map(this::convertToFeedArticleParams)
        } else {
            convertToAdSupportedFeedItems(articles)
        }
    }

    val AD_INTERVAL_MIN = 4
    val AD_INTERVAL_MAX = 6
    private fun convertToAdSupportedFeedItems(articles : List<DATA_Article>, max : Int = Int.MAX_VALUE) : List<FeedItemParams<*>>
    {
        // [dho] number of non-ad items to include before inserting an advert
        // into the result - 19/06/20
        var adInterval = Random.nextInt(AD_INTERVAL_MIN, AD_INTERVAL_MAX)

        val items = mutableListOf<FeedItemParams<*>>()

        var idx = 0

        while (true)
        {
            // until
            for(i in 0 until adInterval)
            {
                val a = articles.elementAtOrNull( idx + i) ?: return items

                items.add(convertToFeedArticleParams(a));

                if(items.count() == max)
                {
                    return items
                }
            }

            idx += adInterval;

            // [dho] insert the advert - 19/06/20
            items.add(convertToFeedAdParams());

            if(items.count() == max)
            {
                return items
            }

            adInterval = Random.nextInt(AD_INTERVAL_MIN, AD_INTERVAL_MAX)
        }
    }

    private fun convertToFeedArticleParams(article : DATA_Article)
            = FeedItemParams(
        id = "${FeedItemKind.ARTICLE.name}::${article.id}",
        kind = FeedItemKind.ARTICLE,
        params = ArticleCardViewParams(
            article = article,
            statusFlags = articleOps.getStatusFlags(viewModel.user_membership.value, article),
            onClick = {
                onPlaybackRequest(article)
            },
            onDetailsClick = {
                screens.push(
                    navItem = NavItem(
                        key = screens.articleDetailsViewKey(article.id),
                        item = ArticleDetailsScreen.newInstance(article)
                    ),
                    removeAllExisting = screens::articleDetailsViewMatcher
                    // [dho] 'Unresolved reference: articleDetailsViewMatcher' ?? OK compiler - 19/06/20
                    //ScreensNav::articleDetailsViewMatcher

                )
            }
        )
    )

    private fun convertToFeedAdParams(): FeedItemParams<*> {

        val ad = ads.getNextAdForPlacement(ads.PlacementID_Feed)

        // [dho] prebaked to try and minimize the lag of loading them - 19/06/20
        val params = if(ad != null) FeedAdView.newInstance(
            context, FeedAdViewParams(
                ad = ad
            )
        ) else null

        return FeedItemParams(
            id = "${FeedItemKind.AD.name}::${uuid()}",
            kind = FeedItemKind.AD,
            params = params
        )
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        bindStatusBarToStateUpdates(standardScreen.statusBar)
        bindMiniPlayerToStateUpdates(standardScreen.miniPlayer)
        bindUpsellBannerToStateUpdates(upsellBanner)
        bindFeedToStateUpdates(feed)
    }

    private fun bindFeedToStateUpdates(feed : FeedView)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.user_library.observe(this, Observer {
                val articles = it.articles.values.toList()
                val showAds = viewModel.show_ads.value ?: false
                val userMembership = viewModel.user_membership.value

                feed.refresh(inferFeedViewParams(articles, showAds, userMembership));
            })

            viewModel.show_ads.observe(this, Observer {
                val articles = viewModel.user_library.value?.articles?.values?.toList() ?: listOf()
                val showAds = it
                val userMembership = viewModel.user_membership.value

                feed.refresh(inferFeedViewParams(articles, showAds, userMembership));
            })

            viewModel.user_membership.observe(this, Observer {
                val articles = viewModel.user_library.value?.articles?.values?.toList() ?: listOf()
                val showAds = viewModel.show_ads.value ?: false
                val userMembership = it

                feed.refresh(inferFeedViewParams(articles, showAds, userMembership));
            })
        }
    }

    private fun bindUpsellBannerToStateUpdates(upsellBanner : UpsellBannerView)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.show_upsells.observe(this, Observer {
                refreshUpsellBanner(upsellBanner, it)
            })
        }
    }

    private fun refreshUpsellBanner(upsellBanner: UpsellBannerView, showUpsells : Boolean?)
    {
        val show = showUpsells == true

        upsellBanner.isVisible = show
        upsellBanner.isGone = !show
    }

    private fun onPlaybackRequest(article : DATA_Article)
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
                // [dho] if the user is tapping an article again
                // whilst it is loading just ignore the tap request - 22/05/20
                it.isLoading(player, article) -> {
                    //            updateCachedStatus()
                }
                // [dho] it is NOT loading or waiting to be in a playable state
                // so let's see if it can just be played then - 09/02/20
                it.isLoaded(player, article) -> {
                    // [dho] otherwise restart the track - 16/01/20
                    player.changeTime(0.0f)
                    {
                        err -> if(err != null)
                        {
                            dialog.alert(message = err.localizedMessage)
                        }
                        else {
                            player.play { err -> if(err != null)
                            {
                                dialog.alert(message = err.localizedMessage)
                            }};
                        }
                    }
                    //            updateCachedStatus()
                }
                // [dho] otherwise we need to load the article - 09/02/20
                else -> {
                    loadArticleAndPlay(article)
                }
            }
        }
    }
}