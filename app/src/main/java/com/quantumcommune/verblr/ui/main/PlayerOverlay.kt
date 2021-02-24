package com.quantumcommune.verblr.ui.main

import android.content.DialogInterface
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import androidx.media2.common.SessionPlayer
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.quantumcommune.verblr.*
import java.util.*

class PlayerOverlay : VRBOverlay() {

    companion object {
        fun newInstance() = PlayerOverlay()
    }

    private var loadedAd : UnifiedNativeAd? = null

    private var overlayPlayer : OverlayPlayerView? = null

    override fun onCreateContentView(): View? {

        val article = viewModel.player_article.value

        if(article != null)
        {
            var title : View? = null;
            var trailing : View? = null;

            val refDateUTC = article.details.modified ?: article.details.published ?: article.meta.creationUTC
            val refDateLabel = StringUtils.toDateLabel(refDateUTC)

            if(refDateLabel != null)
            {
                title = TextView(context).apply {
                    this.id = View.generateViewId()
                    this.setLines(1)
                    this.ellipsize = TextUtils.TruncateAt.END
                    this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerDateLabelFontSize)
                    this.typeface = VRBTheme.TYPEFACE_semibold
                    this.setTextColor(VRBTheme.COLOR_contextualInfo)
                    this.text = refDateLabel.toUpperCase(Locale.ROOT)
                }
            }


            if(articleOps.isAvailableOffline(viewModel.user_membership.value, article))
            {
                trailing = VRBLegacyViewFactory.headerIconButtonView(
                    context,
                    iconDrawable = R.drawable.downloaded_contextualinfo,
                    disabled = false,
                    action = null
                )
            }


            val toolbarParams = ToolbarViewParams(
                leading = VRBLegacyViewFactory.headerIconButtonView(
                    context,
                    iconDrawable = R.drawable.chevron_down_contextualinfo,
                    disabled = false,
                    action = this::dismiss
                ),
                title = title,
                trailing = trailing
            )

//            subscribeToPlayerUpdatesAndRefresh()

//            var op = overlayPlayer
//
//            if(op == null)
//            {
                val overlayPlayerParams = inferOverlayPlayerParams(article, player.currentStatus);

                val op = OverlayPlayerView.newInstance(context, overlayPlayerParams).apply {
                    overlayPlayer = this

                    this.layoutParams = ConstraintLayout.LayoutParams(
                        ConstraintLayout.LayoutParams.MATCH_PARENT,
                        ConstraintLayout.LayoutParams.MATCH_PARENT
                    )

                    bindOverlayPlayerToPlayerUpdates(this)
                }

                op.setPadding(0, VRBTheme.gutter, 0, VRBTheme.gutter)
//            }


            val ad = if(viewModel.show_ads.value == true) ads.getNextAdForPlacement(ads.PlacementID_Overlay) else null

            if(ad != null)
            {
                val view = StandardAdSupportedOverlayView.newInstance(
                    context,
                    StandardAdSupportedOverlayViewParams(
                        toolbarParams = toolbarParams,
                        content = op,
                        adParams = AdCardViewParams(
                            ad = ad
                        )//,
//                        showUpsell = viewModel.show_upsells.value ?: false,
//                        onUpsellClick = this::onUpsellClick
                    )
                )

                view.layoutParams = LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )

                view.setBackgroundColor(VRBTheme.COLOR_toolbarBG)

                return view
            }
            else
            {
                val view = StandardOverlayView.newInstance(
                    context,
                    StandardOverlayViewParams(
                        toolbarParams = toolbarParams,
                        content = op
                    )
                )

                view.layoutParams = LinearLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT
                )
                view.setBackgroundColor(VRBTheme.COLOR_toolbarBG)

                val paddingBottom = (
                    ItemCardView.marginVertical * 2 +
                    ItemCardView.artworkDim +
                    VRBTheme.smallGutter
                )

                view.setPadding(0, 0, 0, paddingBottom)

