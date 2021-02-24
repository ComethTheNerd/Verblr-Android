package com.quantumcommune.verblr

import android.content.Context
import android.util.Log
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.quantumcommune.verblr.ui.main.VRBViewModel


class API(private val context : Context, private val viewModel: VRBViewModel) {

    val network = Network(context)
    val ipo = InProgressOperations(viewModel)
    val PLATFORM_NAME = context.getString(R.string.platform)

    val baseURL = "https://api.verblr.com";
    val activateProductRoute = "${baseURL}/activateProduct"
    val bundleRoute = "${baseURL}/getBundle";
    val addDeviceRoute = "${baseURL}/addDevice";
//    val addArticleToLibraryRoute = "${baseURL}/addArticleToUserLibrary";
    val getAddArticleToUserLibraryUploadURLRoute = "${baseURL}/getAddArticleToUserLibraryUploadURL"
    val processAddArticleParamsQueueRoute = "${baseURL}/processAddArticleParamsQueue";
    val accessPerformanceRoute = "${baseURL}/accessPerformance";
//    val updatePerformanceProgressRoute = "${baseURL}/updatePerformanceProgress";
    val createEmailAuthChallengeRoute = "${baseURL}/createEmailAuthChallenge";
    val completeEmailAuthChallengeRoute = "${baseURL}/completeEmailAuthChallenge";
    val sysCheckRoute = "${baseURL}/sysCheck"
    val inviteByEmailRoute = "${baseURL}/inviteByEmail"


    var accessPerformancePoll : TimeoutTask? = null

    fun fetchBundle(filters : Array<String> = arrayOf(), completion : (data : DATA_Bundle?, err : Exception?) -> Unit) {
        if(ipo.hasFlags(InProgressOperations.FLAG_fetchBundle))
        {
            ThreadUtils.ensureMainThreadExec {
                completion(null, Exception("Fetching already in progress"))
            }
        }
        else
        {
            ipo.addFlags(InProgressOperations.FLAG_fetchBundle)

            getUserToken { token, err ->
                if(err != null) {

                    ipo.removeFlags(InProgressOperations.FLAG_fetchBundle)

                    ThreadUtils.ensureMainThreadExec {
                        completion(null, err)
                    }
                }
                else {
                    val body = REQ_FetchBundle(
                        platform = PLATFORM_NAME,
                        filters = if(filters.count() > 0) filters else null,
                        deviceToken = viewModel.deviceToken.value
                    );

                    network.httpPostJSON(bundleRoute, body,  mapOf("Authorization" to "Bearer $token"), DATA_Bundle::class.java)
                    { bundle, err ->

                        ipo.removeFlags(InProgressOperations.FLAG_fetchBundle)

                        completion(bundle, err)
                    };
                }
            }
        }
    }


    fun processAddArticleParamsQueue(completion : (err : Exception?) -> Unit)
    {
        if(ipo.hasFlags(InProgressOperations.FLAG_processAddArticleParamsQueue))
        {
            ThreadUtils.ensureMainThreadExec {
                completion(Exception("Processing add article params queue already in progress"))
            }
        }
        else
        {
            ipo.addFlags(InProgressOperations.FLAG_processAddArticleParamsQueue)

            getUserToken { token, err ->
                if(err != null) {

                    ipo.removeFlags(InProgressOperations.FLAG_processAddArticleParamsQueue)

                    ThreadUtils.ensureMainThreadExec {
                        completion(err)
                    }
                }
                else {
                    network.httpGetJSON(processAddArticleParamsQueueRoute,  mapOf("Authorization" to "Bearer $token"), DATA_ProcessAddArticleParamsQueueResult::class.java)
                    { _, err ->
                        ipo.removeFlags(InProgressOperations.FLAG_processAddArticleParamsQueue)

                        completion(err)
                    };
                }
            }
        }
    }

