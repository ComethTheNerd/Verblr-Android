package com.quantumcommune.verblr.ui.main

import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import com.quantumcommune.verblr.ThreadUtils
import java.util.*

class NavItem<F>(val key : String,
//               val title : String? = null,
//               val status: Status? = null,
               val item : F//,
//               val rightButton : Fragment? = null
) {

}

abstract class Nav<F>(private val stack: MutableLiveData<Stack<NavItem<F>>>) {
    private val mLockObject = Object()

    fun keyMatcher(n : NavItem<F>, key : String) = n.key == key
    fun anyMatcher(n : NavItem<F>) = true

    fun isShowing(predicate: (f: NavItem<F>) -> Boolean): Boolean {
        synchronized(mLockObject)
        {
            val navItem = peek()

            if (navItem != null) {
                return predicate(navItem)
            } else {
                return false
            }
        }
    }

    protected open fun willRemove(navItem: NavItem<F>) : Unit {}

    fun remove(f: F) {

        synchronized(mLockObject)
        {
            val s = stack.value ?: Stack()

            removeAllMatches(s) { n -> n.item === f }

            updateObservers(s)
        }

    }

    fun removeAll(matcher : (NavItem<F>) -> Boolean) {

        synchronized(mLockObject)
        {
            val s = stack.value ?: Stack()

            removeAllMatches(s, matcher)

            updateObservers(s)
        }

    }

    fun peek(): NavItem<F>? {
        synchronized(mLockObject)
        {
            val s = stack.value

            return if(s?.isNotEmpty() == true) s.peek() else null
        }
    }

    fun popIfTop(f: Fragment) {

        synchronized(mLockObject)
        {
            val s = stack.value

            if (s?.isEmpty() == false) {
                val top = s.peek()

                if (top.item == f) {
                    s.pop()
                    updateObservers(s)
                }
            }
        }

    }

    fun push(navItem: NavItem<F>, removeAllExisting: ((NavItem<F>) -> Boolean)? = null) {
        synchronized(mLockObject)
        {
            val s = stack.value

            if (s == null) {
                val newStack = Stack<NavItem<F>>()
                newStack.push(navItem)

                updateObservers(newStack)
            } else {
                removeAllExisting?.let {
                    removeAllMatches(s, removeAllExisting)
                }

                s.push(navItem)

                updateObservers(s)
            }
        }
    }

    private fun removeAllMatches(s: Stack<NavItem<F>>, predicate: (NavItem<F>) -> Boolean): Int {

        val pre = s.count()

        s.removeAll {
            if (predicate(it)) {
                willRemove(it)

                return@removeAll true;
            } else {
                return@removeAll false
            }
        }

        return pre - s.count()
    }

    fun clear() {

        synchronized(mLockObject)
        {
            val s = stack.value ?: Stack<NavItem<F>>()

            s.clear()

            updateObservers(s)
        }
    }

    fun back()
    {
        synchronized(mLockObject)
        {
            val s = stack.value ?: Stack<NavItem<F>>()

            if (s.isNotEmpty()) {
                s.pop()
            }

            updateObservers(s)

        }
    }

    @Synchronized fun updateObservers(newStack : Stack<NavItem<F>>)
    {
        // [dho] adapted from : https://stackoverflow.com/a/49022687/300037 - 17/05/20
        ThreadUtils.ensureMainThreadExec { stack.value = newStack }
    }
}

open class FragmentNav<F : Fragment>(stack: MutableLiveData<Stack<NavItem<F>>>) : Nav<F>(stack)
{
    override fun willRemove(navItem: NavItem<F>) {
        super.willRemove(navItem)

        val fragmentManager = navItem.item.fragmentManager

        fragmentManager?.beginTransaction()?.remove(navItem.item)?.commit()
    }
}

class ScreenNav(viewModel : VRBViewModel) : FragmentNav<VRBScreen>(viewModel.nav_screens)
{
    companion object {
        val KEY_articleDetailViewNavKeyPrefix = "ArticleDetailView::"
        val KEY_authGatewayViewNavKey = "AuthGatewayView"
        val KEY_authGatewayViewConfirmCodeNavKey = "AuthGatewayConfirmCodeView"
        val KEY_accountViewNavKey = "AccountView"
        val KEY_storeViewNavKey = "StoreView"
        val KEY_howToViewNavKey = "HowToView";


    }

    fun articleDetailsViewKey(articleID : String) : String = "$KEY_articleDetailViewNavKeyPrefix$articleID"
    fun isArticleDetailsViewKey(key : String) = key.startsWith(KEY_articleDetailViewNavKeyPrefix)
    fun articleDetailsViewMatcher(n : NavItem<VRBScreen>) = isArticleDetailsViewKey(n.key)
    fun authGatewayViewMatcher(n : NavItem<VRBScreen>) = keyMatcher(n, KEY_authGatewayViewNavKey)
}
class OverlayNav(viewModel : VRBViewModel) : FragmentNav<VRBOverlay>(viewModel.nav_overlays)
{
    companion object {
        val KEY_maxiPlayerViewNavKey = "MaxiPlayer"
        val KEY_upsellViewNavKey = "Upsell"

//        fun playerOverlayMatcher(n : NavItem<VRBOverlay>) = n.key == OverlaysNav.KEY_maxiPlayerViewNavKey
//        fun upsellOverlayMatcher(n : NavItem<VRBOverlay>) = n.key == OverlaysNav.KEY_upsellViewNavKey
    }
    fun playerOverlayMatcher(n : NavItem<VRBOverlay>) = keyMatcher(n, OverlayNav.KEY_maxiPlayerViewNavKey)
    fun upsellOverlayMatcher(n : NavItem<VRBOverlay>) = keyMatcher(n, OverlayNav.KEY_upsellViewNavKey)
}

class StatusNav(viewModel : VRBViewModel) : Nav<VRBStatus>(viewModel.nav_statuses)
{
    companion object {
        val KEY_addArticle = "AddArticle"
        val KEY_refreshBundle = "RefreshBundle"
        val KEY_purchasing = "Purchasing"
        val KEY_membership = "Membership"
        val KEY_processingDeepLink = "ProcessingDeepLink"
        val KEY_processingSharedURL = "ProcessingSharedURL"
    }

    fun addArticleMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_addArticle)
    fun refreshBundleMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_refreshBundle)
    fun purchasingMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_purchasing)
    fun membershipMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_membership)
    fun processingDeepLinkMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_processingDeepLink)
    fun processingSharedURLMatcher(n : NavItem<VRBStatus>) = keyMatcher(n, StatusNav.KEY_processingSharedURL)
}




