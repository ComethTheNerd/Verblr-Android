package com.quantumcommune.verblr

import android.app.Activity
import android.content.Context
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.quantumcommune.verblr.ui.main.VRBViewModel
import java.io.File

data class LocalFileLastAccessed(
    val utc : Map<String, String>
)

class LocalCache(private val activity: Activity, private val viewModel: VRBViewModel) {
    private val prefs = activity.getSharedPreferences("com.quantumcommune.verblr", Context.MODE_PRIVATE)



    companion object {
        val KEY_profile = "profile"
        val KEY_library = "library"
        val KEY_deviceToken = "deviceToken"
        val KEY_jobs = "jobs"
        val KEY_membership = "membership"
        val KEY_config = "config"
        val KEY_lastAccessed = "lastAccessed"
        val KEY_urls = "urls"
        val KEY_localFileLastAccessed = "localFileLastAccessed"

//        val PATH_cachedPerformancePrefix = "article"

        val DOWNLOADED_PERFORMANCES_DIR_NAME = "articles"

        fun getCachedFilesDir(context : Context) : File
        {
            return /*context.getExternalFilesDir(null) ?: */context.filesDir
        }

        fun getCachedPerformancesDir(context : Context) : File
        {
            return File(getCachedFilesDir(context),DOWNLOADED_PERFORMANCES_DIR_NAME)
        }
    }


    private inline fun <reified T> readCached(key : String) : T?
    {
        val jsonString = prefs.getString(key, null)

        if(jsonString != null)
        {
            try {
                return Gson().fromJson(jsonString, T::class.java)
            }
            catch(err : JsonSyntaxException) {
                val editor = prefs.edit()
                editor.remove(key)
                // [dho] NOTE using `commit` to synchronously write to disk,
                // rather than `apply` which applies changes in memory and
                // then asynchronously writes to disk - 18/05/20
                editor.commit()
            }
        }

        return null
    }

    private inline fun <reified T> updateCached(key : String, data : T?) : Boolean
    {
        val editor = prefs.edit()
        var didWrite = false;

        if(data != null)
        {
            val jsonString = Gson().toJson(data)
            editor.putString(key, jsonString)
            didWrite = true;
        }
        else
        {
            editor.remove(key)
        }

        // [dho] NOTE using `commit` to synchronously write to disk,
        // rather than `apply` which applies changes in memory and
        // then asynchronously writes to disk - 18/05/20
        editor.commit()

        return didWrite;
    }

    fun readCachedUserProfile() : DATA_UserProfile?
    {
        return readCached(KEY_profile);
    }

    @Synchronized fun updateCachedUserProfile(profile : DATA_UserProfile?) : Boolean
    {
        return updateCached(KEY_profile, profile)
    }

    fun readCachedUserLibrary() : DATA_UserLibrary?
    {
        return readCached(KEY_library);
    }

    @Synchronized fun updateCachedUserLibrary(library : DATA_UserLibrary?) : Boolean
    {
        return updateCached(KEY_library, library)
    }

    fun readCachedUserJobs() : DATA_UserJobs?
    {
        return readCached(KEY_jobs);
    }

    @Synchronized fun updateCachedUserJobs(jobs: DATA_UserJobs?) : Boolean
    {
        return updateCached(KEY_jobs, jobs)
    }

    fun readCachedUserMembership() : DATA_UserMembership?
    {
        return readCached(KEY_membership);
    }

    @Synchronized fun updateCachedUserMembership(membership: DATA_UserMembership?) : Boolean
    {
        return updateCached(KEY_membership, membership)
    }

    fun readCachedUserConfig() : DATA_UserConfig?
    {
        return readCached(KEY_config);
    }

    @Synchronized fun updateCachedUserConfig(config: DATA_UserConfig?) : Boolean
    {
        return updateCached(KEY_config, config)
    }

    fun readCachedUserLastAccessed() : DATA_UserLastAccessed?
    {
        return readCached(KEY_lastAccessed);
    }

    @Synchronized fun updateCachedUserLastAccessed(lastAccessed: DATA_UserLastAccessed?) : Boolean
    {
        return updateCached(KEY_lastAccessed, lastAccessed)
    }

    fun readCachedUserURLs() : DATA_UserURLs?
    {
        return readCached(KEY_urls);
    }

    @Synchronized fun updateCachedUserURLs(urls: DATA_UserURLs?) : Boolean
    {
        return updateCached(KEY_urls, urls)
    }

    fun readCachedDeviceToken() :  NOTIFICATION_DeviceToken? {
        return readCached(KEY_deviceToken)
    }

    @Synchronized fun updateCachedDeviceToken(deviceToken: NOTIFICATION_DeviceToken?) : Boolean
    {
        return updateCached(KEY_deviceToken, deviceToken)
    }

    @Synchronized fun deleteCachedPerformances(completion : (err : Exception?) -> Unit)
    {
        ThreadUtils.ensureBGThreadExec {
            var didCallback = false

            try {
                getCachedPerformancesDir(activity).listFiles()?.forEach {
//                    if(it.startsWith(PATH_cachedPerformancePrefix))
//                    {
                    it.delete()
//                    }
                }
            }
            catch(err : Exception)
            {
                completion(err)
                didCallback = true
            }

            if(!didCallback)
            {
                completion(null)
            }
        }
    }

    @Synchronized fun updateLocalFileLastAccessUTC(path : String, utc : String)
    {
        ThreadUtils.ensureBGThreadExec {
            val updatedUTCMap = mutableMapOf<String, String>()

            readCached<LocalFileLastAccessed>(KEY_localFileLastAccessed)?.utc?.entries?.forEach {
                updatedUTCMap[it.key] = it.value
            }

            updatedUTCMap[path] = utc

            updateCached(KEY_localFileLastAccessed, LocalFileLastAccessed(utc = updatedUTCMap));
        }
    }


    fun readLocalFileLastAccessUTC() =
        readCached<LocalFileLastAccessed>(KEY_localFileLastAccessed) ?: LocalFileLastAccessed(utc = mapOf())

    fun readLocalFileLastAccessUTC(path: String) =
        readCached<LocalFileLastAccessed>(KEY_localFileLastAccessed)?.utc?.get(path)

    fun cachedPerformanceFilePath(articleID : String) : String
    {
        val child = performanceFilename(articleID)
        return "${getCachedPerformancesDir(activity).absolutePath}/$child"
    }

    fun cachedPerformanceFilePathIfExists(articleID : String) : String?
    {
        val child = performanceFilename(articleID)
        val path = "${getCachedPerformancesDir(activity).absolutePath}/$child"
        val file = File(path)

        return if(file.exists()) path else null
    }

//    @Synchronized fun updateCachedPerformanceFile(articleID: String, byteArray: ByteArray, completion : (path : String?, err : Exception?) -> Unit)
//    {
//        ThreadUtils.ensureBGThreadExec {
//            var err : Exception? = null
//            val child = performanceFilename(articleID)
//            val path = "${getCachedPerformancesDir(activity).absolutePath}/$child"
//            val file = File(getCachedPerformancesDir(activity).absolutePath, child);
//
//            val stream = FileOutputStream(file)
//
//            try {
//                stream.write(byteArray)
//            }
//            catch(e : Exception)
//            {
//                err = e
//            }
//            finally {
//                stream.close()
//            }
//
//            completion(path, err)
//        }
//    }

    fun performanceFilename(articleID : String) : String
    {
        val extension = viewModel.user_config.value?.performance_FileExtension ?: "wav";
        return "$articleID.$extension"
    }
}