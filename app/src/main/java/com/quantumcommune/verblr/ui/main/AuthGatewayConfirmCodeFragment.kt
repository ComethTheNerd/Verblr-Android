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

class AuthGatewayConfirmCodeFragment(val email : String) : VRBScreen() {

    companion object {
        fun newInstance(email : String) = AuthGatewayConfirmCodeFragment(email)
    }

    private lateinit var codeInput : TextInputView;
    private lateinit var submitButton : TheButton;

    lateinit var adWrapper : AdWrapperView

    private var isSubmitting : Boolean = false
        set(value) {
            field = value
            refreshCodeInput()
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
            icon = R.drawable.checkmark_shield_fill_subwhite,
            title = "easy access",
            strapline = "now enter the secret code we just sent you"
        )


//        divider.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD,
//            HorizontalDividerView.DividerHeight
//        )

        codeInput = TextInputView.newInstance(context, inferCodeInputParams())

        codeInput.layoutParams = ConstraintLayout.LayoutParams(
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
        content.addView(codeInput)
        content.addView(submitButton)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(divider.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(divider.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(codeInput.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(codeInput.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(codeInput.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
//        constraints.setMargin(codeInput.id, ConstraintSet.TOP, VRBTheme.gutter)
        constraints.setMargin(codeInput.id, ConstraintSet.START, VRBTheme.gutter)
        constraints.setMargin(codeInput.id, ConstraintSet.END, VRBTheme.gutter)

        constraints.connect(submitButton.id, ConstraintSet.TOP, codeInput.id, ConstraintSet.BOTTOM)
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

    private fun refreshCodeInput()
    {
        codeInput.refresh(inferCodeInputParams())
    }

    private fun inferCodeInputParams() : TextInputViewParams
    {
        return TextInputViewParams(
            disabled = isSubmitting,
            label = "access code",
            onChange = { refreshSubmitButton() }
        )
    }

    private fun refreshSubmitButton()
    {
        submitButton.refresh(inferSubmitButtonParams())
    }

    private fun inferSubmitButtonParams() : TheButtonParams
    {
        val code = codeInput.text ?: ""

        val isAuthenticating = ipo.hasFlags(InProgressOperations.FLAG_authenticate)
        val label = if(isSubmitting || isAuthenticating) "loading..." else "sign in"
        val disabled = isSubmitting || isAuthenticating || code.isEmpty()

        return TheButtonParams(
            label = label,
            disabled = disabled,
            action = this::onSubmit
        )
    }

    private fun onSubmit()
    {
        val code = codeInput.text?.toString() ?: ""

        if(code.isNotEmpty())
        {
            isSubmitting = true

            api.completeEmailAuthChallenge(email = email, code = code)
            {
                data, err ->

                if(err != null)
                {
                    showAlertIfError(err)
                    isSubmitting = false
                }
                else if(data != null)
                {
                    player.clear()

                    auth.signInWithToken(data.token)
                    {
                            user, err ->

                        isSubmitting = false

                        if(err != null)
                        {
                            showAlertIfError(err)
                            isSubmitting = false
                        }
                        else
                        {
                            isSubmitting = false
                            viewModel.user.postValue(user)

                            bundleOps.consume(data.bundle, completion = this::showAlertIfError)
                        }
                    }
                }
                else
                {
                    showAlertIfError(Exception("Could not sign you in. Please try again"))
                }
            }
        }
        else
        {
            showAlertIfError(Exception("Please enter a valid code"))
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