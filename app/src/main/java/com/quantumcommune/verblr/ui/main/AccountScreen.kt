package com.quantumcommune.verblr.ui.main

import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.Observer
import com.quantumcommune.verblr.*
import java.util.*


class AccountScreen : VRBScreen() {

    companion object {
        fun newInstance() = AccountScreen()
    }

    private val REFRESH_MEMBERSHIP_FILTERS = arrayOf(BundleOps.FILTER_membership)

    lateinit var standardScreen : StandardScreenView
    lateinit var header : HeaderView

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
            title = toolbarTitleTextView("settings")
        )


        header = HeaderView.newInstance(context, inferHeaderViewParams(viewModel.user_membership.value))

        header.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )


        val divider = HorizontalDividerView.newInstance(context, HorizontalDividerViewParams(
            color = VRBTheme.COLOR_divider
        ))
        divider.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT,
            ConstraintLayout.LayoutParams.WRAP_CONTENT
        )

        val settingList = createSettingListView()
//        settingList.layoutParams = ConstraintLayout.LayoutParams(
//            ConstraintLayout.LayoutParams.MATCH_PARENT,
//            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT
//        )

        val versionInfoIcon = ImageView(context)
        versionInfoIcon.id = View.generateViewId()
        versionInfoIcon.layoutParams = ConstraintLayout.LayoutParams(
            VRBTheme.contextualIconDim,
            VRBTheme.contextualIconDim
        )
        versionInfoIcon.setImageResource(R.drawable.logo_mono)

        val versionInfoName = TextView(context)
        versionInfoName.id = View.generateViewId()
        versionInfoName.maxLines = 1
        versionInfoName.ellipsize = TextUtils.TruncateAt.END
        versionInfoName.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m10Size)
        versionInfoName.setTextColor(VRBTheme.COLOR_contextualInfo)
        versionInfoName.text = "v${getString(R.string.app_version)}"

        val versionInfoCopyright = TextView(context)
        versionInfoCopyright.id = View.generateViewId()
        versionInfoCopyright.maxLines = 1
        versionInfoCopyright.ellipsize = TextUtils.TruncateAt.END
        versionInfoCopyright.setTextSize(TypedValue.COMPLEX_UNIT_DIP, VRBTheme.m9Size)
        versionInfoCopyright.setTextColor(VRBTheme.COLOR_contextualInfo)
        versionInfoCopyright.text = "${getString(R.string.app_name)}©"

        val marginHorizontal = VRBTheme.gutter
        val marginVertical = VRBTheme.gutter

        val content = ConstraintLayout(context)
        content.id = View.generateViewId()

        content.layoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_PARENT
        )

        content.addView(header)
        content.addView(divider)
        content.addView(settingList)
        content.addView(versionInfoIcon)
        content.addView(versionInfoName)
        content.addView(versionInfoCopyright)

        val constraints = ConstraintSet()
        constraints.clone(content)

        constraints.connect(header.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP)
        constraints.centerHorizontally(header.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(header.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(header.id, ConstraintSet.END, marginHorizontal)

        constraints.connect(divider.id, ConstraintSet.TOP, header.id, ConstraintSet.BOTTOM)
        constraints.connect(divider.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(divider.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.setMargin(divider.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(divider.id, ConstraintSet.END, marginHorizontal)

        constraints.connect(settingList.id, ConstraintSet.TOP, divider.id, ConstraintSet.BOTTOM)
        constraints.connect(settingList.id, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
        constraints.connect(settingList.id, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
        constraints.connect(settingList.id, ConstraintSet.BOTTOM, versionInfoIcon.id, ConstraintSet.TOP)
        constraints.setMargin(settingList.id, ConstraintSet.TOP, marginVertical)
        constraints.setMargin(settingList.id, ConstraintSet.BOTTOM, marginVertical)
        constraints.setMargin(settingList.id, ConstraintSet.START, marginHorizontal)
        constraints.setMargin(settingList.id, ConstraintSet.END, marginHorizontal)

        constraints.connect(versionInfoIcon.id, ConstraintSet.BOTTOM, versionInfoName.id, ConstraintSet.TOP)
        constraints.centerHorizontally(versionInfoIcon.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(versionInfoIcon.id, ConstraintSet.TOP, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoIcon.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoIcon.id, ConstraintSet.END, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoIcon.id, ConstraintSet.BOTTOM, VRBTheme.smallGutter / 2)


        constraints.connect(versionInfoName.id, ConstraintSet.BOTTOM, versionInfoCopyright.id, ConstraintSet.TOP)
        constraints.centerHorizontally(versionInfoName.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(versionInfoName.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoName.id, ConstraintSet.END, VRBTheme.smallGutter)

        constraints.connect(versionInfoCopyright.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM)
        constraints.centerHorizontally(versionInfoCopyright.id, ConstraintSet.PARENT_ID)
        constraints.setMargin(versionInfoCopyright.id, ConstraintSet.START, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoCopyright.id, ConstraintSet.END, VRBTheme.smallGutter)
        constraints.setMargin(versionInfoCopyright.id, ConstraintSet.BOTTOM, VRBTheme.smallGutter)

        constraints.applyTo(content);

        content.setBackgroundColor(VRBTheme.COLOR_contentBG)

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

        return standardScreen
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if(!ipo.hasFlags(InProgressOperations.FLAG_fetchBundle))
        {
            api.fetchBundle(REFRESH_MEMBERSHIP_FILTERS, this::handleMembershipRefreshResult)
        }

        bindMiniPlayerToStateUpdates(standardScreen.miniPlayer)
        bindStatusBarToStateUpdates(standardScreen.statusBar)
        bindHeaderToStateUpdates(header)
    }

    private fun handleMembershipRefreshResult(data : DATA_Bundle?, err : Exception?)
    {
        if(err != null)
        {
            showToastIfError(err)
        }
        else if(data != null)
        {
            bundleOps?.consume(data, REFRESH_MEMBERSHIP_FILTERS, ::showToastIfError)
        }
    }

    private fun bindHeaderToStateUpdates(header : HeaderView)
    {
        ThreadUtils.ensureMainThreadExec {
            viewModel.user_membership.observe(this, Observer {
                refreshHeader(it)
            })
        }
    }

    private fun inferHeaderViewParams(userMembership: DATA_UserMembership?) : HeaderViewParams {

        if(userMembership != null && !membershipOps.isAnon(userMembership))
        {
            if(membershipOps.isPaid(userMembership))
            {
                val icon = R.drawable.star_fill_subwhite
                val tier = userMembership.tier
                val title = "$tier member"

                val pending = userMembership.pending

                // [dho] pending downgrade - 08/07/20
                if(pending?.tier === "standard")
                {
                    val pendingTierEndUTC = pending.tierEndUTC
                    val pendingTierEndLabel = if(pendingTierEndUTC != null)
                        StringUtils.toDateLabel(pendingTierEndUTC)
                    else null

                    return HeaderViewParams(
                        icon = icon,
                        title = title,
                        strapline = "ending ${pendingTierEndLabel ?: "soon"} (tap to renew)",
                        action = this::showUpsell
                    )
                }

                val expiresLabel = (
                    if(userMembership.expires != null)
                        StringUtils.toDateLabel(userMembership.expires, relative = false)?.toUpperCase(Locale.ROOT)
                    else null
                )

                if(expiresLabel != null)
                {
                    return HeaderViewParams(
                        icon = icon,
                        title = title,
                        strapline = "renewal due $expiresLabel"
                    )
                }
                else
                {
                    val joinedLabel = StringUtils.toDateLabel(userMembership.starts)?.toUpperCase(Locale.ROOT)

                    if(joinedLabel != null)
                    {
                        return HeaderViewParams(
                            icon = icon,
                            title = title,
                            strapline = "joined $joinedLabel"
                        )
                    }
                    else
                    {
                        return HeaderViewParams(
                            icon = icon,
                            title = title,
                            strapline = "thank you for your support"
                        )
                    }
                }
            }
            else {
                val icon = R.drawable.person_fill_subwhite
                val title = "standard member"

//                if(viewModel.show_upsells.value == true)
//                {
                return HeaderViewParams(
                    icon = icon,
                    title = title,
                    strapline = "tap to upgrade",
                    action = this::showUpsell
                )
//                }
            }
        }
        else
        {
            return HeaderViewParams(
                icon = R.drawable.person_fill_subwhite,
                title = "guest member",
                strapline = "tap to sign up or login",
                action = this::showAuthGateway
            )
        }
    }

    private fun refreshHeader(userMembership: DATA_UserMembership?)
    {
        header.refresh(inferHeaderViewParams(userMembership))
    }

    private fun createSettingListView() : ListView {
//        val marginHorizontal = VRBTheme.gutter
//        val marginVertical = VRBTheme.smallGutter
        val dividerHeight = VRBTheme.smallGutter

        val list = ListView(context);
        list.id = View.generateViewId()
        list.isVerticalScrollBarEnabled = false
        list.isHorizontalScrollBarEnabled = false

        val listLayoutParams = ConstraintLayout.LayoutParams(
            ConstraintLayout.LayoutParams.MATCH_PARENT,
            ConstraintLayout.LayoutParams.MATCH_CONSTRAINT_SPREAD
        )
        listLayoutParams.setMargins(0, 0, 0,0)
        list.layoutParams = listLayoutParams
        list.adapter = AccountSettingListViewAdapter(context, AccountSettingListViewParams(
            items = getSettingListItems()
        ))
        list.divider = VRBTheme.GRAD_transparent
        list.dividerHeight = dividerHeight;

        return list
    }




    private fun getSettingListItems() : List<AccountSettingViewParams>
    {
        val config = viewModel.user_config.value
        val isInviteOnly = config?.invite_Only_Enabled ?: false
        val manageSubscriptionsURL = config?.manageSubscriptionsURL
        val membership = viewModel.user_membership.value
        val showSignUp = membershipOps.isAnon(membership) && !isInviteOnly
        val showSignIn = membershipOps.isNullOrAnon(membership)
        val showSignOut = membershipOps.isAdmin(membership)
        val showDowngrade = membershipOps.isPaid(membership) && manageSubscriptionsURL?.isNotEmpty() == true
        val showRestorePurchases = !membershipOps.isNullOrAnon(membership)
//        val showInviteOthersLink = membership != null && !membershipOps.isAnon(membership)
        val isAuthenticating = ipo.hasFlags(InProgressOperations.FLAG_authenticate)
        val isPurchasing = ipo.hasFlags(InProgressOperations.FLAG_purchase)

        val supportURL = config?.supportURL
        val privacyPolicyURL = config?.privacyPolicyURL
        val termsOfUseURL = config?.termsOfUseURL

        val list = mutableListOf<AccountSettingViewParams>()

        if(showSignUp)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.plus_electro,
                    label = "create account",
                    disabled = isAuthenticating,
                    action = this::showAuthGateway
                )
            )
        }

        list.add(
            AccountSettingViewParams(
                icon = R.drawable.person_2_electro,
                label = if(showSignIn) "sign in" else "switch accounts",
                disabled = isAuthenticating,
                action = this::showAuthGateway
            )
        )

        if(showSignOut)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.person_2_electro,
                    label = "sign out",
                    disabled = isAuthenticating
                )
                {
                    auth?.signOut {
                            err ->
                        if(err != null)
                        {
                            dialog?.alert(message = err.localizedMessage)
                        }
                        else
                        {
                            player.clear()
                            viewModel.user.postValue(null)
                        }
                    }
                }
            )
        }


        if(showRestorePurchases)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.restore_purchased_electro,
                    label = "restore purchases",
                    disabled = isPurchasing
                )
                {
                    shop?.processAnyWaitingPurchases {
                            err ->
                        if(err != null)
                        {
                            showAlertIfError(err)
                        }
                        else
                        {
                            dialog?.toast("Done")
                        }
                    }
                }
            )
        }

        list.add(
            AccountSettingViewParams(
                icon = R.drawable.trash_electro,
                label = "delete offline audio"
            )
            {
                localCache?.deleteCachedPerformances {
                        err ->
                    if(err != null)
                    {
                        dialog.toast(err.localizedMessage ?: "Something went wrong")
                    }
                    else
                    {
                        dialog.toast("Offline audio files deleted")
                    }
                }
            }
        )


        if(showDowngrade)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.downgrade_electro,
                    label = "cancel subscription"
                )
                {
                    val a = activity;

                    if(a != null && manageSubscriptionsURL?.isNotEmpty() == true)
                    {
                        openURL(a, manageSubscriptionsURL)
                    }
                    else
                    {
                       showAlertIfError(Exception("Could not load subscriptions URL"))
                    }
                }
            )
        }

        if(supportURL?.isNotEmpty() == true)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.quote_bubble_electro,
                    label = "support"
                )
                {
                    activity?.let {
                        openURL(it, supportURL)
                    }
                }
            )
        }

        if(privacyPolicyURL?.isNotEmpty() == true)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.fingerprint_electro,
                    label = "privacy policy"
                )
                {
                    activity?.let {
                        openURL(it, privacyPolicyURL)
                    }
                }
            )
        }

        if(termsOfUseURL?.isNotEmpty() == true)
        {
            list.add(
                AccountSettingViewParams(
                    icon = R.drawable.list_electro,
                    label = "terms of use"
                )
                {
                    activity?.let {
                        openURL(it, termsOfUseURL)
                    }
                }
            )
        }


        return list
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

    private fun showUpsell()
    {
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




data class AccountSettingListViewParams(
    val items : List<AccountSettingViewParams>
);

private class AccountSettingListViewAdapter(private val context : Context?, private val params : AccountSettingListViewParams) : BaseAdapter()
{
    private val ids = params.items.map { nextID() }

    companion object {
        private var idSeed : Long = 0

        @Synchronized fun nextID() : Long = ++idSeed
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {

        val item = params.items[position]

        return when(convertView)
        {
            is AccountSettingView -> {
                convertView.refresh(item)
                convertView
            }
            else -> AccountSettingView.newInstance(context, item)
        }
    }

    override fun getItem(position: Int): Any = params.items[position]

    override fun getItemId(position: Int): Long =
        ids[position] ?: error("ID could not be retrieved from library feed")

    override fun getCount(): Int = params.items.count()
}