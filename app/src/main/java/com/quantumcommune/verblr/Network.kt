package com.quantumcommune.verblr

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import com.android.volley.*
import com.android.volley.toolbox.HttpHeaderParser
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.*


class Network(val context : Context)
{
    companion object {
        val TEMP_DOWNLOADS_DIR_NAME = "downloads"


        fun parseURLFileExt(url : String) : String? {
            var basis = url
            val questionMarkIdx = url.indexOf('?');

            if(questionMarkIdx > -1)
            {
                basis = basis.substring(0, questionMarkIdx)
            }

            val lastDotIdx = basis.lastIndexOf('.');

            if(lastDotIdx > -1 && lastDotIdx !== basis.length - 1)
            {
                // [dho] NOTE +1 to strip off the preceding dot - 13/06/20
                return basis.substring(lastDotIdx + 1)
            }

            return null
        }
    }

    // Instantiate the RequestQueue.
    private val queue = Volley.newRequestQueue(context)

    fun <T> httpGetJSON(url : String, headers : Map<String, String>? = null, type : Class<T>, completion : (data : T?, err : Exception?) -> Unit)
    {
        val stringRequest = object : StringRequest(
            Request.Method.GET,
            url,
            Response.Listener<String> { jsonString ->
                try {
                    val data = Gson().fromJson(jsonString, type)

                    completion(data, null)
                }
                catch(err : JsonSyntaxException) {
                    completion(null, Exception("Response data was malformed"));
                }
            },
            Response.ErrorListener { completion(null, parseError(it)) }
        )
        {
            // [dho] adapted from : https://stackoverflow.com/a/19601084/300037 - 18/05/20
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val h = mutableMapOf<String, String>();

                headers?.entries?.forEach { entry -> h[entry.key] = entry.value }

                h["Content-Type"] = "application/json";

                return h;
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest) // sends it
    }

    fun <T> httpPostJSON(url : String, body : Any, headers : Map<String, String>? = null, type : Class<T>, completion : (data : T?, err : Exception?) -> Unit)
    {
        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            Response.Listener<String> { jsonString ->
                try {
                    val data = Gson().fromJson(jsonString, type)

                    completion(data, null)
                }
                catch(err : JsonSyntaxException)
                {
                    completion(null, Exception("Response data was malformed"));
                }
            },
            Response.ErrorListener {
                completion(null, parseError(it))
            }
        )
        {
            // [dho] adapted from : https://stackoverflow.com/a/19601084/300037 - 18/05/20
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val h = mutableMapOf<String, String>();

                headers?.entries?.forEach { entry -> h[entry.key] = entry.value }

                h["Content-Type"] = "application/json";

                return h;
            }

            override fun getBody(): ByteArray {
                return Gson().toJson(body).toByteArray();
            }

