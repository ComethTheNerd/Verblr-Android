package com.quantumcommune.verblr

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.android.billingclient.api.SkuDetails
import com.google.android.gms.ads.identifier.AdvertisingIdClient
import com.google.android.gms.common.GooglePlayServicesNotAvailableException
import com.google.android.gms.common.GooglePlayServicesRepairableException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.quantumcommune.verblr.ui.main.*
import java.io.IOException
import java.util.*
import java.util.concurrent.atomic.AtomicInteger


class MainActivity : AppCompatActivity() {


    lateinit var viewModel: VRBViewModel
    lateinit var localCache: LocalCache
    lateinit var bundleOps : BundleOps
    lateinit var membershipOps : MembershipOps
    lateinit var api : API
    lateinit var firebaseWrapper: FirebaseWrapper
    lateinit var ipo : InProgressOperations
    lateinit var dialog : Dialog
    lateinit var screens : ScreenNav
    lateinit var overlays : OverlayNav
    lateinit var statuses : StatusNav
    lateinit var network : Network
    lateinit var player : Player
    lateinit var notifications : Notifications
    lateinit var shop : Shop
    lateinit var platform : String
    lateinit var ads : AdVendor

//    var mediaPlayerService : LocalMediaPlayerService? = null

    private val SPLASH_FLASH_PREVENTION_DELAY_MS = 1000L
    private val LOAD_HTML_INFO_TIMEOUT_MS = 15000L

    private val FLAG_initEnsureUser = 0x1
    private val FLAG_initRestoreBundle = 0x2
    private val FLAG_initRefreshBundleIfConnected = 0x4
    private val FLAG_initMediaPlayer = 0x8
    private val FLAG_initProcessAnyWaitingPurchasesIfConnected = 0x10
//    private val FLAG_initAdProvider = 0x20

    private val MASK_initAll = (
        FLAG_initEnsureUser or FLAG_initRestoreBundle or
        FLAG_initRefreshBundleIfConnected or FLAG_initMediaPlayer or
        FLAG_initProcessAnyWaitingPurchasesIfConnected// or
//        FLAG_initAdProvider
    )


    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {

        // [dho] "Make sure this is before calling super.onCreate"
        // adapted from : https://android.jlelse.eu/launch-screen-in-android-the-right-way-aca7e8c31f52
        // - 24/06/20
        setTheme(R.style.AppTheme);

        super.onCreate(savedInstanceState)

//        val displayMetrics = DisplayMetrics()
//        windowManager.defaultDisplay.getMetrics(displayMetrics)
//        val height = displayMetrics.heightPixels
//        val width = displayMetrics.widthPixels

        // [dho] Remove title bar. Do not put this in `configureWindow`
        // because it will cause an exception if it is used after `setContentView` - 24/06/20
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        configureWindow()

        setContentView(R.layout.main_activity)

        showScreen(SplashScreen.newInstance())

        // [dho] NOTE do NOT use `applicationContext` here because it causes a crash
        // if you try to do things like show a dialog after failed initialization, and in
        // general we should only use application context if we need a context separate to the
        // activity lifecycle - 09/06/20
        val context = this//.applicationContext
//        val hasSavedInstanceState = savedInstanceState != null

        viewModel = ViewModelProviders.of(this).get(VRBViewModel::class.java)
        localCache = LocalCache(this, viewModel)
        bundleOps = BundleOps(context, viewModel, localCache)
        membershipOps = MembershipOps()
        api = API(context, viewModel)
        firebaseWrapper = FirebaseWrapper(this)
        ipo = InProgressOperations(viewModel)
        dialog = Dialog(context)
        screens = ScreenNav(viewModel)
        overlays = OverlayNav(viewModel)
        statuses = StatusNav(viewModel)
        network = Network(context)
        notifications = Notifications(context)
        shop = Shop(context, api, viewModel)
        platform = getString(R.string.platform)
        player = Player(this, viewModel, localCache, api, network)
        ads = AdVendor(context)

        // [dho] background thread for init - 20/05/20
        ThreadUtils.ensureBGThreadExec {
            init()
        }
    }

