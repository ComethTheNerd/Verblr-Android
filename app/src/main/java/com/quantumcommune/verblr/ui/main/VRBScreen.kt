package com.quantumcommune.verblr.ui.main

import android.app.Activity
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.media2.common.SessionPlayer
import com.quantumcommune.verblr.*
import java.util.*


abstract class VRBScreen : Fragment(), VRBUI {
    companion object
    {
        val defaultStatusIcon = R.drawable.exclamationmark_circle_fill_contextualinfo
    }

    protected lateinit var api : API
    protected lateinit var viewModel: VRBViewModel
    protected lateinit var articleOps : ArticleOps
    protected lateinit var bundleOps : BundleOps
    protected lateinit var membershipOps: MembershipOps;
    protected lateinit var localCache : LocalCache
    protected lateinit var network : Network
    protected lateinit var ipo : InProgressOperations
    protected lateinit var dialog : Dialog
    protected lateinit var screens : ScreenNav
    protected lateinit var overlays : OverlayNav
    protected lateinit var statuses : StatusNav
    protected lateinit var auth : FirebaseWrapper
    protected lateinit var player : Player
    protected lateinit var shop : Shop
    protected lateinit var ads : AdVendor

//    private lateinit var sheetBehavior : BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [dho] sharing same model instance with activity. Adapted from : https://stackoverflow.com/a/57609917/300037 - 17/05/20
        activity?.let {
            val mainActivity = it as MainActivity

            viewModel = ViewModelProviders.of(it).get(VRBViewModel::class.java)
            ipo = InProgressOperations(viewModel)
            screens = mainActivity.screens//ScreensNav(viewModel)
            overlays = mainActivity.overlays//OverlaysNav(viewModel)
            statuses = mainActivity.statuses
            shop = mainActivity.shop
            auth = FirebaseWrapper(activity as Activity)
            player = mainActivity.player
            ads = mainActivity.ads

            if(context != null)
            {
                localCache = LocalCache(it, viewModel)
                api = API(context!!, viewModel)
                bundleOps = BundleOps(context!!, viewModel, localCache)
                dialog = Dialog(context!!)
                network = Network(context!!)
                membershipOps = MembershipOps()
                articleOps = ArticleOps(localCache, membershipOps)

            }

        }
    }

    protected fun toolbarRefreshButtonView(filters : Array<String>, completion : (bundle : DATA_Bundle?, err : Exception?) -> Unit) = VRBLegacyViewFactory.headerIconButtonView(
            context,
            iconDrawable = R.drawable.arrow_clockwise_subwhite,
            disabled = ipo.hasFlags(InProgressOperations.FLAG_fetchBundle)
        )
        {
            api.fetchBundle(filters) {
                    data, err ->

                if(err != null)
                {
                    completion(null, err)
                }
                else if(data != null)
                {
                    bundleOps?.consume(data, filters)
                    {
                        err -> completion(data, err)
                    }
                }
            }
        }


    protected fun toolbarTitleLogoView() : ImageView
    {
        val logoDim = VRBTheme.headerViewIconDim
        val logo = ImageView(context)
        logo.id = View.generateViewId()
        logo.setImageResource(R.drawable.logo_mono)
        logo.layoutParams = LinearLayout.LayoutParams(logoDim, logoDim)
        logo.setOnClickListener { screens.clear() }

        return logo
    }

    protected fun toolbarTitleTextView(input : String) : TextView
    {
        val textView = TextView(context)
        textView.id = View.generateViewId()
        textView.setLines(1)
        textView.ellipsize = TextUtils.TruncateAt.END
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m18Size)
        textView.typeface = VRBTheme.TYPEFACE_semibold
        textView.setTextColor(VRBTheme.COLOR_toolbarCTA)
        textView.text = input.toLowerCase(Locale.ROOT)

        return textView
    }




    protected fun bindStatusBarToStateUpdates(statusBar : StatusBarView)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.nav_statuses.observe(this, Observer {
                refreshStatusBar(statusBar, viewModel.user_membership.value, it)
            })

            viewModel.user_membership.observe(this, Observer {
                refreshStatusBar(statusBar, it, viewModel.nav_statuses.value)
            })
        }
    }



    private fun refreshStatusBar(statusBar : StatusBarView, userMembership: DATA_UserMembership?, navItems : Stack<NavItem<VRBStatus>>?)
    {
        val params = inferStatusBarParams(userMembership, navItems)

        statusBar.refresh(params)
    }

    protected fun inferStatusBarParams(userMembership: DATA_UserMembership?, navItems : Stack<NavItem<VRBStatus>>?) : StatusBarViewParams
    {
        val latestStatus = navItems?.lastOrNull()?.item

        return if(latestStatus != null)
        {
            StatusBarViewParams(
                icon = latestStatus.icon ?: defaultStatusIcon,
                label = latestStatus.label,
                action = latestStatus.action,
                isHidden = false
            )
        }
        else
        {
            val streamCount = viewModel.user_lastAccessed.value?.performances?.count() ?: 0
            val articleCount = viewModel.user_library.value?.articles?.count() ?: 0

            val label = (
                if(streamCount > 0) "tap here to sign up and continue listening!"
                else if(articleCount > 0) "tap here to register or sign in..."
                else "already a member? tap here to sign in..."
            )

            // [dho] TODO also hide if already showing auth gateway? - 09/07/20
            val isHidden = !membershipOps.isNullOrAnon(userMembership)

            StatusBarViewParams(
                icon = R.drawable.person_electro,
                label = label,
                action = this::showAuthGateway,
                isHidden = isHidden
            )
        }
    }

    private fun showAuthGateway()
    {
        screens.push(
            NavItem(
                key = ScreenNav.KEY_authGatewayViewNavKey,
                item = AuthGatewayScreen.newInstance()
            ),
            removeAllExisting = screens::authGatewayViewMatcher
        )
    }

    protected fun bindMiniPlayerToStateUpdates(miniPlayer : MiniPlayerView)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.player_article.observe(this, Observer { article ->
                refreshMiniPlayer(miniPlayer,
                    article = article,
                    state = viewModel.player_state.value,
                    progress = viewModel.player_progress.value,
                    buffState = viewModel.player_buff_state.value,
                    buffProgress =viewModel.player_buff_progress.value
                )
            })

