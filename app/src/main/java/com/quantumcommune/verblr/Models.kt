package com.quantumcommune.verblr

data class DATA_Error(
    val message : String,
    val stack : String?,
    val code : String?
)
{

}

data class REQ_FetchBundle(
    val platform : String,
    val filters : Array<String>?,
    val deviceToken : NOTIFICATION_DeviceToken?
) {

}

data class DATA_Bundle(
    val profile : DATA_UserProfile? = null,
    val jobs : DATA_UserJobs? = null,
    val membership : DATA_UserMembership? = null,
    val library : DATA_UserLibrary? = null,
    val config : DATA_UserConfig? = null,
    val lastAccessed : DATA_UserLastAccessed? = null,
    val urls : DATA_UserURLs? = null
) 

data class DATA_UserProfile(
    val id : String
//    val email : String?
) 

data class DATA_UserJobs(
    val lastUpdateUTC : String?,
    val jobs : Array<DATA_Job>
) 

data class DATA_UserMembership(
    val userID :  String,
    val tier : String, //"standard" | "premium";
    val flags : Int,
    val starts : String,
    val expires : String?,
    val refreshes : String?,
    val entitlement : DATA_UserEntitlement,
    val credits : Array<DATA_Denomination>,
    val wallets : Array<DATA_Wallet>,
    val pending : DATA_UserMembershipPending?,
    val productCadence : String?
) 

data class DATA_UserLibrary(
    val articles : Map<String /* article ID */, DATA_Article>
) 

data class DATA_UserLastAccessed(
    val articles : Map<String /* article ID */, Analytics_UserLastAccessedArticle>,
    val performances : Map<String /* performance hash */, Analytics_UserLastAccessedPerformance>
) { }

data class Analytics_UserLastAccessedArticle(
    val userID : String,
    val articleID : String,
    val accessUTC : String
)

data class Analytics_UserLastAccessedPerformance(
    val userID : String,
    val performanceHash : String,
    val articleID : String,
    val accessUTC : String,
    val progress : Float,
    val duration : Float
)

data class DATA_ProvisionedURL(
    val userID : String,
    val url : String,
    val purpose : String,
    val refID : String,
    val expiresUTC : String,
    val meta : DATA_CommonMeta
) { }

/*data*/ class DATA_EmailAuthChallengeCreated {

}

data class DATA_EmailAuthChallengeCompleted(
    val userID : String,
    val email : String,
    val token : String,
    val bundle : DATA_Bundle,
    val reason : String,
    val migratedUserID : String?
)

data class DATA_Wallet(
    val userID : String,
    val source : String,
    val tier : String,
    val amount : Int,
    val currency : String,
    val lastUpdateUTC : String,
    val meta : DATA_CommonMeta
) 

data class DATA_Denomination(
    val currency: String,
    val amount : Int
) 

data class DATA_UserEntitlement(
    val cadence : String,
    val credits : Array<DATA_Denomination>
)

data class DATA_Job(
    val type : String
)

data class NOTIFICATION_DeviceToken(
    val vendorID : String,
    val systemName : String,
    val systemVersion : String,
    val model : String,
    val platform : String,
    val token : String
)

data class DATA_UserURLs(
    val active : Array<DATA_ProvisionedURL>
)

// [dho] NOTE keep this flattened so new config versions are always
// backwards compatible in terms of deserialization - 16/11/19
data class DATA_UserConfig
(
    val invite_Only_Enabled : Boolean,
    val invite_Email : String,
    val invite_EmailSubject : String,
    val invite_EmailBody : String,

    val addArticle_SyncRequest_MaxBytes : Int,

//    val appStore_ManageSubscriptionsURL : String,
//    val appStore_RequestReviewURL : String,
//
//    val appStore_ProductID_PremiumSubscription_Monthly : String,
//    val appStore_ProductID_PremiumSubscription_Yearly : String,
//    val appStore_ProductID_UltraSubscription_Monthly : String,
//    val appStore_ProductID_UltraSubscription_Yearly : String,
//    val appStore_ProductID_Topup10_Lifetime : String,

    val products : Map<String /*product id*/, DATA_Product>,

    val membership_Entitlement_FreshStreamLimit_Anon : Int?,

    val membership_Entitlement_FreshStreamLimit_Standard : Int?,

    val membership_Entitlement_Cadence_Premium : String?,
    val membership_Entitlement_FreshStreamLimit_Premium : Int?,
    val membership_Entitlement_FreshWordLimit_Premium : Int?,

    val membership_Entitlement_Cadence_Ultra : String?,
    val membership_Entitlement_FreshStreamLimit_Ultra : Int?,
    val membership_Entitlement_FreshWordLimit_Ultra : Int?,


    val topup_Cadence_10 : String?,
    val topup_FreshStreamLimit_10 : Int?,
    val topup_FreshWordLimit_10 : Int?,

    val performance_ProgressUpdate_SendToServer : Boolean,
    val performance_ProgressUpdateDelta_Seconds : Float,
    val performance_FileExtension : String,

    val jobs_PollInterval_Seconds : Float,

    val accessPerformance_Bill_WordCount_Rounding : Int,
    val accessPerformance_PollInterval_Seconds : Float,
    val accessPerformance_MaxAttempts : Int,

    val privacyPolicyURL : String?,
    val termsOfUseURL : String?,
    val supportURL : String?,
    val manageSubscriptionsURL : String?,
    val requestReviewURL : String?,

    val contact_Email : String?
)

