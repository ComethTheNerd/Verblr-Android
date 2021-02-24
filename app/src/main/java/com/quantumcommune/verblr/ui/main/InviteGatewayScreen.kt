package com.quantumcommune.verblr.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import com.quantumcommune.verblr.BundleOps
import com.quantumcommune.verblr.DATA_Bundle
import com.quantumcommune.verblr.StringUtils
import com.quantumcommune.verblr.openMailTo

class InviteGatewayScreen : VRBScreen() {

    companion object {
        fun newInstance() = InviteGatewayScreen()
    }

    lateinit var adWrapper : AdWrapperView

    private lateinit var submitButton : TheButton
    private lateinit var existingInviteOrAccountLink : LinkView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {

        val toolbarParams = ToolbarViewParams(
            leading = toolbarRefreshButtonView(arrayOf(
                BundleOps.FILTER_config
            ), this::onRefreshComplete),
            title = toolbarTitleLogoView()
        )

        val headerParams = HeaderViewParams(
            title = "hello there",
            strapline = "the service is currently invite only"
        )

        submitButton = TheButton.newInstance(context, inferSubmitButtonParams())
        existingInviteOrAccountLink = LinkView.newInstance(context, inferExistingInviteOrAccountLinkParams())


        val content = ConstraintLayout(context)

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(submitButton)
        content.addView(existingInviteOrAccountLink)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(submitButton.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(submitButton.id, ConstraintSet.PARENT_ID)
//        constraints.setMargin(submitButton.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.setMargin(existingInviteOrAccountLink.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.connect(existingInviteOrAccountLink.id, ConstraintSet.TOP, submitButton.id, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(existingInviteOrAccountLink.id, ConstraintSet.PARENT_ID)

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

    private fun inferSubmitButtonParams() : TheButtonParams
    {
        val label = "request invite"
        val disabled = false

        return TheButtonParams(
            label = label,
            disabled = disabled,
            action = this::onSubmit
        )
    }

    private fun inferExistingInviteOrAccountLinkParams() : LinkViewParams
    {
        val label = "I have an invite or account already"
        val disabled = false

        return LinkViewParams(
            label = label,
            disabled = disabled,
            action = this::onExistingInvitationOrAccount
        )
    }

    private fun onSubmit()
    {
        val userConfig = viewModel.user_config.value

        if(userConfig != null)
        {
            val inviteEmail = userConfig.invite_Email
            val inviteEmailSubject = StringUtils.urlEncoded(userConfig.invite_EmailSubject)
            val inviteEmailBody = StringUtils.urlEncoded(userConfig.invite_EmailBody)

            if(inviteEmailSubject != null && inviteEmailBody != null)
            {
                openMailTo(activity as Activity, inviteEmail, inviteEmailSubject, inviteEmailBody)

                return;
            }
        }

        // [dho] fall through to here if we could not create invitation - 22/06/20
        showAlertIfError(Exception("Could not load invitation config. Try refreshing the app and trying again"))
    }

    private fun onRefreshComplete(bundle : DATA_Bundle?, err : Exception?)
    {
        if(err != null)
        {
            showToastIfError(err)
        }
        else
        {
            // [dho] NOTE do not need to do anything because the activity
            // will tear down this gateway if the invite only mode is no longer
            // enabled - 25/06/20
        }
    }

    private fun onExistingInvitationOrAccount()
    {
        screens.push(NavItem(
            key = ScreenNav.KEY_authGatewayViewNavKey,
            item = AuthGatewayScreen.newInstance()
        ))
    }
}