                return view
            }
        }
        else
        {
            dismiss()

            return null
        }
    }

    private fun onUpsellClick()
    {
        overlays.push(
            NavItem(
                key = OverlayNav.KEY_upsellViewNavKey,
                item = UpsellOverlay.newInstance()
            ),
//            removeAllExisting = overlays::playerOverlayMatcher
            // [dho] TODO look into bug where sliding down an overlay
            // displays the previous overlay in the stack that was slid
            // down before... seems they are not being completely removed when
            // slid down - 07/07/20
            removeAllExisting = overlays::anyMatcher//upsellOverlayMatcher
        )
    }


    override fun onStateChanged(state: Int) {
        super.onStateChanged(state)

        if(state == BottomSheetBehavior.STATE_HIDDEN)
        {
            cleanupAd()
        }
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        cleanupAd()
    }

    private fun cleanupAd()
    {
        try {
            loadedAd?.videoController?.stop()
        }
        catch(err : Exception )
        {

        }

        loadedAd = null
    }


    protected fun bindOverlayPlayerToPlayerUpdates(overlayPlayer : OverlayPlayerView)
    {
        viewModel.player_article.observe(this, Observer {
            refreshOverlayPlayer(overlayPlayer, it, player.currentStatus)
        })

        viewModel.player_state.observe(this, Observer {
            refreshOverlayPlayer(overlayPlayer, viewModel.player_article.value, player.currentStatus)
        })

        viewModel.player_progress.observe(this, Observer {
            refreshOverlayPlayer(overlayPlayer, viewModel.player_article.value, player.currentStatus)
        })
    }

    private fun refreshOverlayPlayer(overlayPlayer : OverlayPlayerView, article : DATA_Article?, status : MediaPlayerStatus)
    {
        if(article != null)
        {
            val params = inferOverlayPlayerParams(article, status)

            overlayPlayer.refresh(params)
        }
        else
        {
            dismiss()
        }
    }

    protected fun inferOverlayPlayerParams(article : DATA_Article, status : MediaPlayerStatus) : OverlayPlayerViewParams
    {
        val artworkURL = article.artwork.largeArtwork ?: article.artwork.standardArtwork ?: null;

        val onDetailsClick = {
            screens.push(
                navItem = NavItem(
                    key = screens.articleDetailsViewKey(article.id),
                    item = ArticleDetailsScreen.newInstance(article)
                ),
                removeAllExisting = screens::articleDetailsViewMatcher
                // [dho] 'Unresolved reference: articleDetailsViewMatcher' ?? OK compiler - 19/06/20
                //ScreensNav::articleDetailsViewMatcher
            )

            dismiss()
        }

        return OverlayPlayerViewParams(
            artworkURL = artworkURL,
            title = article.details.title,
            author = article.details.author?.name,
            organization = article.details.organization?.name,
            onDetailsClick = onDetailsClick,
            seekBar = convertToLabeledSeekBarViewParams(article, status),
            controls = convertToOverlayPlayerControlsViewParams(article, status)
        )
    }


    private fun convertToLabeledSeekBarViewParams(article : DATA_Article, status : MediaPlayerStatus) : LabeledSeekBarViewParams
    {
        val disabled = status.state == LocalMediaPlayerService.STATE_loading

        val hasProgress = !(status.progress < 0.0f)
        val hasDuration = status.duration > 0.0f

        // [dho] NOTE will be some max negative number when initializing - 20/06/20
        val progress = if(hasProgress) StringUtils.toDurationLabel(status.progress) else "--:--"
        val duration = if(hasDuration) StringUtils.toDurationLabel(status.duration) else "--:--"

        val value = if(hasProgress && hasDuration) status.progress / status.duration else 0.0f

        return LabeledSeekBarViewParams(
            fromLabel = progress,
            toLabel = duration,
            value = value,
            onSeek = this::onSeek,
            onScrubStart = this::onScrubStart,
            onScrubEnd = this::onScrubEnd,
            disabled = disabled
        )
    }

    private fun onSeek(scalar : Float, userDriven : Boolean)
    {
        if(userDriven)
        {
            val currentStatus = player.currentStatus

            player.changeTime(currentStatus.duration * scalar, this::defaultCompletionHandler)
        }
    }


    private var resumeAfterScrub = false
    private fun onScrubStart()
    {
        resumeAfterScrub = player.currentStatus.state == LocalMediaPlayerService.STATE_playing

        player.pause(this::defaultCompletionHandler)
    }

    private fun onScrubEnd()
    {
        if(resumeAfterScrub)
        {
            player.play(this::defaultCompletionHandler)
        }
    }

    private fun convertToOverlayPlayerControlsViewParams(article : DATA_Article, status : MediaPlayerStatus) : OverlayPlayerControlsViewParams
    {
//        val state = articleOps.playingState(player, article)
        val state = status.state
        val disabled = state == LocalMediaPlayerService.STATE_loading

        val leadingButtonIcon = R.drawable.go_backward_10_subwhite
        val onLeadingButtonClick = { player.skipBackward(this::defaultCompletionHandler) }
        val leadingButtonDisabled = disabled


        val centralButtonDisabled = disabled
        val centralButtonIcon = when(state)
        {
            LocalMediaPlayerService.STATE_playing -> {
                if(status.buffState != SessionPlayer.BUFFERING_STATE_COMPLETE && status.buffProgress <= 0.1f)
                    R.drawable.sync_subwhite
                else
                    R.drawable.pause_circle_fill_subwhite
            }
            LocalMediaPlayerService.STATE_loading -> R.drawable.sync_subwhite
            else -> R.drawable.play_circle_fill_subwhite
        }
        val onCentralButtonClick = when(state)
        {
            LocalMediaPlayerService.STATE_playing -> { { player.pause(this::defaultCompletionHandler) } }
            else -> { { player.play(this::defaultCompletionHandler) } }
        }

        val trailingButtonIcon = R.drawable.go_forward_10_subwhite
        val onTrailingButtonClick = { player.skipForward(this::defaultCompletionHandler) }
        val trailingButtonDisabled = disabled


        return OverlayPlayerControlsViewParams(
            leadingButtonIcon = leadingButtonIcon,
            onLeadingButtonClick = onLeadingButtonClick,
            leadingButtonDisabled = leadingButtonDisabled,
            centralButtonIcon = centralButtonIcon,
            onCentralButtonClick = onCentralButtonClick,
            centralButtonDisabled = centralButtonDisabled,
            trailingButtonIcon = trailingButtonIcon,
            onTrailingButtonClick = onTrailingButtonClick,
            trailingButtonDisabled = trailingButtonDisabled
        );

    }

}