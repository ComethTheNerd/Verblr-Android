package com.quantumcommune.verblr

import androidx.lifecycle.MutableLiveData
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.SkuType
import com.quantumcommune.verblr.ui.main.VRBViewModel


// [dho] adapted from : https://stackoverflow.com/a/59565726/300037 - 08/06/20
object ShopDataBridge {
    val membershipUpdate by lazy {
        MutableLiveData<DATA_UserMembership>()
    }
}

class Shop(val activity : MainActivity, val api : API, val viewModel : VRBViewModel) : PurchasesUpdatedListener
{
    private val billingClient: BillingClient = BillingClient.newBuilder(activity)
        // [dho] "Note that if you do not call enablePendingPurchases(), you cannot instantiate the Google Play Billing Library."
        // https://developer.android.com/google/play/billing/billing_library_overview#pending
        // - 09/06/20
        .enablePendingPurchases()
        .setListener(this)
        .build()

    private var isConnected = false

    private val SKUTypes = arrayOf(BillingClient.SkuType.SUBS, BillingClient.SkuType.INAPP)

    val subscriptionProductGroupID = activity.getString(R.string.subscription_product_group_id)
    val topupProductGroupID = activity.getString(R.string.topup_product_group_id)


    fun close()
    {
        billingClient?.endConnection()
    }


    private fun withConnection(work: (billingClient : BillingClient?, err : Exception?) -> Unit)
    {
        if(isConnected)
        {
            work(billingClient, null)
            return
        }

//        billingClient = BillingClient.newBuilder(activity)
//            // [dho] "Note that if you do not call enablePendingPurchases(), you cannot instantiate the Google Play Billing Library."
//            // https://developer.android.com/google/play/billing/billing_library_overview#pending
//            // - 09/06/20
//            .enablePendingPurchases()
//            .setListener(this)
//            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                // [dho] IMPORTANT NOTE does NOT work inside emulator...
                // have to run it on a physical device to connect successfully - 11/06/20
                if (billingResult.responseCode ==  BillingClient.BillingResponseCode.OK) {
                    isConnected = true
                    // The BillingClient is ready. You can query purchases here.
                    work(billingClient, null)
                }
                else
                {
                    isConnected = false
//                    billingClient?.endConnection()
                    work(null, maybeParseError(billingResult))
                }
            }
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.

                /*
                *
                * It's strongly recommended that you implement your own connection retry policy and
                * override the onBillingServiceDisconnected() method. Make sure you maintain the
                * BillingClient connection when executing any methods.
                * */
//                billingClient?.endConnection()
//                billingClient = null
                isConnected = false
            }
        })
    }

    fun processAnyWaitingPurchases(completion : (err : Exception?) -> Unit)
    {
        processAnyWaitingPurchasesSequentially(skuTypes = SKUTypes, index = 0, completion = completion)
    }

    private fun processAnyWaitingPurchasesSequentially(skuTypes : Array<String>, index: Int, completion : (err : Exception?) -> Unit)
    {
        withConnection {
            billingClient, err ->

            if(err !== null)
            {
                completion(err)
            }
            else if(billingClient == null)
            {
                completion(Exception("Could not instantiate billing client"))
            }
            else if(skuTypes.count() > index)
            {
                val skuType = skuTypes[index]

                val result = billingClient?.queryPurchases(skuType)

                handleResult(result.billingResult, result.purchasesList)
                {
                    err ->
                    if(err !== null)
                    {
                        completion(err)
                    }
                    else
                    {
                        processAnyWaitingPurchasesSequentially(skuTypes, index + 1, completion)
                    }
                }
            }
            else
            {
                completion(null)
            }
        }

    }

