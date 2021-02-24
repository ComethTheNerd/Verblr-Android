package com.quantumcommune.verblr

enum class ArticleStatusFlag(val value : Int)
{
    AVAILABLE_OFFLINE(0x1),
    PROCESSING_IN_PROGRESS(0x2),
    PROCESSING_ERROR(0x4),
    CONTENT_QUALITY_WARNING(0x8),

    ANY_ISSUE(PROCESSING_IN_PROGRESS.value or PROCESSING_ERROR.value or CONTENT_QUALITY_WARNING.value)
}

class ArticleOps(val localCache: LocalCache, val membershipOps: MembershipOps) {
    companion object
    {
        val STATUS_active = "active"
        val STATUS_fail = "fail"
        val STATUS_success = "success"

        fun sort(articles : Collection<DATA_Article>, localCache: LocalCache, remoteLastAccessed: DATA_UserLastAccessed?) : List<DATA_Article>
        {
            val utcs = mutableMapOf<String, String>()

            val localLastAccessed = localCache.readLocalFileLastAccessUTC()

            articles.forEach { utcs[it.id] = getArticleSortDate(it, localCache, localLastAccessed, remoteLastAccessed) }

            return articles.sortedWith(Comparator { a1 : DATA_Article, a2 : DATA_Article -> DateTimeUtils.compareUTC(utcs[a2.id]!!, utcs[a1.id]!!) })
        }

        fun getArticleSortDate(article: DATA_Article, localCache : LocalCache, localLastAccessed: LocalFileLastAccessed, remoteLastAccessed : DATA_UserLastAccessed?) = (
                getPerformanceLastLocalAccessUTC(article, localCache, localLastAccessed) ?:
                getPerformanceLastRemoteAccessUTC(article, remoteLastAccessed) ?:
                // [dho] when the user added the article to their library - 25/06/20
                article.addedToLibraryUTC ?:
                // [dho] when the article was last modified by the author - 25/06/20
                article.details.modified ?:
                // [dho] when the article was published by the author - 25/06/20
                article.details.published ?:
                // [dho] NOTE if the article already existed in the system, then this
                // will be the date corresponding to when the user that added this
                // article added it, which may not be the same user as the one logged in here - 25/06/20
                article.meta.creationUTC
        )

        fun getPerformanceLastLocalAccessUTC(article : DATA_Article, localCache : LocalCache, localLastAccessed: LocalFileLastAccessed) : String?
        {
            val path = localCache.cachedPerformanceFilePathIfExists(article.id)

            if(path != null)
            {
                val utc = localLastAccessed.utc[path]

                if(utc != null)
                {
                    return utc;
                }
            }

            return null
        }

        fun getPerformanceLastRemoteAccessUTC(article : DATA_Article, remoteLastAccessed : DATA_UserLastAccessed?)
            = remoteLastAccessed?.performances?.values?.filter { it.articleID == article.id }?.sortedWith(
                performanceAccessUTCLatestFirstComparator
            )?.firstOrNull()?.accessUTC

        private val performanceAccessUTCLatestFirstComparator = Comparator {
            p1 : Analytics_UserLastAccessedPerformance, p2 : Analytics_UserLastAccessedPerformance ->
            DateTimeUtils.compareUTC(p2.accessUTC, p1.accessUTC)
        }

        // [dho] NOTE using >0 rather than equality, so we can things like `ANY_WARNING` - 20/06/20
        fun hasStatusFlagBitsInCommon(input : Int, flag : ArticleStatusFlag) = (input and flag.value) > 0

        fun isProcessing(article: DATA_Article): Boolean = article.status == STATUS_active

        fun isFailed(article: DATA_Article): Boolean = article.status == STATUS_fail

        fun hasContentQualityWarning(article: DATA_Article): Boolean {
            val didSubmitSource = article.clientIncludedSource ?: false;
            val isAmp = article.source.amp;

            if(!didSubmitSource && !isAmp)
            {
                return true
            }

            val hasContentRoot = article.inferredContentRoot ?: false;

            if(!hasContentRoot)
            {
                return true
            }


            if(article.details.title == null || article.details.description == null)
            {
                return true;
            }

            return false;
        }
    }

    fun isProcessing(article: DATA_Article) = ArticleOps.isProcessing(article)

    fun isFailed(article: DATA_Article) = ArticleOps.isFailed(article)

    fun hasContentQualityWarning(article: DATA_Article) = ArticleOps.hasContentQualityWarning(article)

    fun getStatusFlags(membership: DATA_UserMembership?, article : DATA_Article) : Int
    {
        var statusFlags = 0x0

        if(isAvailableOffline(membership, article))
        {
            statusFlags = statusFlags or ArticleStatusFlag.AVAILABLE_OFFLINE.value
        }

        if(isFailed(article))
        {
            statusFlags = statusFlags or ArticleStatusFlag.PROCESSING_ERROR.value
        }

        if(hasContentQualityWarning(article))
        {
            statusFlags = statusFlags or ArticleStatusFlag.CONTENT_QUALITY_WARNING.value
        }

        if(isProcessing(article))
        {
            statusFlags = statusFlags or ArticleStatusFlag.PROCESSING_IN_PROGRESS.value
        }

        return statusFlags
    }



    fun isAvailableOffline(membership: DATA_UserMembership?, article: DATA_Article): Boolean {
        if(membershipOps.useOfflineCaching(membership))
        {
           return localCache.cachedPerformanceFilePathIfExists(article.id) != null
        }

        return false
    }

    fun isLoaded(player : Player, article: DATA_Article): Boolean {

//        Log.d("ARTICLE_OPS", "a : ${player.article()?.id == article.id} :: b : ${player.isLastLoadedArticleID(article.id)} :: c : ${!player.isLoading()}")

        return player.article()?.id == article.id &&
                player.isLastLoadedArticleID(article.id) &&
                !player.isLoading()//player.isLoadingOrWaitingToPlay()
    }

    // [dho] removing this because it was ported from iOS where the player has a concept
    // of 'waiting to play' which means it is collecting enough data to start - 17/06/20
//    fun isLoadingOrWaitingToPlay(player : Player, article: DATA_Article): Boolean {
//        return player.article()?.id == article.id && player.isLoadingOrWaitingToPlay()
//    }
    fun isLoading(player : Player, article: DATA_Article): Boolean {
        return player.article()?.id == article.id && player.isLoading()
    }

    fun isPlaying(player : Player, article: DATA_Article): Boolean {
        return player.article()?.id == article.id && player.currentStatus.state == LocalMediaPlayerService.STATE_playing
    }
//
//    fun playingState(player : Player, article: DATA_Article) = when {
//        isPlaying(player, article) -> {
//            LocalMediaPlayerService.STATE_playing
//        }
//        isLoading(player, article) -> {
//            LocalMediaPlayerService.STATE_loading
//        }
//        isLoaded(player, article) -> {
//            LocalMediaPlayerService.STATE_paused
//        }
//        else -> {
//            null
//        }
//    }


}