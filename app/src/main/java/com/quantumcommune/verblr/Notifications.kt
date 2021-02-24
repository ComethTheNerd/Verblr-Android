package com.quantumcommune.verblr

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.iid.FirebaseInstanceId

class Notifications(private var context : Context) {
    val TYPE_userAddArticleToUserLibrary = context.getString(R.string.user_add_article_to_user_library_notification_type);
    val TYPE_userAccessPerformance = context.getString(R.string.user_access_performance_notification_type);
    val TYPE_subscriptionLapsed = context.getString(R.string.subscription_lapsed_notification_type);
    val TYPE_inviteAccepted = context.getString(R.string.invite_accepted_notification_type);
    val TYPE_unspentCreditReminder = context.getString(R.string.unspent_credit_reminder_notification_type);
    val TYPE_invitationRewardReminder = context.getString(R.string.invitation_reward_reminder_notification_type);

    companion object {
        val KEY_notificationType = "vrb_type";
        val KEY_jsonString = "vrb_json"
    }

    fun getToken(completion : (token : String?, err : Exception?) -> Unit)
    {
        FirebaseInstanceId.getInstance().instanceId
            .addOnCompleteListener(OnCompleteListener { task ->
                if(task.isSuccessful)
                {
                    // Get new Instance ID token
                    val token = task.result?.token

                    completion(token, null)
                }
                else
                {
                    completion(null, Exception(task.exception?.localizedMessage ?: "Something went wrong"))
                }
            })
    }


    val CHANNEL_id_general = context.getString(R.string.general_notification_channel_id)
    val CHANNEL_name_general = "general"
    val CHANNEL_description_general = "general Verblr notifications"

    val generalNotificationBuilder = getBuilder(CHANNEL_id_general,
        CHANNEL_name_general, CHANNEL_description_general)


    val notificationManager : NotificationManager
        get() = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun getBuilder(channelID : String, channelName : String, channelDescription : String) : Notification.Builder {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create the NotificationChannel
                val importance = NotificationManager.IMPORTANCE_DEFAULT
                val mChannel = NotificationChannel(channelID, channelName, importance)
                mChannel.description = channelDescription
                // Register the channel with the system; you can't change the importance
                // or other notification behaviors after this
                notificationManager.createNotificationChannel(mChannel)

                return Notification.Builder(context, mChannel.id);
            }
            else
            {
                return Notification.Builder(context)
            }
        }



}