    @Synchronized fun init(req : Int = MASK_initAll)
    {
        var initToDoFlags = req
        var initFailedFlags = 0x0
        val initErrors = arrayListOf<Exception>()

        if(initToDoFlags == 0)
        {
            checkInit(initToDoFlags, initFailedFlags, initErrors)
            return
        }

        ipo.addFlags(InProgressOperations.FLAG_initialize)

        val willEnsureUser = (initToDoFlags and FLAG_initEnsureUser) == FLAG_initEnsureUser
        val willRestoreBundle = (initToDoFlags and FLAG_initRestoreBundle) == FLAG_initRestoreBundle
        val willRefreshBundleIfConnected = (initToDoFlags and FLAG_initRefreshBundleIfConnected) == FLAG_initRefreshBundleIfConnected
        val willInitMediaPlayer = (initToDoFlags and FLAG_initMediaPlayer) == FLAG_initMediaPlayer
        val willProcessAnyWaitingPurchasesIfConnected = (initToDoFlags and FLAG_initProcessAnyWaitingPurchasesIfConnected) == FLAG_initProcessAnyWaitingPurchasesIfConnected
//        val willInitAdProvider = (initToDoFlags and FLAG_initAdProvider) == FLAG_initAdProvider

        if(willRestoreBundle)
        {
            bundleOps.restore { restoredBundle, err ->

                if(err != null)
                {
                    initErrors.add(err)

                    initFailedFlags = initFailedFlags or FLAG_initRestoreBundle
                }

                initToDoFlags = initToDoFlags and FLAG_initRestoreBundle.inv()

                checkInit(initToDoFlags, initFailedFlags, initErrors)
            }
        }

        if(willEnsureUser)
        {
            ensureUserOnInit(willRefreshBundleIfConnected)
            {
                user, bundle, err ->

                if(err != null)
                {
                    initErrors.add(err)

                    if(user == null)
                    {
                        initFailedFlags = initFailedFlags or FLAG_initEnsureUser
                    }

                    if(willRefreshBundleIfConnected && bundle == null)
                    {
                        initFailedFlags = initFailedFlags or FLAG_initRefreshBundleIfConnected
                    }
                }

                initToDoFlags = initToDoFlags and FLAG_initEnsureUser.inv()

                if(willRefreshBundleIfConnected)
                {
                    initToDoFlags = initToDoFlags and FLAG_initRefreshBundleIfConnected.inv()
                }

                checkInit(initToDoFlags, initFailedFlags, initErrors)
            }
        }
        else if(willRefreshBundleIfConnected)
        {
            refreshBundle()
            {
                _, err ->

                // [dho] if user is offline then we should not block them
                // continuing to use the app - 09/06/20
                suppressIfNetworkError(err)?.let {
                    initErrors.add(it)
                    initFailedFlags = initFailedFlags or FLAG_initRefreshBundleIfConnected
                }

                initToDoFlags = initToDoFlags and FLAG_initRefreshBundleIfConnected.inv()

                checkInit(initToDoFlags, initFailedFlags, initErrors)
            }
        }

        if(willInitMediaPlayer)
        {
            initMediaPlayer()
            {
                err ->
                    if(err != null)
                    {
                        initFailedFlags = initFailedFlags or FLAG_initMediaPlayer
                    }

                    initToDoFlags = initToDoFlags and FLAG_initMediaPlayer.inv()

                    checkInit(initToDoFlags, initFailedFlags, initErrors)
            }
        }

        if(willProcessAnyWaitingPurchasesIfConnected)
        {
            ThreadUtils.ensureBGThreadExec {
                shop?.processAnyWaitingPurchases {
                    err ->
                    //                    initFailedFlags = initFailedFlags or FLAG_initProcessWaitingPurchasesIfConnected

                    initToDoFlags = initToDoFlags and FLAG_initProcessAnyWaitingPurchasesIfConnected.inv()

                    checkInit(initToDoFlags, initFailedFlags, initErrors)
                }
            }
        }

//        if(willInitAdProvider)
//        {
//            ads.init {
//                err ->
//
//                // not treating error as failure.. no ads, no big deal - 15/06/20
//                if(err !== null)
//                {
//                    Log.e("ADS", err.localizedMessage)
//                }
//
//                initToDoFlags = initToDoFlags and FLAG_initAdProvider.inv()
//
//                checkInit(initToDoFlags, initFailedFlags, initErrors)
//            }
//        }
    }

    private fun ensureUserOnInit(willRefreshBundleIfConnected : Boolean, completion : (user : FirebaseUser?, bundle : DATA_Bundle?, error : Exception?) -> Unit)
    {
        firebaseWrapper.ensureUser { user, err ->

            if(err != null)
            {
                completion(null, null, err)
            }
            else {
                ThreadUtils.ensureMainThreadExec {
                    viewModel.user.value = user

                    if(willRefreshBundleIfConnected)
                    {
                        ThreadUtils.ensureBGThreadExec {
                            refreshBundle{
                                    bundle, err ->

                                // [dho] if user is offline then we should not block them
                                // continuing to use the app - 09/06/20
                                completion(user, bundle, suppressIfNetworkError(err))
                            }
                        }
                    }
                    else
                    {
                        completion(user, null, null)
                    }
                }
            }
        }
    }

    private fun suppressIfNetworkError(err : Exception?) : Exception?
    {
        if(err !== null)
        {
            // [dho] TODO CLEANUP HACK - 09/06/20
            val isNetworkError = err.message?.startsWith("A network error") ?: false;

            if(!isNetworkError)
            {
                return err
            }
        }

        return null
    }