    fun addArticleToUserLibrary(url : String, html : String?, completion: (err: Exception?) -> Unit)
    {
        if(ipo.hasFlags(InProgressOperations.FLAG_addArticle))
        {
            ThreadUtils.ensureMainThreadExec {
                completion(Exception("Add article already in progress"))
            }
        }
        else
        {
            ipo.addFlags(InProgressOperations.FLAG_addArticle)

            getUserToken { token, err ->
                if(err != null) {

                    ipo.removeFlags(InProgressOperations.FLAG_addArticle)

                    ThreadUtils.ensureMainThreadExec {
                            completion(err)
                    }
                }
                else {
                    network.httpGetJSON(getAddArticleToUserLibraryUploadURLRoute, mapOf("Authorization" to "Bearer $token"), DATA_AddArticleParamsUploadURL::class.java)
                    { data, err ->

                        if (err != null) {
                            ipo.removeFlags(InProgressOperations.FLAG_addArticle)
                            completion(err)
                        }
                        else if(data == null)
                        {
                            ipo.removeFlags(InProgressOperations.FLAG_addArticle)
                            completion(Exception("Could not retrieve add article upload URL"))
                        }
                        else {
                            val uploadURL = data.url;
                            val uploadFilename = data.filename;
                            val uploadFields = data.fields;

                            val payload = REQ_AddArticle(
                                url = url,
                                html = html,
                                platform = PLATFORM_NAME,
                                deviceToken = viewModel.deviceToken.value
                            )

                            network.s3Upload(uploadURL, uploadFilename, payload, uploadFields) { err ->

                                if(err == null)
                                {
                                    setTimeout({
                                        processAddArticleParamsQueue {
                                                err -> Log.d("procAddArticleParamsQ", err?.localizedMessage ?: "OK")
                                        }
                                    },0)
                                }

                                ipo.removeFlags(InProgressOperations.FLAG_addArticle)

                                completion(err)
                            }
                        }
                    }
                }
            }
        }
    }

    fun accessPerformance(articleID: String, completion : (data : DATA_Admission?, error : Exception?) -> Unit)
    {
        accessPerformancePoll?.cancel?.invoke()
        accessPerformancePoll = null;

        ipo.addFlags(InProgressOperations.FLAG_accessPerformance)

        getUserToken { token, err ->
            if(err != null) {

                ipo.removeFlags(InProgressOperations.FLAG_accessPerformance)

                ThreadUtils.ensureMainThreadExec {
                    completion(null, err)
                }
            }
            else {
                val body = REQ_AccessPerformance(articleID, viewModel.deviceToken.value);

                network.httpPostJSON(accessPerformanceRoute, body,  mapOf("Authorization" to "Bearer $token"), RES_AccessPerformance::class.java)
                { data, err ->
                    if(data != null)
                    {
                        if(data.performance != null)
                        {
                            val performance = data.performance
//                            val performanceHash = performance.performanceHash
//                            val laPerformance = performance.lastAccessed

//                            if(laPerformance != null)
//                            {
//                                updateCachedLastAccessedPerformance(performanceHash, laPerformance)
//                            }

                            accessPerformancePoll?.cancel?.invoke()
                            accessPerformancePoll = null;

                            ipo.removeFlags(InProgressOperations.FLAG_accessPerformance)

                            completion(performance, err);
                        }
                        else
                        {
                            accessPerformancePoll?.cancel?.invoke()

                            val delayMS = (
                                    (viewModel.user_config.value?.accessPerformance_PollInterval_Seconds ?: 1.4f) * 1000
                            ).toLong()

                            // [dho] the API did not give us a performance admission, and there
                            // was no error, so we poll - 18/05/20
                            accessPerformancePoll = setTimeout({
                                accessPerformance(articleID, completion)
                            }, delayMS)
                        }
                    }
                    else
                    {
                        accessPerformancePoll?.cancel?.invoke()
                        accessPerformancePoll = null;

                        ipo.removeFlags(InProgressOperations.FLAG_accessPerformance)

                        completion(null, err);
                    }
                };
            }
        }
    }

    fun addDevice(deviceToken : NOTIFICATION_DeviceToken, completion : (data : RES_AddDevice?, error : Exception?) -> Unit)
    {
        getUserToken { token, err ->
            if(err != null) {

                ipo.removeFlags(InProgressOperations.FLAG_addArticle)

                ThreadUtils.ensureMainThreadExec {
                    completion(null, err)
                }
            }
            else {
                val body = REQ_AddDevice(deviceToken)

                network.httpPostJSON(addDeviceRoute, body, mapOf("Authorization" to "Bearer $token"), RES_AddDevice::class.java)
                { data, err ->
                    completion(data, err)
                }
            }
        }
    }

