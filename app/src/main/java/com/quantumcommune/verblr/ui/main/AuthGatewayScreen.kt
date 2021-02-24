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
import com.quantumcommune.verblr.StringUtils

class AuthGatewayScreen : VRBScreen() {

    companion object {
        fun newInstance() = AuthGatewayScreen()
    }

    private lateinit var emailInput : TextInputView;
    private lateinit var submitButton : TheButton;

    lateinit var adWrapper : AdWrapperView
//    lateinit var standardGateway : StandardGatewayView

    private var isSubmitting : Boolean = false
        set(value) {
            field = value
            refreshEmailInput()
            refreshSubmitButton()
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
            icon = R.drawable.lock_shield_fill_subwhite,
            title = "easy access",
            strapline = "enter your email to get your sign in code"
        )


//        divider.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
//            HorizontalDividerView.DividerHeight
//        )

        emailInput = TextInputView.newInstance(context, inferEmailInputParams())

        emailInput.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        submitButton = TheButton.newInstance(context, inferSubmitButtonParams())


        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_divider
        ))
        divider.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        val content = ConstraintLayout(context)

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(divider)
        content.addView(emailInput)
        content.addView(submitButton)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(divider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(divider.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(emailInput.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(emailInput.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(emailInput.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.setMargin(emailInput.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(emailInput.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(emailInput.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(submitButton.id, ConstraintSet.TOP, emailInput.id, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(submitButton.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(submitButton.id, ConstraintSet.TOP, VRBTheme.gutter)

        constraints.applyTo(content);

        val standardGatewayParams = StandardGatewayViewParams(
            toolbarParams,
            headerParams,
            content
        )

        val standardGateway = StandardGatewayView.newInstance(context, standardGatewayParams)
        standardGateway.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

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


    private fun refreshEmailInput()
    {
        emailInput.refresh(inferEmailInputParams())
    }

    private fun inferEmailInputParams() : TextInputViewParams
    {
        return TextInputViewParams(
            type = android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS or android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS,
            disabled = isSubmitting,
            label = "email",
            onChange = { refreshSubmitButton() }
        )
    }


    private fun refreshSubmitButton()
    {
        submitButton.refresh(inferSubmitButtonParams())
    }

    private fun inferSubmitButtonParams() : TheButtonParams
    {
        val isAuthenticating = ipo.hasFlags(InProgressOperations.FLAG_authenticate)
        val label = if(isSubmitting || isAuthenticating) "loading..." else "request code"
        val disabled = isSubmitting || isAuthenticating || !StringUtils.isValidEmail(emailInput.text)

        return TheButtonParams(
            label = label,
            disabled = disabled,
            action = this::onSubmit
        )
    }

    private fun onSubmit()
    {
        val email = emailInput.text?.toString() ?: ""

        if(StringUtils.isValidEmail(email))
        {
            isSubmitting = true

            api.createEmailAuthChallenge(email)
            {
                err ->
                isSubmitting = false

                if(err != null)
                {
                    showAlertIfError(err)
                }
                else
                {
                    screens.push(
                        NavItem(
                            key = ScreenNav.KEY_authGatewayViewConfirmCodeNavKey,
                            item = AuthGatewayConfirmCodeFragment.newInstance(email))
                    )
                }
            }
        }
        else
        {
            showAlertIfError(Exception("Please enter a valid email address"))
            isSubmitting = false
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.ipo.observe(this, Observer<Int> { _ ->
            refreshSubmitButton()
        })
    }

}