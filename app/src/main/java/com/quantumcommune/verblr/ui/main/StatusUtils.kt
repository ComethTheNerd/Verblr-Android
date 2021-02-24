package com.quantumcommune.verblr.ui.main

import android.content.Context
import com.quantumcommune.verblr.*


class StatusUtils(private val context : Context?, private val dialog: Dialog, private val overlays: OverlayNav) {

    private val STATUS_LABEL_subscriptionEnding = "subscription ending (tap for info)"
    private val STATUS_ICON_subscriptionEnding = R.drawable.exclamationmark_circle_fill_contextualinfo;

    fun inferArticleStatus(article : DATA_Article) : VRBStatus?
    {
        if(ArticleOps.isProcessing(article))
        {
            return VRBStatus(
                label = "this article is currently processing"
            )
        }

        if(ArticleOps.isFailed(article))
        {
            return VRBStatus(
                label = "submit this article again to fix a processing error",
                action = { showArticleFailureDialog(article) }
            )
        }

        if(ArticleOps.hasContentQualityWarning(article))
        {
            val didFindContentRoot = (article.inferredContentRoot ?: false)

            return VRBStatus(
                label = (
                    if(didFindContentRoot) "some article details could not be determined"
                    else "Verblr was unsure about article starting point"
                )
            )
        }

        return null
    }

    fun inferAddArticleStatus(ipo : Int) : VRBStatus?
    {
        if((ipo and InProgressOperations.FLAG_addArticle) == InProgressOperations.FLAG_addArticle)
        {
            return VRBStatus(
                // [dho] TODO loading symbol in contextual info colour! - 24/06/20
                icon = R.drawable.exclamationmark_circle_fill_contextualinfo,
                label = "processing..."
            )
        }
        else
        {
            return null
        }
    }


    fun inferRefreshBundleStatus(ipo : Int) : VRBStatus?
    {
        if((ipo and InProgressOperations.FLAG_fetchBundle) == InProgressOperations.FLAG_fetchBundle)
        {
            return VRBStatus(
                // [dho] TODO loading symbol in contextual info colour! - 24/06/20
                icon = R.drawable.exclamationmark_circle_fill_contextualinfo,
                label = "fetching..."
            )
        }
        else
        {
            return null
        }
    }

    fun inferPurchaseStatus(ipo : Int) : VRBStatus?
    {
        if((ipo and InProgressOperations.FLAG_purchase) == InProgressOperations.FLAG_purchase)
        {
            return VRBStatus(
                // [dho] TODO info circle symbol in contextual info colour! - 24/06/20
                icon = R.drawable.exclamationmark_circle_fill_contextualinfo,
                label = "processing..."
            )
        }
        else
        {
            return null
        }
    }

    fun inferMembershipStatus(userMembership: DATA_UserMembership) : VRBStatus?
    {
        val pending = userMembership.pending

        if(pending != null)
        {
            return when(pending.reason)
            {
                "subscription-change" -> VRBStatus(
                    label = "pending membership change (tap for info)",
                    action = { showPendingChangeDialog(userMembership) }
                )
                "billing-error" -> VRBStatus(
                    icon = STATUS_ICON_subscriptionEnding,
                    label = STATUS_LABEL_subscriptionEnding,
                    action = this::showBillingErrorDialog
                )
                "autorenew-off" -> VRBStatus(
                    icon = STATUS_ICON_subscriptionEnding,
                    label = STATUS_LABEL_subscriptionEnding,
                    action = { showAutoRenewDisabledDialog(pending) }
                )
                else -> VRBStatus(
                    icon = STATUS_ICON_subscriptionEnding,
                    label = STATUS_LABEL_subscriptionEnding,
                    action = { showGenericSubscriptionEndingDialog(pending) }
                )
            }
        }
        else
        {
            return null
        }
    }

    private fun showArticleFailureDialog(article : DATA_Article)
    {
        context?.let { context ->
            dialog.confirm(
                "Article Failed",
                "An error occurred whilst processing this article. Would you like to open the website to try again?"
            )
            {
                if(it)
                {
                    val url = article.source.canonicalURL ?: article.source.contentURL;

                    openURL(context, url)
                }
            }
        }
    }


    private fun showAutoRenewDisabledDialog(pending : DATA_UserMembershipPending?)
    {
        dialog.alert(
            title = "Auto Renew Disabled",
            message = "${_subscriptionEndingLabel(pending)}You can enable it from the Verblr section of your phone settings"
        )
    }

    private fun showBillingErrorDialog()
    {
        dialog.alert(
            title ="Billing Exception",
            message = "Please update the payment information in your phone settings to avoid any interruption to your user experience"
        )
    }

    private fun showPendingChangeDialog(userMembership: DATA_UserMembership)
    {
        val pendingTier = userMembership?.pending?.tier;
        val pendingProductCadence = userMembership?.pending?.productCadence;

        if(pendingTier !== null && pendingProductCadence !== null)
        {
            dialog.alert(
                title = "Change Pending",
                message = "Your membership change to $pendingTier $pendingProductCadence is currently in progress"
            )
        }
    }



    private fun showGenericSubscriptionEndingDialog(pending : DATA_UserMembershipPending)
    {
        dialog.confirm(
            "Subscription Ending",
            "${_subscriptionEndingLabel(pending)}Would you like to renew?"
        )
        {
            if(it)
            {
                showUpsellOverlay()
            }
        }
    }


    private fun _subscriptionEndingLabel(pending : DATA_UserMembershipPending?) : String
    {
        val tierEndLabel = if(pending?.tierEndUTC != null)
            StringUtils.toDateLabel(pending?.tierEndUTC)
        else null

        return if(tierEndLabel != null) {
            "Your subscription ends $tierEndLabel."
        } else {
            ""
        }
    }

    private fun showUpsellOverlay()
    {
        // [dho] TODO NOTE if SKUs have not loaded then the buttons will say
        // unavailable, but I guess that's OK for now?? - 24/06/20
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