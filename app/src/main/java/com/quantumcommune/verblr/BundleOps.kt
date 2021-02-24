package com.quantumcommune.verblr

import android.content.Context
import com.quantumcommune.verblr.ui.main.VRBViewModel

class BundleOps(private val context : Context, private val viewModel: VRBViewModel, private val localCache: LocalCache) {
    companion object
    {
        val FILTER_library = "library";
        val FILTER_membership = "membership";
        val FILTER_urls = "urls";
        val FILTER_lastAccessed = "lastAccessed";
        val FILTER_jobs = "jobs";
        val FILTER_config = "config";
        val FILTER_profile = "profile";

        val ALL_FILTERS = arrayOf(FILTER_profile, FILTER_jobs, FILTER_membership, FILTER_library, FILTER_config, FILTER_lastAccessed, FILTER_urls)
    }

    fun restore(completion : (bundle : DATA_Bundle?, err : Exception?) -> Unit)
    {
        ThreadUtils.ensureBGThreadExec {
            val cachedUserProfile = localCache.readCachedUserProfile()
            val cachedUserMembership = localCache.readCachedUserMembership()
            val cachedUserJobs = localCache.readCachedUserJobs()
            val cachedUserLibrary = localCache.readCachedUserLibrary()
            val cachedUserConfig = localCache.readCachedUserConfig()
            val cachedUserURLs = localCache.readCachedUserURLs()
            val cachedUserLastAccessed = localCache.readCachedUserLastAccessed()

            val restoredBundle = if(cachedUserConfig !== null) DATA_Bundle(
                profile = cachedUserProfile,
                membership = cachedUserMembership,
                jobs = cachedUserJobs,
                library = cachedUserLibrary,
                config = cachedUserConfig,
                urls = cachedUserURLs,
                lastAccessed = cachedUserLastAccessed
            ) else null

            ThreadUtils.ensureMainThreadExec {
                viewModel.user_profile.value = cachedUserProfile
                viewModel.user_membership.value = cachedUserMembership
                viewModel.user_jobs.value = cachedUserJobs
                viewModel.user_library.value = cachedUserLibrary
                viewModel.user_config.value = cachedUserConfig
                viewModel.user_urls.value = cachedUserURLs
                viewModel.user_lastAccessed.value = cachedUserLastAccessed

                completion(restoredBundle, null)
            }
        }
    }

    fun clear(completion : (err : Exception?) -> Unit)
    {
        consume(
            DATA_Bundle(
                profile = null,
                jobs = null,
                membership = null,
                library = null,
                config = null,
                lastAccessed = null,
                urls = null
            ),
            filters = ALL_FILTERS,
            completion = completion
        )
    }

    fun consume(bundle: DATA_Bundle, filters: Array<String> = arrayOf(), completion : (err : Exception?) -> Unit) {
        val willUpdateUserProfile = bundle.profile != null || filters.contains(FILTER_profile)
        val willUpdateUserJobs = bundle.jobs != null || filters.contains(FILTER_jobs)
        val willUpdateUserMembership =
            bundle.membership != null || filters.contains(FILTER_membership)
        val willUpdateUserLibrary = bundle.library != null || filters.contains(FILTER_library)
        val willUpdateUserConfig = bundle.config != null || filters.contains(FILTER_config)
        val willUpdateUserLastAccessed =
            bundle.lastAccessed != null || filters.contains(FILTER_lastAccessed)
        val willUpdateUserURLs = bundle.urls != null || filters.contains(FILTER_urls)

        ThreadUtils.ensureBGThreadExec {
            if (willUpdateUserProfile) {
                localCache.updateCachedUserProfile(bundle.profile);
            }

            if (willUpdateUserJobs) {
                localCache.updateCachedUserJobs(bundle.jobs);
            }

            if (willUpdateUserMembership) {
                localCache.updateCachedUserMembership(bundle.membership);
            }

            if (willUpdateUserLibrary) {
                localCache.updateCachedUserLibrary(bundle.library);
            }

            if (willUpdateUserConfig) {
                localCache.updateCachedUserConfig(bundle.config);
            }

            if (willUpdateUserLastAccessed) {
                localCache.updateCachedUserLastAccessed(bundle.lastAccessed);
            }

            if (willUpdateUserURLs) {

                localCache.updateCachedUserURLs(bundle.urls);

    //            val pendingAddArticleURLs : List<DATA_ProvisionedURL> = bundle.urls?.active?.filter {
    //                    element -> element.purpose == "add-article"
    //            } ?: listOf<DATA_ProvisionedURL>();
    //
    //            if(pendingAddArticleURLs.count() > 0)
    //            {
    //                scheduleBGAddArticleParamsQueueCheck()
    //            }
            }
        }

        ThreadUtils.ensureMainThreadExec {
            if(willUpdateUserProfile)
            {
                viewModel.user_profile.value = bundle.profile;
            }

            if(willUpdateUserJobs)
            {
                viewModel.user_jobs.value = bundle.jobs;
            }

            if(willUpdateUserMembership)
            {
                viewModel.user_membership.value = bundle.membership;
            }

            if(willUpdateUserLibrary)
            {
                viewModel.user_library.value = bundle.library;
            }

            if(willUpdateUserConfig)
            {
                viewModel.user_config.value = bundle.config;
            }

            if(willUpdateUserLastAccessed)
            {
                viewModel.user_lastAccessed.value = bundle.lastAccessed;
            }

            if(willUpdateUserURLs)
            {
                viewModel.user_urls.value = bundle.urls;

//                    val pendingAddArticleURLs : List<DATA_ProvisionedURL> = bundle.urls?.active?.filter {
//                            element -> element.purpose == "add-article"
//                    } ?: listOf<DATA_ProvisionedURL>();
//
//                    if(pendingAddArticleURLs.count() > 0)
//                    {
//                        scheduleBGAddArticleParamsQueueCheck()
//                    }
            }

            completion(null)
        }
    }

    fun updateCachedLastAccessedPerformanceIfNewer(lap : Analytics_UserLastAccessedPerformance, completion : (err : Exception?) -> Unit)
    {
        val oldLA = viewModel.user_lastAccessed.value

        val newLAPs = mutableMapOf<String, Analytics_UserLastAccessedPerformance>();

        oldLA?.performances?.entries?.forEach { entry -> newLAPs[entry.key] = entry.value }

        // [dho] if the performance hash was already in the map - 22/05/20
        val existingLAP = newLAPs[lap.performanceHash]

        if(existingLAP != null)
        {
            // [dho] only update the entry if the one we are trying to override it
            // with is newer - 22/05/20
            if(DateTimeUtils.compareUTC(existingLAP.accessUTC, lap.accessUTC) == -1)
            {
                newLAPs[lap.performanceHash] = lap;
            }
        }
        else
        {
            newLAPs[lap.performanceHash] = lap;
        }

        consume(
            DATA_Bundle(lastAccessed = DATA_UserLastAccessed(
                articles = oldLA?.articles ?: mapOf(),
                performances = newLAPs
            )),
            filters = arrayOf(BundleOps.FILTER_lastAccessed),
            completion = completion
        )
    }
}
