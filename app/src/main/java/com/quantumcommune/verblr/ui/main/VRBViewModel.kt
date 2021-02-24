package com.quantumcommune.verblr.ui.main

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.android.billingclient.api.SkuDetails
import com.google.firebase.auth.FirebaseUser
import com.quantumcommune.verblr.*
import java.util.*

data class Status(
    val iconName : String? = null,
    val message : String,
    val action : (() -> Unit)? = null
)

class VRBViewModel : ViewModel() {

//    val init_errors by lazy {
//        MutableLiveData<ArrayList<Exception>>()
//    }

    val appEnteredBG by lazy {
        MutableLiveData<Boolean>()
    }

    val show_ads = MutableLiveData<Boolean>(false)
    val show_upsells = MutableLiveData<Boolean>(false)

    val user by lazy {
        MutableLiveData<FirebaseUser>()
    }

    val ipo = MutableLiveData<Int>(0)

    val deviceToken by lazy {
        MutableLiveData<NOTIFICATION_DeviceToken>()
    }

    val status by lazy {
        MutableLiveData<Status>()
    } 

    val nav_screens by lazy {
        MutableLiveData<Stack<NavItem<VRBScreen>>>()
    }

    val nav_overlays by lazy {
        MutableLiveData<Stack<NavItem<VRBOverlay>>>()
    }

    val nav_statuses by lazy {
        MutableLiveData<Stack<NavItem<VRBStatus>>>()
    }

    val creditCheck_article by lazy {
        MutableLiveData<DATA_Article>()
    }

    val player_article by lazy {
        MutableLiveData<DATA_Article>()
    }

    val player_duration by lazy {
        MutableLiveData<Float>()
    }

    val player_progress by lazy {
        MutableLiveData<Float>()
    }

    val player_progress_scalar by lazy {
        MutableLiveData<Float>()
    }

    val player_state by lazy {
        MutableLiveData<Int>()
    }

    val player_buff_state by lazy {
        MutableLiveData<Int>()
    }

    val player_buff_progress by lazy {
        MutableLiveData<Float>()
    }


    val user_profile by lazy {
        MutableLiveData<DATA_UserProfile>()
    }

    val user_jobs by lazy {
        MutableLiveData<DATA_UserJobs>()
    }

    val user_membership by lazy {
        MutableLiveData<DATA_UserMembership>()
    }

    val user_library by lazy {
        MutableLiveData<DATA_UserLibrary>()
    }

    val user_config by lazy {
        MutableLiveData<DATA_UserConfig>()
    }

    val user_lastAccessed by lazy {
        MutableLiveData<DATA_UserLastAccessed>()
    }

    val user_urls by lazy {
        MutableLiveData<DATA_UserURLs>()
    }

    val product_skus by lazy {
        MutableLiveData<Map<String /*product id*/, SkuDetails>>()
    }
}