    @Synchronized private fun checkInit(initToDoFlags: Int, initFailedFlags : Int, initErrors : ArrayList<Exception>)
    {
        if(initToDoFlags == 0)
        {
            if(initErrors.count() > 0)
            {
                dialog.alert(message = initErrors!![0].localizedMessage)
                {
                    dialog.toast("Retrying initialization...");
                    setTimeout({
                        init(initFailedFlags)
                    }, (Dialog.ToastDuration + 200).toLong())
                }
            }
            else
            {
                ThreadUtils.ensureMainThreadExec {

                    // [dho] when purchases result in a membership update, we need to make sure
                    // we consume it - 11/06/20
                    ShopDataBridge.membershipUpdate.observe(this, Observer {
                        bundleOps?.consume(bundle = DATA_Bundle(membership = it), filters = arrayOf(BundleOps.FILTER_membership))
                        {
                                err -> if(err !== null)
                        {
                            dialog?.alert(message = err.localizedMessage)
                        }
                        }
                    })

                    PlayerDataBridge.currentMediaID.observe(this, Observer {
                        // [dho] when the user starts playing an article,
                        // then backgrounds the app, and then relaunches the app from the media notification
                        // in the system tray, we need to retrieve the item that was playing and the state it is
                        // - 23/06/20
                        val restoredArticleID = PlayerDataBridge.currentMediaID.value
                        val restoredArticle = viewModel.user_library.value?.articles?.get(restoredArticleID)

                        viewModel.player_article.postValue(restoredArticle)
                    })

                    PlayerDataBridge.state.observe(this, Observer {
                        viewModel.player_state.postValue(it)
                    })

                    PlayerDataBridge.duration.observe(this, Observer {
                        viewModel.player_duration.postValue(it)
                    })

                    PlayerDataBridge.progress.observe(this, Observer {
                        viewModel.player_progress.postValue(it)
                    })

                    PlayerDataBridge.buffState.observe(this, Observer {
                        viewModel.player_buff_state.postValue(it)
                    })

                    PlayerDataBridge.buffProgress.observe(this, Observer {
//                        Log.i("BUFF PROGRESS", it.toString());
                        viewModel.player_buff_progress.postValue(it)
                    })

                    RemoteNotificationServiceDataBridge.token.observe(this, Observer {
                            token -> if(token !== null) onPushNotificationTokenChange(token)
                    })

                    RemoteNotificationServiceDataBridge.remoteMessage.observe(this, Observer {
                            message -> onPushNotificationReceived(message)
                    })

                    RemoteNotificationServiceDataBridge.recommendBundleRefresh.observe(this, Observer {
                            flag -> if(flag) onPushNotificationServiceRecommendsBundleRefresh()
                    })


                    viewModel.nav_screens.observe(this, Observer {
                            nav -> onScreenChange(nav)
                    });

                    viewModel.nav_overlays.observe(this, Observer {
                            nav -> onOverlayChange(nav)
                    });

                    viewModel.user.observe(this, Observer {
                            user -> onUserChange(user)
                    })


                    val statusUtils = StatusUtils(this, dialog, overlays)

                    viewModel.user_membership.observe(this, Observer {
                        ThreadUtils.ensureMainThreadExec {
                            viewModel.show_ads.value = shouldShowAds(it)
                            viewModel.show_upsells.value = shouldShowUpsells(it, viewModel.user_lastAccessed.value, viewModel.product_skus.value)

                            updateSingletonStatus(
                                StatusNav.KEY_membership,
                                statusUtils.inferMembershipStatus(it),
                                statuses::membershipMatcher
                            )
                        }
                    })

                    viewModel.user_lastAccessed.observe(this, Observer {
                        viewModel.show_upsells.postValue(
                            shouldShowUpsells(viewModel.user_membership.value, it, viewModel.product_skus.value)
                        )
                    })


                    viewModel.ipo.observe(this, Observer {
                        updateSingletonStatus(
                            StatusNav.KEY_refreshBundle,
                            statusUtils.inferRefreshBundleStatus(it),
                            statuses::refreshBundleMatcher
                        )

                        updateSingletonStatus(
                            StatusNav.KEY_purchasing,
                            statusUtils.inferPurchaseStatus(it),
                            statuses::purchasingMatcher
                        )

                        updateSingletonStatus(
                            StatusNav.KEY_addArticle,
                            statusUtils.inferAddArticleStatus(it),
                            statuses::addArticleMatcher
                        )
                    })

                    viewModel.product_skus.observe(this, Observer {
                        viewModel.show_upsells.postValue(
                            shouldShowUpsells(viewModel.user_membership.value, viewModel.user_lastAccessed.value, it)
                        )
                    })

                    viewModel.user_config.observe(this, Observer {
                            user_config -> onConfigurationChange(user_config)
                    })


                    ipo.removeFlags(InProgressOperations.FLAG_initialize)

                    handleIntent(intent)
                }
            }
        }
    }

    private fun updateSingletonStatus(key : String, status : VRBStatus?, matcher : (NavItem<VRBStatus>) -> Boolean)
    {
        if(status != null)
        {
            statuses.push(
                NavItem(
                    key = key,
                    item = status
                ),
                removeAllExisting = matcher
            )
        }
        else
        {
            statuses.removeAll(matcher)
        }
    }

//    private fun xxx(config : DATA_UserConfig?, membership: DATA_UserMembership?)
//    {
//        if(config != null)
//        {
//            val products = shop.fetchProductInfo(config.products)
//            {
//                skus, err -> {}
//            }
//        }
//
//        ads.init {
//            err ->
//        }

    //                val mm = viewModel.user_membership.value
//                if(mm != null && !membershipOps.isPaid(mm))
//                {
//                    // init shop and ads
//                }
//    }


    override fun onPause() {
        super.onPause()

        viewModel?.appEnteredBG.postValue(true)
    }

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onResume() {
        super.onResume()

        configureWindow();

        if(viewModel?.appEnteredBG.value == true)
        {
            // [dho] show a splash screen whilst the app catches up to
            // where the user was and refreshes any data - 07/06/20
            showScreen(SplashScreen.newInstance())

            val opsLeftToFinish = AtomicInteger(3)
            val lock = Object()
            val onResumeOpCompleted = {
                synchronized(lock)
                {
                    if(opsLeftToFinish.decrementAndGet() == 0) {
                        viewModel?.appEnteredBG.postValue(false)

                        showInferredCurrentScreen(
                            ipo = ipo,
                            userConfig = viewModel.user_config.value,
                            userMembership = viewModel.user_membership.value,
                            nav_routeStack = viewModel.nav_screens.value
                        )
                    }
                }
            }

            // [dho] this is gnarly, but the other operations for resuming sometimes
            // complete so quickly that the splash screen is torn down as soon
            // as it shows, causing an uncomfortable visual flash for the user!
            // this HACK means it will persist for at least the delay timer! - 24/06/20
            setTimeout(onResumeOpCompleted, SPLASH_FLASH_PREVENTION_DELAY_MS)

            refreshBundle{ _, err -> onResumeOpCompleted() }

            ThreadUtils.ensureBGThreadExec {
                shop?.processAnyWaitingPurchases { err -> onResumeOpCompleted() }
            }
        }
        else
        {
            // [dho] `onCreate` will handle the setup - 24/06/20
        }


    }

    private fun configureWindow()
    {
        hideBars()

        // [dho] this means that the background behind the dialog will be
        // the correct color - 24/06/20
        window.decorView.setBackgroundColor(VRBTheme.COLOR_toolbarBG)

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    }