    fun createEmailAuthChallenge(email : String, completion : (err : Exception?) -> Unit)
    {
        getUserToken {
            token, err ->

            if(err != null) {
                ThreadUtils.ensureMainThreadExec {
                        completion(err)
                }
            }
            else
            {
                val body = REQ_CreateEmailAuthChallenge(email)

                network.httpPostJSON(createEmailAuthChallengeRoute, body,  mapOf("Authorization" to "Bearer $token"), DATA_EmailAuthChallengeCreated::class.java)
                { _, err ->

                    completion(err)
                };
            }
        }
    }

    fun completeEmailAuthChallenge(email : String, code : String, completion : (data : DATA_EmailAuthChallengeCompleted?, err : Exception?) -> Unit)
    {
        if(ipo.hasFlags(InProgressOperations.MASK_switchUser))
        {
            ThreadUtils.ensureMainThreadExec {
                    completion(null, Exception("Authentication already in progress"))
            }
        }
        else
        {
            ipo.addFlags(InProgressOperations.MASK_switchUser)

            getUserToken { token, err ->

                if (err != null) {

                    ipo.removeFlags(InProgressOperations.MASK_switchUser)

                    ThreadUtils.ensureMainThreadExec {
                            completion(null, err)
                    }
                } else {
                    val body = REQ_CompleteEmailAuthChallenge(
                        email = email,
                        code = code,
                        platform = PLATFORM_NAME
                    )

                    network.httpPostJSON(
                        completeEmailAuthChallengeRoute,
                        body,
                        mapOf("Authorization" to "Bearer $token"),
                        DATA_EmailAuthChallengeCompleted::class.java
                    )
                    { data, err ->

                        //                    if(err != null)
//                    {
//
//                    }
//                    else
//                    {
                        ipo.removeFlags(InProgressOperations.MASK_switchUser)
                        completion(data, err)

//                        val signInToken = data.token
//                        val bundle = data.bundle
//
//                        signInWithCustomToken(signInToken)
//                        {
//                            _, err ->
//
//                            if(err == null)
//                            {
//                                consumeBundle(bundle);
//                            }
//
//
//                        }
//                    }

                    };
                }
            }
        }
    }

    fun inviteByEmail(recipientEmail : String, completion : (err : Exception?) -> Unit)
    {
        if(ipo.hasFlags(InProgressOperations.FLAG_inviteByEmail))
        {
            ThreadUtils.ensureMainThreadExec {
                    completion(Exception("Invite by email already in progress"))
            }
        }
        else
        {
            ipo.addFlags(InProgressOperations.FLAG_inviteByEmail)

            getUserToken { token, err ->

                if (err != null) {

                    ipo.removeFlags(InProgressOperations.FLAG_inviteByEmail)

                    ThreadUtils.ensureMainThreadExec {
                        completion(err)
                    }
                } else {
                    val body = REQ_InviteByEmail(recipientEmail, deviceToken = viewModel.deviceToken.value)

                    network.httpPostJSON(
                        inviteByEmailRoute,
                        body,
                        mapOf("Authorization" to "Bearer $token"),
                        RES_InviteByEmail::class.java
                    )
                    { _, err ->
                        ipo.removeFlags(InProgressOperations.FLAG_inviteByEmail)
                        completion(err)
                    };
                }
            }
        }
    }

    fun activateProduct(purchaseToken : String, completion : (data : RES_ActivateProduct?, error : Exception?) -> Unit)
    {
        getUserToken { token, err ->
            if(err != null) {
                ThreadUtils.ensureMainThreadExec {
                    completion(null, err)
                }
            }
            else {
                val body = REQ_ActivateProduct(platform = PLATFORM_NAME, receipt = purchaseToken)

                network.httpPostJSON(activateProductRoute, body, mapOf("Authorization" to "Bearer $token"), RES_ActivateProduct::class.java)
                { data, err ->
                    completion(data, err)
                }
            }
        }
    }

    private fun getUserToken(completion : (token : String?, err : Exception?) -> Unit)
    {
        val user = viewModel.user.value;

        if(user != null)
        {
            user.getIdToken(true /* force refresh */).addOnSuccessListener(OnSuccessListener { result ->
                completion(result.token, null)
            }).addOnFailureListener(OnFailureListener {
                completion(null, Exception(it.localizedMessage))
            })
        }
        else
        {
            completion(null, Exception("Login to perform this action"))
        }
    }
}