            override fun getBodyContentType(): String {
                return "application/json"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest) // sends it
    }

    fun s3Upload(url : String, filename : String, payload : Any, fields : Map<String, String>, completion : (err: Exception?) -> Unit)
    {
        val boundary = UUID.randomUUID().toString();

        val stringRequest = object : StringRequest(
            Request.Method.POST,
            url,
            Response.Listener<String> { _ ->
                completion(null)
            },
            Response.ErrorListener { completion(parseError(it)) }
        )
        {
            // [dho] adapted from : https://stackoverflow.com/a/19601084/300037 - 18/05/20
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                return mapOf<String, String>(
                    "Content-Type" to "multipart/form-data; boundary=$boundary"
                );
            }

            override fun getBody(): ByteArray {
                val jsonString = Gson().toJson(payload);

                val data = ByteArrayOutputStream()

                fields.forEach { field ->
                    val fieldName = field.key;
                    val fieldValue = field.value;

                    data.write("\r\n--$boundary\r\n".toByteArray(Charsets.UTF_8))
                    data.write("Content-Disposition: form-data; name=\"$fieldName\"\r\n\r\n".toByteArray(Charsets.UTF_8))
                    data.write(fieldValue.toByteArray(Charsets.UTF_8))
                }

                data.write("\r\n--$boundary\r\n".toByteArray(Charsets.UTF_8))
                data.write("Content-Disposition: form-data; name=\"file\"; filename=\"$filename\"\r\n".toByteArray(Charsets.UTF_8))
                data.write("Content-Type: application/json\r\n\r\n".toByteArray(Charsets.UTF_8))
                data.write(jsonString.toByteArray(Charsets.UTF_8))

                data.write("\r\n--$boundary--\r\n".toByteArray(Charsets.UTF_8))

                return data.toByteArray()
            }

            override fun getBodyContentType(): String {
                return "multipart/form-data; boundary=$boundary"
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest) // sends it
    }


    private var downloadBroadcastReceiver : BroadcastReceiver? = null
    private var downloadCallbacks = mutableMapOf<Long, DownloadRequest>()

    data class DownloadRequest(
//        val tempPath : String,
        val destinationFile : File,
        val completion: (err: Exception?) -> Unit
    )

    inner class DownloadBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val downloadManager =  context?.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager ?: return;
            // [dho] https://developer.android.com/reference/android/app/DownloadManager#EXTRA_DOWNLOAD_ID - 23/06/20
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, Long.MIN_VALUE) ?: return;
            val downloadRequest = downloadCallbacks.get(id) ?: return

            // [dho] onReceive always runs on UI thread apparently so we delegate off it
            // https://stackoverflow.com/a/5676888/ - 24/06/20
            ThreadUtils.bgThreadExec { handleDownloadResult(downloadManager, id, downloadRequest) }
        }
    }

    @Synchronized fun downloadFile(title : String, description : String, from : String, to : String, completion : (err: Exception?) -> Unit)
    {
        ThreadUtils.ensureBGThreadExec {
            if(downloadBroadcastReceiver == null)
            {
                context.registerReceiver(DownloadBroadcastReceiver(), IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))
            }

            val downloadManager =  context.getSystemService(Context.DOWNLOAD_SERVICE) as? DownloadManager;

            if(downloadManager == null)
            {
                completion(Exception("Could not get download manager"))
            }
            else
            {
                val uri = Uri.parse(from); // "http://www.example.com/myfile.mp3"

                val request = DownloadManager.Request(uri);
                request.setTitle(title); // "My File"
                request.setDescription(description); // "Downloading"
                request.setAllowedOverMetered(true) // [dho] TODO configurable by user? - 23/06/20
                request.setAllowedOverRoaming(true) // [dho] TODO configurable by user? - 23/06/20

                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_HIDDEN);
                request.setVisibleInDownloadsUi(false);

                // [dho] the DownloadManager cannot access the internal files dir for the app due to security
                // constraints, so we have to download to an external temp file first, and then copy the file
                // to internal storage in our broadcast receiver handler. This is better anyway because it means
                // the app will not get confused if there is a partially downloaded file in the local performance
                // directory, ie. if a file is there, guaranteed it will be complete - 24/06/20
                val tempPath = "${context.getExternalFilesDir(null)}/$TEMP_DOWNLOADS_DIR_NAME/${uuid()}"

                val destinationPath = to
                val destinationFileURI = Uri.parse(destinationPath)
                val destinationFile = File(destinationFileURI.path)

                request.setDestinationUri(Uri.parse("file://$tempPath")); // folderName + "/myfile.mp3"

                // [dho] NOTE result handled in a broadcast receiver, hence the need for a map to store
                // callbacks etc - 24/06/20
                downloadCallbacks[downloadManager.enqueue(request)] = DownloadRequest(
//                    tempPath = tempPath,
                    destinationFile = destinationFile,
                    completion = completion
                )
            }
        }

    }

    private fun handleDownloadResult(downloadManager : DownloadManager, id : Long, downloadRequest : DownloadRequest)
    {
        try {
            val downloadQuery = DownloadManager.Query();
            //set the query filter to our previously Enqueued download
            downloadQuery.setFilterById(id);

            //Query the download manager about downloads that have been requested.
            val cursor = downloadManager.query(downloadQuery);

            if(!cursor.moveToFirst())
            {
                return;
            }

            //column for download  status
            val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
            val status = cursor.getInt(columnIndex);
            //column for reason code if the download failed or paused
            val columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);
            val reason = cursor.getInt(columnReason);

            when(status)
            {
                DownloadManager.STATUS_FAILED -> {
                    downloadRequest.completion(Exception("Download failed (code: $reason)"))
                }
                DownloadManager.STATUS_PAUSED -> {

                }
                DownloadManager.STATUS_PENDING -> {}
                DownloadManager.STATUS_RUNNING -> {}
                DownloadManager.STATUS_SUCCESSFUL -> {
                    try {
                        val tempPath = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))
                        val tempFileURI = Uri.parse(tempPath)
                        val tempFile = File(tempFileURI.path)

                        tempFile.copyTo(downloadRequest.destinationFile, true /* overwrite */)
                        tempFile.delete()

                        downloadRequest.completion(null)
                    }
                    catch(err : Exception)
                    {
                        downloadRequest.completion(err)
                    }
                }
            }

            downloadCallbacks.remove(id)
        }
        catch(err : Exception)
        {
            // TODO index out of bounds query exceptions if cursor is empty etc? - 23/06/20
        }
    }

    fun isPlayableURL(url : String, contentType : String, completion : (result : Boolean?, error : Exception?) -> Unit)
    {
        _isContentTypeURLByRangeRequest(url, contentType, completion)
    }

    private fun _isContentTypeURLByRangeRequest(url : String, contentType : String, completion : (result : Boolean?, err : Exception?) -> Unit)
    {
        var isMatch = false;

        val stringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener { completion(isMatch, null) },
            Response.ErrorListener { completion(isMatch, it) }
        )
        {
            // [dho] adapted from : https://stackoverflow.com/a/19601084/300037 - 18/05/20
            @Throws(AuthFailureError::class)
            override fun getHeaders(): Map<String, String> {
                val h = mapOf<String, String>(
                    "Range" to "bytes=0-1"
                );

                return h;
            }

            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                isMatch = isContentTypeMatch(response, contentType)

                // [dho] DISABLING this because
                // 1. we do not care about parsing it, only the header we have interrogated
                // 2. it can trigger a java.lang.OutOfMemoryError
                //
                // - 23/06/20
//                return super.parseNetworkResponse(response)
                return parseVolleyResponse(response)
            }
        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest) // sends it
    }

    fun _isContentTypeURLByBasicRequest(url : String, contentType : String, completion : (result : Boolean?, err : Exception?) -> Unit)
    {
        var isMatch = false;

        val stringRequest = object : StringRequest(
            Method.GET,
            url,
            Response.Listener<String> { completion(isMatch, null) },
            Response.ErrorListener { completion(isMatch, it) }
        )
        {
            override fun parseNetworkResponse(response: NetworkResponse?): Response<String> {
                isMatch = isContentTypeMatch(response, contentType)

                // [dho] DISABLING this because
                // 1. we do not care about parsing it, only the header we have interrogated
                // 2. it can trigger a java.lang.OutOfMemoryError
                //
                // - 23/06/20
//                return super.parseNetworkResponse(response)
                return parseVolleyResponse(response)
            }

        }

        // Add the request to the RequestQueue.
        queue.add(stringRequest) // sends it
    }

    private fun isContentTypeMatch(response: NetworkResponse?, contentType : String)
            = response?.headers?.get("Content-Type") == contentType

    private fun parseVolleyResponse(response: NetworkResponse?)
            = when (response?.statusCode) {
        in 200..299 -> {
            Response.success("", HttpHeaderParser.parseCacheHeaders(response))
        }
        else -> {
            Response.error(VolleyError(response))
        }
    }


    fun parseError(v : VolleyError) : Exception
    {
        val jsonString = v.networkResponse.data.toString(Charsets.UTF_8)

        try {
            val error = Gson().fromJson(jsonString, DATA_Error::class.java)

            if(error?.message != null)
            {
                return Exception(error.message)
            }
        }
        catch(err : JsonSyntaxException)
        {

        }

        return Exception(v.localizedMessage ?: "Something went wrong")
    }

    fun destroy()
    {
        if(downloadBroadcastReceiver != null)
        {
            try {
                context.unregisterReceiver(downloadBroadcastReceiver)
            }
            catch(err : Exception)
            {

            }

        }
    }

}