    // [dho] adapted from : https://stackoverflow.com/a/22571619/300037 - 21/05/20
    private fun hideBars()
    {
        if (Build.VERSION.SDK_INT < 16)
        {
            // Hide the status bar
            window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            // Hide the action bar
            supportActionBar?.hide();
        }
        else
        {
            // Hide the status bar
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
            // Hide the action bar
            actionBar?.hide();
        }
    }

    private fun refreshBundle(completion : (bundle : DATA_Bundle?, err : Exception?) -> Unit)
    {
        api.fetchBundle {
                bundle, err ->

            if(err != null)
            {
                completion(null, err)
            }
            else
            {
                bundleOps.consume(bundle!!)
                {
                        err -> completion(if(err == null) bundle else null, err)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent) {
        // [dho] first we try to detect whether we were started from the user tapping an
        // FCM notification
        //
        // adapted from : https://firebase.google.com/docs/cloud-messaging/android/receive#handling_messages
        // - 08/06/20
        val extras = intent?.extras

        if(extras !== null)
        {
            val notificationType = extras.getString(Notifications.KEY_notificationType)
            val jsonString = extras.getString(Notifications.KEY_jsonString)

            if(notificationType !== null && jsonString !== null)
            {
                onLaunchedFromPushNotification(
                    notificationType = notificationType,
                    jsonString = jsonString
                )
                {
                    // callback
                }

                return;
            }
        }

        // [dho] otherwise try to infer where to take the user from the intent action
        //
        // adapted from : https://developer.android.com/training/sharing/receive#sharing-shortcuts-api
        // - 17/05/20
        when (intent?.action) {
            Intent.ACTION_VIEW -> {
                val appLinkData: Uri? = intent.data

                if(appLinkData !== null)
                {
                    statuses.push(
                        navItem = NavItem(
                            key = StatusNav.KEY_processingDeepLink,
                            item = VRBStatus(
                                label = "inspecting deep link..."
                            )
                        ),
                        removeAllExisting = statuses::processingDeepLinkMatcher
                    )

                    onLaunchedFromDeepLink(appLinkData)
                    {
                        statuses.removeAll(statuses::processingDeepLinkMatcher)
                    }
                }
            }
            Intent.ACTION_SEND -> {
                if (intent.type == "text/plain") {
                    onLaunchedFromSharingText(intent)
                    {

                    }
                }
//                else if(intent.type === "application/pdf")
//                {
//                    val i = 0;
//                }
            }
            else -> {
                // Handle other intents, such as being started from the home screen
            }
        }
    }

    private fun onLaunchedFromPushNotification(notificationType : String, jsonString : String, completion: () -> Unit) {
        ThreadUtils.ensureBGThreadExec {
            when(notificationType)
            {
                notifications.TYPE_userAddArticleToUserLibrary -> {
                    // [dho] FCM can only accept data as maps of string to string so we
                    // serialize the actual data as a json string mapped to a single `json`
                    // key, and deserialize it here to the appropriate target type - 08/06/20
                    var payload: DATA_UserAddArticleToUserLibraryNotificationPayload? = null;

                    try {
                        payload = Gson().fromJson(
                            jsonString,
                            DATA_UserAddArticleToUserLibraryNotificationPayload::class.java
                        )
                    } catch (err: JsonSyntaxException) {
                    }

                    if(payload !== null) {
                        val url = payload.url
                        val status = payload.status

                        if (status == "success") {
                            val article = payload.article

                            if(article !== null)
                            {
                                // [dho] jump to the article details - 08/06/20
                                screens?.push(NavItem(
                                    key = screens.articleDetailsViewKey(article.id),
                                    item = ArticleDetailsScreen.newInstance(article)
                                ))
                            }
                            // [dho] also refresh the bundle - 08/06/20
                            refreshBundle { _, _ ->  completion() }

                        } else if (url !== null) {
                            val errorDescription = payload.errorDescription

                            val prefix = if(errorDescription?.isNotEmpty() == true) "$errorDescription. " else "";

                            dialog?.confirm(
                                "Article Failed",
                                "${prefix}Would you like to open the website and try again?"
                            ){
                                    result ->
                                if(result)
                                {
                                    openURL(this, url)
                                }

                                completion()
                            }
                        }
                    }
                    else {
                        completion()
                    }
                }
                else -> completion()
            }
        }
    }

    // [dho] https://developer.android.com/studio/write/app-link-indexing?utm_source=android-studio#associatesite - 11/06/20
    private fun onLaunchedFromDeepLink(deepLink : Uri, completion: () -> Unit)
    {
        val host = deepLink.host?.toLowerCase(Locale.ROOT)
        val path = deepLink.path?.toLowerCase(Locale.ROOT)
        val action = deepLink.getQueryParameter(getString(R.string.deep_link_param_name_action))?.toLowerCase(Locale.ROOT)
        val articleURL = deepLink.getQueryParameter(getString(R.string.deep_link_param_name_url))

        val isValidHostAndPath = (
            host == getString(R.string.deep_link_host) &&
            path == getString(R.string.deep_link_path)
        )

        if(isValidHostAndPath)
        {
            val isValidActionAndURL = (
                action == getString(R.string.deep_link_action_name_add) &&
                        StringUtils.isValidURL(articleURL)
            )

            if(isValidActionAndURL)
            {
                val preHash = getWaitUntilLibraryChangedHash(viewModel.user_library.value)

                api.addArticleToUserLibrary(url = articleURL!!, html = null)
                {
                    err ->

                    if(err !== null)
                    {
                        dialog.alert(
                            message = err.localizedMessage ?: "Something went wrong adding an article to your library",
                            completion = completion
                        )
                    }
                    // [dho] adapted from : https://stackoverflow.com/a/11649654/ - 11/06/20
                    else if(!NotificationManagerCompat.from(this).areNotificationsEnabled())
                    {
                        // [dho] user has NOT got notifications enabled so we warn them that
                        // their library will not auto refresh - 11/06/20
                        dialog.confirm(
                            getString(R.string.confirm_dialog_notifications_disabled_title),
                            getString(R.string.confirm_dialog_notifications_disabled_message)
                        )
                        {
                                shouldOpenSettings ->

                            if(shouldOpenSettings)
                            {
                                // [dho] adapted from : https://stackoverflow.com/a/6239044 - 11/06/20
                                startActivity(
                                    Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:$packageName")
                                    )
                                );

                                // [dho] NOTE not going to poll here because user is switching away
                                // from the app - 01/07/20
                                completion();

                            }
                            else
                            {
                                ThreadUtils.ensureBGThreadExec {
                                    waitUntilLibraryChanged(preHash) { completion() }
                                }

                            }
                        }
                    }
                    else
                    {
                        ThreadUtils.ensureBGThreadExec {
                            waitUntilLibraryChanged(preHash) { completion() }
                        }
                    }
                }
            }
            else
            {
                dialog.alert(
                    message = "The Verblr link is malformed. Please check the URL and try again",
                    completion = completion
                )
            }
        }
        else
        {
            // [dho] fallback to opening URL in web if we cannot handle it - 11/06/20
            openURL(this, deepLink.toString());
            completion()
        }
    }

    private fun tryParseClientSideHTML(url : String, completion: (html : String?, err: Exception?) -> Unit)
    {
        val webView = findViewById<WebView>(R.id.client_side_html_parser)

        if(webView == null)
        {
            completion(null, Exception("Could not retrieve web view"))
            return;
        }

        loadURL(webView, url)
        {
            err ->

            if(err != null)
            {
                completion(null, err)
            }
            else
            {
                ThreadUtils.ensureMainThreadExec {
                    webView.evaluateJavascript(
                        """
                            (function() {
            var url = document.URL;
            var html = document.documentElement.outerHTML;

            var isAMP = (
                document.documentElement.hasAttribute("⚡") ||
                document.documentElement.hasAttribute("amp")
            );

            var ampLink = document.head.querySelector("link[rel='amphtml']");
            var ampURL = !!ampLink ? (ampLink.getAttribute("href") || null) : null;

            return {
                "url" : url,
                "html" : document.documentElement.outerHTML,
                "isAMP" : isAMP,
                "ampURL" : ampURL
            }
        })();
                        """.trimIndent()
                    )
                    {
                            result ->

                        ThreadUtils.ensureBGThreadExec {
                            if(result?.isNotEmpty() == true)
                            {
                                val pageInfo : PARSE_ClientSidePageInfo? = try {
                                    Gson().fromJson(result, PARSE_ClientSidePageInfo::class.java)
                                }
                                catch(err : JsonSyntaxException)
                                {
                                    null
                                }

                                if(pageInfo != null)
                                {
                                    // [dho] we found an AMP URL should will try to retrieve that HTML
                                    // instead. Note, I'm being extra cautious with the guard to prevent
                                    // any inadvertent infinite page loading loops from malformed pages - 26/06/20
                                    if(!pageInfo.isAMP && pageInfo.ampURL != null && pageInfo.url != pageInfo.ampURL)
                                    {
                                        tryParseClientSideHTML(pageInfo.ampURL)
                                        {
                                            ampHTML, err ->

                                            if(ampHTML?.isNotEmpty() == true)
                                            {
                                                completion(ampHTML, null)
                                            }
                                            else
                                            {
                                                // [dho] fallback
                                                completion(pageInfo.html, null)
                                            }
                                        }
                                    }
                                    else
                                    {
                                        completion(pageInfo.html, null)
                                    }
                                }
                                else
                                {
                                    completion(null, Exception("Client side page info was malformed"));
                                }
                            }
                            else
                            {
                                completion(null, Exception("Could not retrieve HTML info from page"))
                            }
                        }
                    }
                }
            }
        }
    }

    private fun loadURL(webView : WebView, url : String, completion: (err: Exception?) -> Unit)
    {
        var timer : TimeoutTask? = null
        var lock = Object()
        var didSettle = false
        var didReceivePageStartEvent = false
        var didReceivePageFinishedEvent = false
        var didReceiveProgressCompleteEvent = false

        val cleanup = {
            timer?.cancel?.invoke()
            ThreadUtils.ensureMainThreadExec {
                webView.webChromeClient = null
                webView.webViewClient = null
                webView.loadUrl("about:blank")
            }
        }

        val notifyIfDone = {
            var isDone = false
            synchronized(lock)
            {
                isDone = (
                    !didSettle &&
                    didReceivePageStartEvent &&
                    didReceivePageFinishedEvent &&
                    didReceiveProgressCompleteEvent
                )

                if(isDone)
                {
                    cleanup()
                    didSettle = true
                }
            }

            if(isDone)
            {
                completion(null)
            }
        }

        ThreadUtils.ensureMainThreadExec {
            var running = 0

            webView.settings.javaScriptEnabled = true

            webView.webChromeClient = object : WebChromeClient() {
                override fun onProgressChanged(view: WebView?, progress: Int) {
                    if (progress == 100) {
                        didReceiveProgressCompleteEvent = true
                        notifyIfDone()
                    }
                }
            }

            // [dho] adapted from : https://stackoverflow.com/a/17815721 - 26/06/20
            webView.webViewClient = object: WebViewClient() {
                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    if(view != null && request != null)
                    {
                        running++;
                        view.loadUrl(request.url.toString());
                        return true;
                    }

                    return false
                }

                override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                    didReceivePageStartEvent = true
                    running = running.coerceAtLeast(1); // First request move it to 1.
                }

                override fun onPageFinished(view: WebView?, url: String?) {
                    if(--running == 0) { // just "running--;" if you add a timer.
                        didReceivePageFinishedEvent = true
                        notifyIfDone()
                    }
                }
            }

            webView.loadUrl(url)
        }

        timer = setTimeout({
            ThreadUtils.ensureMainThreadExec { webView.stopLoading() }

            var err : Exception? = null

            synchronized(lock)
            {
                if(!didSettle)
                {
                    didSettle = true
                    cleanup()
                    err = Exception("Could not load the requested URL within a reasonable timeframe")
                }
            }

            if(err != null)
            {
                completion(err)
            }

        }, LOAD_HTML_INFO_TIMEOUT_MS);
    }

