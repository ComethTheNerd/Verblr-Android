package com.quantumcommune.verblr

import androidx.lifecycle.MutableLiveData
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

// [dho] adapted from : https://stackoverflow.com/a/59565726/300037 - 08/06/20
object RemoteNotificationServiceDataBridge {
    val token by lazy {
        MutableLiveData<String>()
    }

    val recommendBundleRefresh by lazy {
        MutableLiveData<Boolean>()
    }

    val remoteMessage by lazy {
        MutableLiveData<RemoteMessage>()
    }
}

class PushNotificationService : FirebaseMessagingService() {
    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        RemoteNotificationServiceDataBridge.token.postValue(token)
    }

    /** [dho] NOTE only called 100% if app is in foreground. If in background then this is only called if we
     * omit the `notification` key in the payload sent to FCM on the server - in that case it is a `data`
     * only payload and this service will call the `onMessageReceived` handler.
     *
     * If there is both `notification` and `data` key set in the payload and app is in background or not alive at all,
     * then the `notification` is automatically shown in the system tray, and upon the user interacting with it, the
     * `data` is added as extras on the launch Intent sent to the default activity - 08/06/20
     * */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage);

        /** [dho] TODO add support for background `data` only notification
         * seems like determining if app is in foreground or not is hacky
         * and so we won't bother yet, and just assume a notification will always
         * be the hybrid `notification`/`data` kind and the user will get a tray
         * notification on our behalf that they can tap to launch the app from - 08/06/20
         * */
        RemoteNotificationServiceDataBridge.remoteMessage.postValue(remoteMessage)
    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()

        /*
        * When the app instance receives this callback, it should perform a full sync with your app server
        * */
        RemoteNotificationServiceDataBridge.recommendBundleRefresh.postValue(true)
    }
}