//        viewModel.player_duration.observe(this, Observer<Float> { duration ->
//            val durationView = miniPlayerView.findViewWithTag<TextView>(XXXXXVRBView.TAG_miniPlayerDurationLabel)
//
//            if(durationView != null)
//            {
//                durationView.text = if(duration != null) StringUtils.toDurationLabel(duration) else "--:--"
//            }
//        })

//        viewModel.player_progress.observe(this, Observer<Float> { progress ->
//
//            val progressView = miniPlayerView.findViewWithTag<TextView>(XXXXXVRBView.TAG_miniPlayerProgressLabel)
//
//            if(progressView != null)
//            {
//                progressView.text = if(progress != null) StringUtils.toDurationLabel(progress) else "--:--"
//            }
//        })

            viewModel.player_state.observe(this, Observer {
                refreshMiniPlayer(miniPlayer,
                    article = viewModel.player_article.value,
                    state = it,
                    progress = viewModel.player_progress.value,
                    buffState = viewModel.player_buff_state.value,
                    buffProgress =viewModel.player_buff_progress.value
                )
            })

            viewModel.player_progress.observe(this, Observer {
                refreshMiniPlayer(miniPlayer,
                    article = viewModel.player_article.value,
                    state = viewModel.player_state.value,
                    progress = it,
                    buffState = viewModel.player_buff_state.value,
                    buffProgress =viewModel.player_buff_progress.value
                )
            })

            viewModel.player_buff_progress.observe(this, Observer {
                refreshMiniPlayer(miniPlayer,
                    article = viewModel.player_article.value,
                    state = viewModel.player_state.value,
                    progress = viewModel.player_progress.value,
                    buffState = viewModel.player_buff_state.value,
                    buffProgress = it
                )
            })

            viewModel.player_buff_state.observe(this, Observer {
                refreshMiniPlayer(miniPlayer,
                    article = viewModel.player_article.value,
                    state = viewModel.player_state.value,
                    progress = viewModel.player_progress.value,
                    buffState = it,
                    buffProgress =viewModel.player_buff_progress.value
                )
            })