    private fun onLaunchedFromSharingText(intent: Intent, completion: () -> Unit)
    {
        intent.getStringExtra(Intent.EXTRA_TEXT)?.let {

            /** [dho] FIX for sharing from apps like Medium that add some extraneous text to the shared data, eg:
             * “A Traumatized Nation” by The Good Men Project
             * https://link.medium.com/KiXQnhFzv9
             *
             * So we just tokenize the shared string on whitespace and take the first token that
             * looks like a valid URL - 04/09/20
             */
            val maybeURL = it.split("\\s".toRegex()).map { part -> part.trim() }.firstOrNull { token ->
                StringUtils.isValidURL(token)
            }

            if(maybeURL != null)
            {
                ThreadUtils.ensureBGThreadExec {
                    statuses.push(
                        navItem = NavItem(
                            key = StatusNav.KEY_processingSharedURL,
                            item = VRBStatus(
                                label = "inspecting shared article..."
                            )
                        ),
                        removeAllExisting = statuses::processingSharedURLMatcher
                    )

                    tryParseClientSideHTML(maybeURL)
                    {
                        html, err ->
                        // [dho] NOTE not that bothered if there is an error parsing the HTML - 26/06/20

                        val preHash = getWaitUntilLibraryChangedHash(viewModel.user_library.value)

                        api.addArticleToUserLibrary(url = maybeURL, html = html) { err ->
                            if (err != null) {
                                dialog.alert(title = "Article Failed", message = err.localizedMessage)
                            }


                            statuses.removeAll(statuses::processingSharedURLMatcher)

                            ThreadUtils.ensureBGThreadExec {
                                waitUntilLibraryChanged(preHash) { completion() }
                            }
                        }
                    }
                }
            }
            else
            {
                dialog.alert(title = "Article Failed", message = "Input is not a valid URL")
                completion()
            }
        }
    }