//    fun canSubscribe() : Boolean = billingClient?.isFeatureSupported(BillingClient.FeatureType.SUBSCRIPTIONS).responseCode == BillingClient.BillingResponseCode.OK


    fun checkout(skuDetails : SkuDetails, completion : (err : Exception?) -> Unit)
    {
        withConnection { billingClient, err ->

            if(err !== null)
            {
                ThreadUtils.ensureBGThreadExec { completion(err) }
            }
            else if(billingClient == null)
            {
                ThreadUtils.ensureBGThreadExec { completion(Exception("Could not instantiate billing client")) }
            }
            else
            {
                // [dho] adapted from : https://developer.android.com/google/play/billing/billing_library_overview#Enable - 09/06/20
                ThreadUtils.ensureMainThreadExec {
                    val userID = viewModel.user_membership.value?.userID

                    if(userID == null)
                    {
                        ThreadUtils.ensureBGThreadExec { completion(Exception("Please log in and try your purchase again")) }
                    }
                    else
                    {
                        val flowParams = BillingFlowParams.newBuilder()
                            .setSkuDetails(skuDetails)
                            .setObfuscatedAccountId(userID)
                            .build()

                        val billingResult = billingClient.launchBillingFlow(activity, flowParams)

                        ThreadUtils.ensureBGThreadExec {
                            val err = maybeParseError(billingResult)
                            completion(err)
                        }
                    }
                }
            }
        }
    }

    fun fetchProductInfo(products : Map<String /*product id*/, DATA_Product>,
                         completion : (details : Map<String /*product id*/, SkuDetails>?, err : Exception?) -> Unit) {
        val subSKUs = products.entries.filter {
            it.value.productGroupID == subscriptionProductGroupID && it.value.available
        } .map {
            it.key
        }

        val topupSKUs = products.entries.filter {
            it.value.productGroupID == topupProductGroupID && it.value.available
        } .map {
            it.key
        }

        val details = mutableMapOf<String, SkuDetails>()

        // [dho] NOTE doing subs and topups as sequential requests rather than parallel
        // just to make it easier to report a singular error if it happens - 11/06/20
        if(subSKUs.isNotEmpty())
        {
            querySKUDetails(SkuType.SUBS, subSKUs.toMutableList())
            { data, err ->
                if(err == null)
                {
                    addSKUDetailsToMap(data, details)

                    if(topupSKUs.isNotEmpty())
                    {
                        querySKUDetails(SkuType.INAPP, topupSKUs.toMutableList())
                        {
                            data, err ->

                                addSKUDetailsToMap(data, details)
                                completion(details, err);
                        }
                    }
                    else
                    {
                        completion(details, null)
                    }
                }
                else
                {
                    completion(null, err);
                }
            }
        }
        else if(topupSKUs.isNotEmpty())
        {
            querySKUDetails(SkuType.INAPP, topupSKUs.toMutableList())
            {
                data, err ->
                    addSKUDetailsToMap(data, details)

                    completion(details, err);
            }
        }
        else
        {
            completion(details, null)
        }
    }


    private fun querySKUDetails(type : String, skus : MutableList<String>, completion: (skuDetails : Collection<SkuDetails>?, err: Exception?) -> Unit)
    {
        withConnection { billingClient, err ->
            if (err !== null)
            {
                completion(null, err)
            } else if (billingClient == null)
            {
                completion(null, Exception("Could not instantiate billing client"))
            } else
            {
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(skus).setType(type)

                billingClient.querySkuDetailsAsync(params.build()) {
                        billingResult, skuDetailsList ->
                    // Process the result.

                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        completion(skuDetailsList, null)
                    }
                    else
                    {
                        val message = if(billingResult.debugMessage.isNotEmpty()) billingResult.debugMessage else "Something went wrong (code: ${billingResult.responseCode})"

                        val err = Exception("Failed to fetch product info. $message")

                        completion(null, err)
                    }
                }
            }
        }
    }

    private fun addSKUDetailsToMap(data : Collection<SkuDetails>?, details : MutableMap<String, SkuDetails>)
    {
        data?.forEach {
            details[it.sku] = it
        }
    }

    override fun onPurchasesUpdated(billingResult: BillingResult?, purchases: MutableList<Purchase>?) {
        handleResult(billingResult, purchases)
        {
            err ->
        }
    }

    private fun handleResult(billingResult: BillingResult?, purchases: MutableList<Purchase>?, completion : (err : Exception?) -> Unit) {
        val err = maybeParseError(billingResult)

        if(err !== null)
        {
            completion(err)
            return
        }

        if(purchases != null)
        {
            processPurchasesSequentially(
                membership = viewModel.user_membership.value,
                purchases = purchases,
                index = 0
            ){
                membership, err ->

                if(membership !== null)
                {
                    ShopDataBridge.membershipUpdate.postValue(membership)
                }

                completion(err)
            }
        }
        else
        {
            completion(null)
        }
    }

    private fun processPurchasesSequentially(membership : DATA_UserMembership?, purchases : MutableList<Purchase>, index : Int, completion : (membership : DATA_UserMembership?, err : Exception?) -> Unit)
    {
        if(purchases.count() > index)
        {
            val purchase = purchases[index]

            when(purchase.purchaseState)
            {
                Purchase.PurchaseState.PURCHASED -> {
                    // Here you can confirm to the user that they've started the pending
                    // purchase, and to complete it, they should follow instructions that
                    // are given to them. You can also choose to remind the user in the
                    // future to complete the purchase if you detect that it is still
                    // pending.

                    if(purchase.isAcknowledged)
                    {
                        processPurchasesSequentially(membership, purchases, index + 1, completion);
                    }
                    else
                    {
                        InProgressOperations(viewModel).addFlags(InProgressOperations.FLAG_purchase)
                        // Grant the item to the user, and then acknowledge the purchase
                        api.activateProduct(purchase.purchaseToken)
                        {
                                data, err ->

                            if(err !== null)
                            {
                                completion(null, err)
                            }
                            else
                            {
                                processPurchasesSequentially(data?.membership ?: membership, purchases, index + 1, completion)
                            }
                        }
                    }
                }

                Purchase.PurchaseState.PENDING -> {
                    // Here you can confirm to the user that they've started the pending
                    // purchase, and to complete it, they should follow instructions that
                    // are given to them. You can also choose to remind the user in the
                    // future to complete the purchase if you detect that it is still
                    // pending.
                    InProgressOperations(viewModel).addFlags(InProgressOperations.FLAG_purchase)
                    processPurchasesSequentially(membership, purchases, index + 1, completion)
                }

                else -> {
                    processPurchasesSequentially(membership, purchases, index + 1, completion)
                }
            }
        }
        else
        {
            InProgressOperations(viewModel).removeFlags(InProgressOperations.FLAG_purchase)
            completion(membership, null)
        }
    }

    private fun maybeParseError(billingResult: BillingResult?) : Exception?  = when(billingResult?.responseCode)
    {
        BillingClient.BillingResponseCode.OK -> null
        BillingClient.BillingResponseCode.SERVICE_TIMEOUT ->
            Exception("The purchase request timed out. Please try again")
        BillingClient.BillingResponseCode.FEATURE_NOT_SUPPORTED ->
            Exception("In app purchases are not available on this device")
        BillingClient.BillingResponseCode.SERVICE_DISCONNECTED ->
            Exception("The connection to the server was lost. Please try again")
        BillingClient.BillingResponseCode.USER_CANCELED -> null
        BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE ->
            Exception("The purchase service is currently unavailable. Please try again shortly")
        BillingClient.BillingResponseCode.BILLING_UNAVAILABLE ->
            Exception("The billing service is currently unavailable. Please try again shortly")
        BillingClient.BillingResponseCode.ITEM_UNAVAILABLE ->
            Exception("The requested item is currently unavailable. Please try again shortly")
        BillingClient.BillingResponseCode.DEVELOPER_ERROR ->
            Exception(billingResult.debugMessage ?: "Something went wrong")
        BillingClient.BillingResponseCode.ERROR ->
            Exception(billingResult.debugMessage ?: "Something went wrong")
        BillingClient.BillingResponseCode.ITEM_ALREADY_OWNED ->
            Exception("The requested item is already owned")
        BillingClient.BillingResponseCode.ITEM_NOT_OWNED ->
            Exception("The requested item is not owned")
        else -> Exception(billingResult?.debugMessage ?: "Something went wrong")

    }
}