//        sheetBehavior.setBottomSheetCallback(object : BottomSheetCallback() {
//            override fun onStateChanged(view: View, newState: Int) {
//                when (newState) {
//                    BottomSheetBehavior.STATE_HIDDEN -> {
//                    }
//                    BottomSheetBehavior.STATE_EXPANDED -> {
////                        btn_bottom_sheet.setText("Close Sheet")
//                    }
//                    BottomSheetBehavior.STATE_COLLAPSED -> {
////                        btn_bottom_sheet.setText("Expand Sheet")
//                    }
//                    BottomSheetBehavior.STATE_DRAGGING -> {
//                    }
//                    BottomSheetBehavior.STATE_SETTLING -> {
//                    }
//                }
//            }
//
//            override fun onSlide(view: View, v: Float) {}
//        })
        }
    }

    private fun refreshMiniPlayer(miniPlayer : MiniPlayerView, article : DATA_Article?, state : Int?, progress : Float?, buffState : Int?, buffProgress : Float?)
    {
        val params = inferMiniPlayerParams(article, state, progress, buffState, buffProgress)

        miniPlayer.refresh(params)
    }

    protected fun inferMiniPlayerParams(article : DATA_Article?, state : Int?, progress : Float?, buffState : Int?, buffProgress : Float?) : MiniPlayerViewParams
    {
        val duration = viewModel.player_duration.value ?: 0.0f

        val progressScalar = (
            if(duration > 0.0f)
                clamp((progress ?: 0.0f) / duration, 0.0f, 1.0f)
            else
                0.0f
        )

        val progressBar = ProgressBarViewParams(
            progressScalar = progressScalar,
            progressTint = VRBTheme.COLOR_progressBarCompleted,
            progressBackgroundTint = VRBTheme.COLOR_progressBarRemaining
        )

        val buttonIcon = when(state)
        {
            LocalMediaPlayerService.STATE_playing -> {
                if(buffState != SessionPlayer.BUFFERING_STATE_COMPLETE && (buffProgress ?: 0.0F) <= 0.1f)
                    R.drawable.sync_subwhite
                else
                    R.drawable.pause_circle_fill_subwhite
            }
            LocalMediaPlayerService.STATE_paused -> R.drawable.play_circle_subwhite
            LocalMediaPlayerService.STATE_loading -> R.drawable.sync_subwhite
            else -> R.drawable.play_circle_subwhite//R.drawable.transparent
        }

        val onButtonClick = {
            when (state) {
                LocalMediaPlayerService.STATE_playing -> player?.pause(this::showAlertIfError)
                LocalMediaPlayerService.STATE_paused -> player?.play(this::showAlertIfError)
                else -> {
                }
            }
        }

        val onClick = {

//            var showAdAndThen : (() -> Unit)? = null
//
//            // [dho] DISABLING showing an ad when the user clicks on the mini
//            // player for now, just because if the article is loading at the same
//            // time and then it finishes loading in the background, it will detect
//            // the player overlay is showing and will start playing automatically,
//            // which is wrong because we do not want the audio to play before the user
//            // has finished the advert - 18/06/20
////            if(viewModel.show_ads.value == true && player.isLoading())
////            {
////                showAdAndThen = {}
////            }
//
//            val params = AdSupportedPlayerOverlayParams(showAdAndThen = showAdAndThen)
//
//
//            overlays?.push(
//                NavItem(
//                    key = OverlaysNav.KEY_maxiPlayerViewNavKey,
//                    fragment = AdSupportedPlayerOverlay.newInstance(params)
//                ),
//                matcher = { it.key == OverlaysNav.KEY_maxiPlayerViewNavKey }
//            )


            overlays.push(
                NavItem(
                    key = OverlayNav.KEY_maxiPlayerViewNavKey,
                    item = PlayerOverlay.newInstance()
                ),
                // [dho] TODO look into bug where sliding down an overlay
                // displays the previous overlay in the stack that was slid
                // down before... seems they are not being completely removed when
                // slid down - 07/07/20
                removeAllExisting = overlays::anyMatcher//playerOverlayMatcher
            )
        }

        return MiniPlayerViewParams(
            artworkURL = article?.artwork?.largeArtwork ?: article?.artwork?.standardArtwork ?: null,
            title = article?.details?.title ?: "",
            buttonIcon = buttonIcon,
            progressBar = progressBar,
            onButtonClick = onButtonClick,
            onClick = onClick,
            isHidden = article == null
        )
    }

    private fun suppressAutoShowPlayerOverlay(n : NavItem<VRBOverlay>)
            = overlays.playerOverlayMatcher(n) || overlays.upsellOverlayMatcher(n)

    protected fun loadArticleAndPlay(article : DATA_Article)
    {
        val membership = viewModel.user_membership.value

        if(membershipOps.isNullOrAnon(membership))
        {
            val lap = viewModel.user_lastAccessed.value

            // [dho] has not accessed a performance of this article already - 01/07/20
            // [dho] TODO this will NOT account for if the user is accessing a different performance
            // of the same article, but it is good enough for now - 01/07/20
            if(lap?.articles?.containsKey(article.id) != true)
            {
                val config = viewModel.user_config.value
                val limit = config?.membership_Entitlement_FreshStreamLimit_Anon ?: 0
                val listened = lap?.performances?.count() ?: 0

                if(listened >= limit)
                {
                    dialog.alert(
                        icon = R.drawable.person_electro,
                        title = "Want More?",
                        message = "Sign in or register to keep listening",
                        completion = this::showAuthGateway
                    )

                    return;
                }
            }
        }


        val useCache = membershipOps.useOfflineCaching(membership)
        val isCancellationRequested = { articleID : String ->
            viewModel.player_article.value != null &&
                    viewModel.player_article.value?.id != articleID
        }

        player.loadArticle(article, useCache, isCancellationRequested, { lap, err ->
            if(err != null) {
                dialog.alert(message = err.localizedMessage ?: "Something went wrong");
            } else {//if(player.isLastLoadedArticleID(article.id)) {

                if(overlays.isShowing(this::suppressAutoShowPlayerOverlay))
                {
                    beginArticlePlayback(article, lap)
                }
                else
                {
//                    var showAdAndThen : (() -> Unit)? = null
//
//                    if(viewModel.show_ads.value == true)
//                    {
//                        showAdAndThen = { beginArticlePlayback(article, lap) }
//                    }
//
//                    // [dho] after the maxi player has shown an ad we will then play
//                    // the audio - 15/06/20
//                    val playerOverlayParams = AdSupportedPlayerOverlayParams(showAdAndThen = showAdAndThen)
//
//                    // [dho] show maxi player - 09/06/20
//                    overlays.push(
//                        NavItem(
//                            key = OverlaysNav.KEY_maxiPlayerViewNavKey,
//                            fragment = AdSupportedPlayerOverlay.newInstance(playerOverlayParams)
//                        ),
//                        matcher = this::playerOverlayMatcher
//                    )
//
//                    if(playerOverlayParams.showAdAndThen == null)
//                    {
//                        beginArticlePlayback(article, lap)
//                    }


                    beginArticlePlayback(article, lap)

                    overlays.push(
                        NavItem(
                            key = OverlayNav.KEY_maxiPlayerViewNavKey,
                            item = PlayerOverlay.newInstance()
                        ),
                        // [dho] TODO look into bug where sliding down an overlay
                        // displays the previous overlay in the stack that was slid
                        // down before... seems they are not being completely removed when
                        // slid down - 07/07/20
                        removeAllExisting = overlays::anyMatcher//playerOverlayMatcher
                    )


                }


            }

            //                    updateCachedStatus()
        })
    }


    private fun beginArticlePlayback(article : DATA_Article, lap : Analytics_UserLastAccessedPerformance?)
    {
        if(player.article() != article)
        {
            return
        }

        if(lap != null)
        {
            // [dho] restore where user got up to last time - 22/05/20
            player.changeTime(lap.progress){
                    err ->
                if(err != null)
                {
                    showAlertIfError(err)
                }
                else {
                    player.play(this::showAlertIfError)
                }
            }

            bundleOps.updateCachedLastAccessedPerformanceIfNewer(lap)
            {
                // [dho] NOTE we do not really care about the error - 22/05/20
            }
        }
        else
        {
            player.play(this::showAlertIfError);
        }
    }

//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        // [dho] sharing same model instance with activity. Adapted from : https://stackoverflow.com/a/57609917/300037 - 17/05/20
//        activity?.let {
//            viewModel = ViewModelProviders.of(it).get(MainViewModel::class.java)
//        }
//    }

    fun showAlertIfError(err : Exception?)
    {
        if(err != null){
            dialog.alert(message = err.localizedMessage ?: "Something went wrong")
        }
    }

    fun showToastIfError(err : Exception?)
    {
        if(err != null){
            dialog.toast(err.localizedMessage ?: "Something went wrong")
        }
    }
}