    private val WULC_FILTERS = arrayOf(BundleOps.FILTER_library)
    private val WULC_MAX_ATTEMPTS = 3
    private val WULC_ATTEMPT_INTERVAL_MS = 3100L
    private var waitUntilLibChangedPoll : TimeoutTask? = null
    /** [dho] a hash used to detect whether library has changed - 01/07/20 */
    private fun getWaitUntilLibraryChangedHash(userLibrary : DATA_UserLibrary?)
            = (userLibrary?.articles?.count() ?: -1).toString()

    private fun waitUntilLibraryChanged(preHash : String, attempt : Int = 0, completion: (err: Exception?) -> Unit)
    {
        waitUntilLibChangedPoll?.cancel?.invoke()
        waitUntilLibChangedPoll = null

        if(attempt >= WULC_MAX_ATTEMPTS)
        {
            statuses.removeAll(statuses::processingSharedURLMatcher)
            completion(Exception("Max attempts exceeded"))
        }

        statuses.push(
            navItem = NavItem(
                key = StatusNav.KEY_processingSharedURL,
                item = VRBStatus(
                    label = "awaiting result..."
                )
            ),
            removeAllExisting = statuses::processingSharedURLMatcher
        )

        api.fetchBundle(WULC_FILTERS) {
            bundle, err ->

            if(err != null)
            {
                statuses.removeAll(statuses::processingSharedURLMatcher)
                completion(err)
            }
            else
            {
                if(bundle != null)
                {
                    val postHash = getWaitUntilLibraryChangedHash(bundle.library)

                    if(preHash != postHash)
                    {
                        bundleOps.consume(bundle, WULC_FILTERS)
                        {
                            err ->
                            statuses.removeAll(statuses::processingSharedURLMatcher)
                            completion(err)
                        }
                        return@fetchBundle
                    }
                }


                waitUntilLibChangedPoll = setTimeout({
                    // [dho] check if the library is still in the same state - 01/07/20
                    if(getWaitUntilLibraryChangedHash(viewModel.user_library.value) == preHash)
                    {
                        waitUntilLibraryChanged(preHash, attempt + 1, completion)
                    }
                    else
                    {
                        statuses.removeAll(statuses::processingSharedURLMatcher)
                        completion(null)
                    }
                }, WULC_ATTEMPT_INTERVAL_MS)

            }
        }
    }


    private fun onUserChange(user : FirebaseUser?)
    {
        showScreen(SplashScreen.newInstance())

        ipo.addFlags(InProgressOperations.MASK_switchUser)

        if(user != null) // [dho] switched to a different user - 23/05/20
        {
//            player.clear()

            refreshBundle { bundle, err ->

                if(err != null && user.isAnonymous)
                {
                    // [dho] if we did not manage to refresh the bundle then clear
                    // out the old stale data - 23/05/20
                    bundleOps.clear {
                        dialog.toast(err.localizedMessage)
                        ipo.removeFlags(InProgressOperations.MASK_switchUser)
                        // [dho] will trigger replacing splash screen with dashboard - 23/05/20
                        screens.clear()
                    }
                }
                else
                {

                    ThreadUtils.bgThreadExec {
                        setTimeout({
                            val config = /*bundle?.config ?: */viewModel.user_config.value
                            val membership = /*bundle?.membership ?:*/ viewModel.user_membership.value

                            tryInitNonEssentialFeatures(config, membership) { err -> }
                        }, 2000);
                    }


//                    tryInitNonEssentialFeatures(config, membership)
//                    {
//                            err -> // [dho] NOTE do not really care if it fails - 22/06/20

                        ipo.removeFlags(InProgressOperations.MASK_switchUser)
                        // [dho] will trigger replacing splash screen with dashboard - 23/05/20
                        screens.clear()
//                    }

                }
            }
        }
        else // [dho] the user signed out - 23/05/20
        {
            firebaseWrapper.ensureUser { user, err ->
                if(err != null)
                {
                    dialog.alert(message = err.localizedMessage)
                    {
                        // [dho] retry when user closes dialog - 23/05/20
                        onUserChange(null)
                    }
                }
                else
                {
                    // [dho] will trigger the `onUserChange()` delegate again - 23/05/20
                    viewModel.user.postValue(user)
                }
            }
        }
    }