data class DATA_Product(
    val kind : String,
    val productID : String,
    val productGroupID : String,
    val paymentCadence : String,
    val available : Boolean
)

data class REQ_AccessPerformance(
    val articleID : String,
    val deviceToken : NOTIFICATION_DeviceToken?
)

data class RES_AccessPerformance(
    val job : DATA_Job?,
    val performance : DATA_Admission?
)

data class RES_UpdatePerformanceProgress(
    val performanceHash : String,
    val progress : Float
)


data class REQ_AddDevice(
    val deviceToken : NOTIFICATION_DeviceToken
)

/*data*/ class RES_AddDevice
{
}

data class REQ_AddArticle(
    val url : String,
    val html : String?,
    val platform : String,
    val deviceToken : NOTIFICATION_DeviceToken?
)

data class RES_AddArticle(
    val job : DATA_Job?
)

data class REQ_InviteByEmail(
    val recipientEmail : String,
    val deviceToken : NOTIFICATION_DeviceToken?
)

/*data*/ class RES_InviteByEmail
{
}

data class REQ_ActivateProduct(
    val platform : String,
    val receipt : String
);

data class RES_ActivateProduct(
    val membership : DATA_UserMembership
)

data class REQ_CreateEmailAuthChallenge
(
    val email : String
)

data class REQ_CompleteEmailAuthChallenge
(
    val email : String,
    val code : String,
    val platform : String
)

data class DATA_UserMembershipPending(
    val tier : String,
    val reason : String,
    val productCadence : String,
    val tierStartUTC : String?,
    val tierEndUTC : String?
)

data class DATA_Admission(
    val articleID : String,
    val performanceHash: String,
    val lastAccessed : Analytics_UserLastAccessedPerformance?,
    val signedURL: String,
    val fileExtension : String,
    val contentType : String,
    val expiresUTC: String
)

data class DATA_CommonMeta(
    val creationUTC : String,
    val creatorID : String
)

data class DATA_Article(
    val id : String,
    val hash : String,
    val status : String,
    val content : DATA_ContentInfo,
    val details : DATA_DetailInfo,
    val socials : DATA_SocialInfo,
    val source : DATA_SourceInfo,
    val artwork : DATA_ArtworkInfo,
    val meta : DATA_CommonMeta,
    val inferredContentRoot : Boolean,
    val clientIncludedSource : Boolean,
    val addedToLibraryUTC : String?
){}

data class DATA_SocialInfo(
    val twitter: String?
);

data class DATA_SourceInfo(
    val type : String?,
    val contentURL : String,
    val canonicalURL : String?,
    val basisURL : String,
    val amp : Boolean
)

data class DATA_ContentInfo(
    val effectiveWordCount : Int
)

data class DATA_DetailInfo
(
    val published : String?,
    val modified : String?,
    val expires : String?,
    val author: DATA_Author?,
    val organization : DATA_Organization?,
    val title: String?, // article title
    val artwork : String?,
    val description : String?,
    val categories : Map<String, DATA_ArticleCategory>,
    val tags : Map<String, DATA_ArticleTag>,
    val language : String?
)

data class DATA_ArticleCategory(
    val name : String
)

data class DATA_ArticleTag(
    val name : String
)

data class DATA_ArtworkInfo(
    val standardArtwork : String?,
    val largeArtwork : String?
)

data class DATA_Author(
    val name : String,
    val socials : DATA_Socials
)

data class DATA_Organization(
    val name : String,
    val socials : DATA_Socials
)

data class DATA_Socials(
    val website : String?,
    val twitter : String?
)

data class DATA_AddArticleParamsUploadURL(
    val url : String,
    val filename : String,
    val expiresUTC : String,
    val fields : Map<String, String>
)

/*data*/ class DATA_ProcessAddArticleParamsQueueResult
{

}

/*data*/ class DATA_SysCheckResult {

}

data class DATA_UserAddArticleToUserLibraryNotificationPayload(
    val type: String,
    val url : String?,
    val status : String,
    val article : DATA_Article?,
    val errorDescription : String?
)

data class PARSE_ClientSidePageInfo(
    val url : String,
    val html : String,
    val isAMP : Boolean,
    val ampURL : String?
)