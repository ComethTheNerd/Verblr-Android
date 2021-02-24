package com.quantumcommune.verblr.ui.main

import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Observer
import com.android.billingclient.api.SkuDetails
import com.quantumcommune.verblr.*
import java.util.*

class UpsellOverlay : VRBOverlay() {

    companion object {
        fun newInstance() = UpsellOverlay()
    }

    private val REFRESH_FILTERS = arrayOf(BundleOps.FILTER_membership, BundleOps.FILTER_config)

    private var overlayUpsell : OverlayUpsellView? = null

    private fun handleMembershipRefreshResult(data : DATA_Bundle?, err : Exception?)
    {
        if(err == null && data != null)
        {
            bundleOps?.consume(data, REFRESH_FILTERS) { err -> }
        }
    }

    override fun onCreateContentView(): View? {

        if(!ipo.hasFlags(InProgressOperations.FLAG_fetchBundle))
        {
            api.fetchBundle(REFRESH_FILTERS, this::handleMembershipRefreshResult)
        }


        val toolbarParams = ToolbarViewParams(
            leading = VRBLegacyViewFactory.headerIconButtonView(
                context,
                iconDrawable = R.drawable.chevron_down_contextualinfo,
                disabled = false,
                action = this::dismiss
            ),
            title = TextView(context).apply {
                this.id = View.generateViewId()
                this.setLines(1)
                this.ellipsize = TextUtils.TruncateAt.END
                this.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.maxiPlayerDateLabelFontSize)
                this.typeface = VRBTheme.TYPEFACE_semibold
                this.setTextColor(VRBTheme.COLOR_contextualInfo)
                this.text = "sign up today".toUpperCase(Locale.ROOT)
            }
        )

//        var ou = overlayUpsell
//
//        if(ou == null)
//        {
            val membership = viewModel.user_membership.value
            val ipo = viewModel.ipo.value
            val skus = viewModel.product_skus.value

            val overlayUpsellParams = inferOverlayUpsellParams(membership, ipo, skus)

            val ou = OverlayUpsellView.newInstance(context, overlayUpsellParams).apply {
                overlayUpsell = this

                this.layoutParams = ConstraintLayout.LayoutParams(
                    ConstraintLayout.LayoutParams.MATCH_PARENT,
                    ConstraintLayout.LayoutParams.MATCH_PARENT
                )

                bindOverlayUpsellToUpdates(this)
            }
//        }

        ou.setPadding(0, VRBTheme.gutter / 2, 0,0 )

        val view = StandardOverlayView.newInstance(
            context,
            StandardOverlayViewParams(
                toolbarParams = toolbarParams,
                content = ou
            )
        )

        view.layoutParams = LinearLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        )

        return view
    }

    protected fun bindOverlayUpsellToUpdates(overlayUpsell : OverlayUpsellView)
    {
        viewModel.ipo.observe(this, Observer {
            refreshOverlayUpsell(overlayUpsell, viewModel.user_membership.value, it, viewModel.product_skus.value)
        })

        viewModel.user_membership.observe(this, Observer {
            refreshOverlayUpsell(overlayUpsell, it, viewModel.ipo.value, viewModel.product_skus.value)
        })

        viewModel.product_skus.observe(this, Observer {
            refreshOverlayUpsell(overlayUpsell, viewModel.user_membership.value, viewModel.ipo.value, it)
        })
    }

    private fun refreshOverlayUpsell(overlayUpsell : OverlayUpsellView, membership : DATA_UserMembership?, ipo : Int?, skus : Map<String, SkuDetails>?)
    {
        val params = inferOverlayUpsellParams(membership, ipo, skus)

        overlayUpsell.refresh(params)
    }

    protected fun inferOverlayUpsellParams(membership : DATA_UserMembership?, ipo : Int?, skus : Map<String, SkuDetails>?) : OverlayUpsellViewParams
    {
        val pendingTier = membership?.pending?.tier
        val isPendingDowngrade = pendingTier != null && pendingTier != "premium"

        val isPurchasing = ipo != null && ((ipo and InProgressOperations.FLAG_purchase) == InProgressOperations.FLAG_purchase)
        val isPaidMembership = membershipOps.isPaid(membership)

        val isPremiumMonthly = isPaidMembership && membership?.productCadence == "monthly"
        val isPremiumAnnual = isPaidMembership && membership?.productCadence == "yearly"

        val premMonthly = getPremiumProduct("monthly")
        val premAnnual = getPremiumProduct("yearly")

        val premMonthlySKU = if(premMonthly != null) skus?.get(premMonthly.productID) else null
        val premMonthlyCTALabel = (
            if(premMonthlySKU == null)
                "unavailable"
            else if(isPremiumMonthly)
                if(isPendingDowngrade)
                    "renew"
                else
                    "subscribed"
            else if(isPremiumAnnual)
                "crossgrade"
            else
                "subscribe"
        )
        val premMonthlyCTADisabled = premMonthlySKU == null || isPurchasing || (isPremiumMonthly && !isPendingDowngrade)

        val premAnnualSKU = if(premAnnual != null) skus?.get(premAnnual.productID) else null
        val premAnnualCTALabel = (
            if(premAnnualSKU == null)
                "unavailable"
            else if(isPremiumAnnual)
                if(isPendingDowngrade)
                    "renew"
                else
                    "subscribed"
            else if(isPremiumMonthly)
                "crossgrade"
            else
                "subscribe"
        )
        val premAnnualCTADisabled = premAnnualSKU == null || isPurchasing || (isPremiumAnnual && !isPendingDowngrade)

        return OverlayUpsellViewParams(
            subscribeMonthlyCTA = ProductButtonViewParams(
                paymentCadenceLabel = "MONTHLY",
                priceLocalized = premMonthlySKU?.price,
                cta = TheButtonParams(
                    label = premMonthlyCTALabel,
                    disabled = premMonthlyCTADisabled
                )
                {
                    tryPurchase(premMonthlySKU)
                }
            ),
            subscribeYearlyCTA = ProductButtonViewParams(
                paymentCadenceLabel = "YEARLY",
                priceLocalized = premAnnualSKU?.price,
                cta = TheButtonParams(
                    label = premAnnualCTALabel,
                    disabled = premAnnualCTADisabled
                )
                {
                    tryPurchase(premAnnualSKU)
                }
            )
        )
    }

    private fun tryPurchase(skuDetails: SkuDetails?)
    {
        if(skuDetails == null)
        {
            defaultCompletionHandler(Exception("Product unavailable. Please try again later"))
        }
        else
        {
            shop.checkout(skuDetails) { err ->
                if(err != null)
                {
                    defaultCompletionHandler(err);
                }
                else
                {
//                    dismiss()
                }
            }
        }
    }

    private fun getPremiumProduct(paymentCadence: String)
        = viewModel.user_config.value?.products?.values?.firstOrNull {
            it.productGroupID == "subscription" && it.kind == "premium-subscription" &&
                    it.paymentCadence == paymentCadence && it.available
        }
}