    private fun tryInitNonEssentialFeatures(userConfig : DATA_UserConfig?, userMembership: DATA_UserMembership?, completion: (err : Exception?) -> Unit)
    {
        val willInitAds = shouldShowAds(userMembership)
        val willFetchSKUs = shouldFetchSKUs(userConfig, userMembership)
        var count = (if (willInitAds) 1 else 0) + (if (willFetchSKUs) 1 else 0)

        if(count > 0)
        {
            val lock = Object()
            var resultErr : Exception? = null
            val handler : (err : Exception?) -> Unit = {
                synchronized(lock)
                {
                    if(it != null)
                    {
                        resultErr = it
                    }

                    if(--count == 0)
                    {
                        completion(resultErr)
                    }
                }
            }

            if(willInitAds)
            {
                ads.populateCache(handler)
            }

            if(userConfig?.products != null && willFetchSKUs)
            {
                updateSKUs(userConfig.products, handler)
            }
        }
        else
        {
            completion(null)
        }
    }

    private fun updateSKUs(products : Map<String, DATA_Product>, completion: (err : Exception?) -> Unit)
    {
        shop.fetchProductInfo(products)
        {
            skus, err ->
                ThreadUtils.ensureMainThreadExec {
                    if(skus != null)
                    {
                        viewModel.product_skus.value = skus
                    }

                    ThreadUtils.bgThreadExec { completion(err) }
                }
        }

    }

    private fun shouldFetchSKUs(userConfig: DATA_UserConfig?, membership: DATA_UserMembership?) = userConfig != null && membership != null && !membershipOps.isPaid(membership)

    private fun shouldShowAds(membership : DATA_UserMembership?) = (
        BuildConfig.DEBUG || !membershipOps.isPaid(membership)
    )

    private fun shouldShowUpsells(membership: DATA_UserMembership?, lastAccessed : DATA_UserLastAccessed?, skus : Map<String /*product id*/, SkuDetails>?) = (
        BuildConfig.DEBUG || (
            // [dho] is not null or anonymous... we need them to have signed up before we can upgrade them - 28/06/20
            !membershipOps.isNullOrAnon(membership) &&
            // [dho] is not already a paid member - 28/06/20
            !membershipOps.isPaid(membership) &&
            // [dho] we have loaded the products - 28/06/20
            skus?.isNotEmpty() == true /*&&
            // [dho] has listened to an article - 28/06/20
            lastAccessed?.performances?.isNotEmpty() == true*/
        )
    )


    private fun onConfigurationChange(userConfig: DATA_UserConfig?)
    {
        showInferredCurrentScreen(
            ipo = ipo,
            userConfig = userConfig,
            userMembership = viewModel.user_membership.value,
            nav_routeStack = viewModel.nav_screens.value
        )
    }

    private fun onPushNotificationTokenChange(token : String)
    {
        ThreadUtils.ensureBGThreadExec {
            var vendorID = ""

            // [dho] adapted from : https://stackoverflow.com/a/36639989/ - 08/06/20
            try {
                vendorID = AdvertisingIdClient.getAdvertisingIdInfo(this)?.id ?: ""
                // Use the advertising id
            } catch (exception: IOException)
            {
                Log.d("vendor_id", "ERROR: $exception.localizedMessage")
            }
            catch (exception: GooglePlayServicesRepairableException)
            {
                Log.d("vendor_id", "ERROR: $exception.localizedMessage")
            }
            catch (exception: GooglePlayServicesNotAvailableException)
            {
                Log.d("vendor_id", "ERROR: $exception.localizedMessage")
            }

            var systemName = ""

            // [dho] adapted from : https://stackoverflow.com/a/55946200/ - 08/06/20
            try {
                val fields = Build.VERSION_CODES::class.java.fields

                for(field in fields)
                {
                    if(field.getInt(Build.VERSION_CODES::class) == Build.VERSION.SDK_INT)
                    {
                        systemName = field.name;
                        break;
                    }
                }
            }
            catch(e : Exception)
            {
                Log.d("system_name", "ERROR: $e.localizedMessage")
            }

            val deviceToken = NOTIFICATION_DeviceToken(
                vendorID = vendorID,
                systemName = systemName,
                systemVersion = Build.VERSION.RELEASE,
                model = Build.MODEL,
                platform = platform,
                token = token
            );

            api?.addDevice(deviceToken)
            {
                _, err ->
                    Log.d("device_token", "Did update device token OK? ${err == null}")
            }
        }
    }

    private fun onPushNotificationReceived(message : RemoteMessage) {
        val notificationType = message.data?.get(Notifications.KEY_notificationType);
        val jsonString = message.data?.get(Notifications.KEY_jsonString);

        if(notificationType !== null && jsonString !== null)
        {
            onLaunchedFromPushNotification(
                notificationType = notificationType,
                jsonString = jsonString
            )
            {
                // callback
            }
        }
    }

    private fun onPushNotificationServiceRecommendsBundleRefresh() {
        ThreadUtils.ensureBGThreadExec {
            refreshBundle { _, _ ->  }
        }
    }

