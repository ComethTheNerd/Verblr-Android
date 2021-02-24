package com.quantumcommune.verblr.ui.main

import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.quantumcommune.verblr.*

abstract class VRBOverlay() : BottomSheetDialogFragment() {

    protected lateinit var api : API
    protected lateinit var viewModel: VRBViewModel
    protected lateinit var articleOps : ArticleOps
    protected lateinit var bundleOps : BundleOps
    protected lateinit var membershipOps: MembershipOps;
    protected lateinit var localCache : LocalCache
    protected lateinit var network : Network
    protected lateinit var ipo : InProgressOperations
    protected lateinit var dialog : com.quantumcommune.verblr.ui.main.Dialog
    protected lateinit var screens : ScreenNav
    protected lateinit var overlays : OverlayNav
    protected lateinit var player : Player
    protected lateinit var auth : FirebaseWrapper
    protected lateinit var ads : AdVendor
    protected lateinit var shop : Shop

//    private lateinit var sheetBehavior : BottomSheetBehavior<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // [dho] sharing same model instance with activity. Adapted from : https://stackoverflow.com/a/57609917/300037 - 17/05/20
        activity?.let {
            val mainActivity = it as MainActivity

            viewModel = ViewModelProviders.of(it).get(VRBViewModel::class.java)
            ipo = InProgressOperations(viewModel)
            screens = mainActivity.screens//ScreensNav(viewModel)
            overlays = mainActivity.overlays//OverlaysNav(viewModel)
            auth = FirebaseWrapper(activity as Activity)
            player = mainActivity.player
            ads = mainActivity.ads
            shop = mainActivity.shop

            if(context != null)
            {
                localCache = LocalCache(activity!!, viewModel)
                api = API(context!!, viewModel)
                bundleOps = BundleOps(context!!, viewModel, localCache)
                dialog = com.quantumcommune.verblr.ui.main.Dialog(context!!)
                network = Network(context!!)
                membershipOps = MembershipOps()
                articleOps = ArticleOps(localCache, membershipOps)
            }

        }

        setStyle(STYLE_NORMAL, R.style.SheetDialog);
    }

    private fun removeOverlayFromNav()
    {
        overlays?.remove(this)
    }

    override fun onCancel(dialog: DialogInterface?) {
        super.onCancel(dialog)

        removeOverlayFromNav()
    }

    override fun onDismiss(dialog: DialogInterface?) {
        super.onDismiss(dialog)

//        removeOverlayFromNav()
    }

//    protected var fullWidth = 0
//    protected var fullHeight = 0

    private lateinit var linearLayout: LinearLayout

    abstract fun onCreateContentView() : View?

    fun requestContentChange()
    {
        context?.let {
            linearLayout.removeAllViews()

            onCreateContentView()?.let {  linearLayout.addView(it) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        linearLayout = LinearLayout(context)

        return linearLayout
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState) as BottomSheetDialog

        dialog.setOnShowListener { _ ->
//            if (Build.VERSION.SDK_INT < 16)
//            {
//                // Hide the status bar
//                dialog.window?.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
//                // Hide the action bar
//                dialog?.supportActionBar?.hide();
//            }
//            else
//            {
//                // Hide the status bar
//                dialog?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN;
//                // Hide the action bar
//                dialog?.actionBar?.hide();
//
//
//            }




            val bottomSheet = dialog.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet) as FrameLayout?

            val bsb = BottomSheetBehavior.from(bottomSheet)

            val parent = bottomSheet?.parent as? CoordinatorLayout
            if(parent != null)
            {
                linearLayout.layoutParams = FrameLayout.LayoutParams(
                    parent.width,
                    parent.height
                )
            }

            requestContentChange();

            // [dho] adapted from : https://stackoverflow.com/a/39062055/300037 - 18/06/20
            bsb.peekHeight = 0
            bsb.isFitToContents = true
            bsb.skipCollapsed = true
            bsb.state = BottomSheetBehavior.STATE_EXPANDED;

            bsb.setBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
                override fun onSlide(p0: View, p1: Float) {

//                    TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                }

                override fun onStateChanged(p0: View, state: Int) {
                    onStateChanged(state)
                }
            })
        }

        return dialog
    }

    private var lastState : Int? = null
    private var lastStateInMotion : Boolean = false

    open fun onStateChanged(state : Int)
    {
        val label = when(state)
        {
            BottomSheetBehavior.STATE_COLLAPSED -> "collapsed"
            BottomSheetBehavior.STATE_DRAGGING -> "dragging"
            BottomSheetBehavior.STATE_EXPANDED -> "expanded"
            BottomSheetBehavior.STATE_HALF_EXPANDED -> "half expanded"
            BottomSheetBehavior.STATE_HIDDEN -> "hidden"
            BottomSheetBehavior.STATE_SETTLING -> "settling"
            else -> "dunno"
        }

        android.util.Log.d("STATE CHANGED XXXXX", label);

        if(state == BottomSheetBehavior.STATE_HIDDEN && lastStateInMotion)
        {
            removeOverlayFromNav();
        }

        lastState = state

        lastStateInMotion = (
            state == BottomSheetBehavior.STATE_DRAGGING ||
            state == BottomSheetBehavior.STATE_SETTLING
        )
    }

    fun defaultCompletionHandler(err : Exception?)
    {
        if(err != null){
            dialog.alert(message = err.localizedMessage ?: "Something went wrong")
        }
    }
}