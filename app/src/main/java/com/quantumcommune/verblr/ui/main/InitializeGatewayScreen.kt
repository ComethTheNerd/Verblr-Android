package com.quantumcommune.verblr.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import com.quantumcommune.verblr.InProgressOperations
import com.quantumcommune.verblr.R

class InitializeGatewayScreen : VRBScreen() {

    companion object {
        fun newInstance() = InitializeGatewayScreen()
    }

    lateinit var adWrapper : AdWrapperView

    private lateinit var syncButton : TheButton

    private var isSubmitting : Boolean = false
        set(value) {
            field = value
            refreshSyncButton()
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val toolbarParams = ToolbarViewParams(
            leading = VRBLegacyViewFactory.headerIconButtonView(
                context,
                iconDrawable = R.drawable.chevron_left_subwhite
            )
            {
                screens.back()
            },
            title = toolbarTitleLogoView()
        )

        val headerParams = HeaderViewParams(
            title = "welcome",
            strapline = "we just need to check in with the server"
        )


        syncButton = TheButton.newInstance(context, inferSyncButtonParams())

        val content = ConstraintLayout(context)

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(syncButton)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(syncButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(syncButton.id, ConstraintSet.PARENT_ID)
//        constraints.setMargin(syncButton.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.applyTo(content);

        val standardGatewayParams = StandardGatewayViewParams(
            toolbarParams,
            headerParams,
            content
        )

        val standardGateway = StandardGatewayView.newInstance(context, standardGatewayParams)

        val ad = if(viewModel.show_ads.value == true) ads.getNextAdForPlacement(ads.PlacementID_Gateway) else null

        val adCard = (
            if(ad != null) AdCardView.newInstance(context, AdCardViewParams(ad = ad))
            else null
        )

        val adWrapperViewParams = AdWrapperViewParams(
            content = standardGateway,
            adCard = adCard
        )

        adWrapper = AdWrapperView.newInstance(context, adWrapperViewParams)

        return adWrapper
    }

    private fun refreshSyncButton()
    {
        syncButton.refresh(inferSyncButtonParams())
    }

    private fun inferSyncButtonParams() : TheButtonParams
    {
        val isFetchingBundle = ipo.hasFlags(InProgressOperations.FLAG_fetchBundle)
        val label = if(isSubmitting || isFetchingBundle) "loading..." else "refresh"
        val disabled = isSubmitting || isFetchingBundle

        return TheButtonParams(
            label = label,
            disabled = disabled,
            action = this::onSubmit
        )
    }

    private fun onSubmit()
    {
        isSubmitting = true

        api.fetchBundle { _, err ->
            showAlertIfError(err)
            isSubmitting = false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.ipo.observe(this, Observer { _ ->
            refreshSyncButton()
        })
    }


}