    override fun onBackPressed() {
//        super.onBackPressed()
        screens?.back()
    }

    private var currentScreenFragment : Fragment? = null

    private fun showInferredCurrentScreen(ipo : InProgressOperations, userConfig: DATA_UserConfig?, userMembership: DATA_UserMembership?, nav_routeStack : Stack<NavItem<VRBScreen>>?)
    {
        val showSplashScreen = (
            ipo.hasFlags(InProgressOperations.FLAG_initialize) ||
            ipo.hasFlags(InProgressOperations.FLAG_authenticate)
        );

        if(showSplashScreen)
        {
            if(currentScreenFragment !is SplashScreen)
            {
                showScreen(SplashScreen.newInstance())
            }

            return
        }

        if(userConfig == null)
        {
            if(currentScreenFragment !is InitializeGatewayScreen)
            {
                showScreen(InitializeGatewayScreen.newInstance())
            }

            return
        }

        if(userConfig.invite_Only_Enabled && membershipOps.isNullOrAnon(userMembership))
        {
            if(currentScreenFragment !is InviteGatewayScreen)
            {
                showScreen(InviteGatewayScreen.newInstance())
            }

            return
        }

        if(nav_routeStack?.isNotEmpty() == true)
        {
            val screen = nav_routeStack.peek()

            if(currentScreenFragment != screen.item)
            {
                showScreen(screen.item)
            }

            return
        }


        if(currentScreenFragment !is LibraryScreen)
        {
            showScreen(LibraryScreen.newInstance())
        }
    }

    private fun onScreenChange(nav_routeStack : Stack<NavItem<VRBScreen>>?)
    {
        showInferredCurrentScreen(
            ipo = ipo,
            userConfig = viewModel.user_config.value,
            userMembership = viewModel.user_membership.value,
            nav_routeStack = nav_routeStack
        );
    }

    @Synchronized private fun showScreen(fragment : Fragment, completion : (() -> Unit)? = null)
    {
        ThreadUtils.ensureMainThreadExec {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commitNow()

            currentScreenFragment = fragment;

            completion?.invoke()
        }
    }

    private fun onOverlayChange(nav_routeStack : Stack<NavItem<VRBOverlay>>?, completion : (() -> Unit)? = null) {
        ThreadUtils.ensureMainThreadExec {
            if (nav_routeStack != null && !nav_routeStack!!.isEmpty()) {
                val navItem = nav_routeStack.peek()

                Log.d("onOverlayChange", nav_routeStack.map { it.key }.joinToString())


                val lo = lastOverlay;
                try {
                    lo?.fragmentManager?.beginTransaction()?.remove(lo)?.commit();
                } catch (err: Exception) {

                }

                navItem.item.show(supportFragmentManager, navItem.key)

                lastOverlay = navItem.item

                completion?.invoke()
            }
        }
    }


    private var lastOverlay : VRBOverlay? = null


//    private var initMediaPlayerCallback : ((err : Exception?) -> Unit)? = null

    private fun initMediaPlayer(completion : (err : Exception?) -> Unit)
    {
//        initMediaPlayerCallback = completion
//        player = Player(/*mediaPlayerService!!*/this, viewModel, localCache, api, network)

//        player!!.init(completion)
        completion(null)


//        initMediaPlayerCallback?.let { it(null) };
//        initMediaPlayerCallback = null;
//
//        initMediaPlayerCallback?.let {
//            it(Exception("Could not start the media player service"))
//        };
//        initMediaPlayerCallback = null;

//        // Attempts to establish a connection with the service.  We use an
//        // explicit class name because we want a specific service
//        // implementation that we know will be running in our own process
//        // (and thus won't be supporting component replacement by other
//        // applications).
//        if (bindService(
//                Intent(this@MainActivity, LocalMediaPlayerService::class.java),
//                mConnection, Context.BIND_AUTO_CREATE
//            )
//        ) {
//            mShouldUnbind = true
//        } else
//        {
//            initMediaPlayerCallback?.let {
//                it(Exception("Could not start the media player service"))
//            };
//            initMediaPlayerCallback = null;
//        }
    }
    // Don't attempt to unbind from the service unless the client has received some
    // information about the service's state.
//    private var mShouldUnbind = false

    // To invoke the bound service, first make sure that this value
// is not null.
//    private var mBoundService: LocalMediaPlayerService? = null

//    private val mConnection: ServiceConnection = object : ServiceConnection {
//        override fun onServiceConnected(
//            className: ComponentName,
//            service: IBinder
//        ) {
//
//
//
//
//            // This is called when the connection with the service has been
//            // established, giving us the service object we can use to
//            // interact with the service.  Because we have bound to a explicit
//            // service that we know is running in our own process, we can
//            // cast its IBinder to a concrete class and directly access it.
//            mediaPlayerService = (service as LocalMediaPlayerService.MyBinder).service
//
//            player = Player(mediaPlayerService!!, viewModel, localCache, api, network)
//
//            initMediaPlayerCallback?.let { it(null) };
//            initMediaPlayerCallback = null;
//        }
//
//        override fun onServiceDisconnected(className: ComponentName) { // This is called when the connection with the service has been
//            // unexpectedly disconnected -- that is, its process crashed.
//            // Because it is running in our same process, we should never
//            // see this happen.
//            mediaPlayerService = null
//            player = null
////            Toast.makeText(
////                this@Binding, R.string.local_service_disconnected,
////                Toast.LENGTH_SHORT
////            ).show()
//        }
//    }



//    private fun doUnbindService()
//    {
//        if (mShouldUnbind)
//        {
//            // Release information about the service's state.
//            unbindService(mConnection)
//            mShouldUnbind = false
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()
//        doUnbindService()
        shop?.close()
        ads?.destroy()
        network?.destroy()
    }

}
