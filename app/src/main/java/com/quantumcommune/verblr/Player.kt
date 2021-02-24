package com.quantumcommune.verblr

import android.content.ComponentName
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.media2.common.MediaItem
import androidx.media2.common.MediaMetadata
import androidx.media2.common.SessionPlayer
import androidx.media2.session.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.quantumcommune.verblr.ui.main.VRBViewModel

class Player(private val activity : MainActivity, private val viewModel: VRBViewModel, private val localCache: LocalCache, private val api: API, private val network : Network) :
    MediaController.ControllerCallback() {

    companion object {
//        lateinit var shared : Player

        // [dho] singleton shared resource - 23/05/20
//        private var mediaPlayer : MediaPlayer? = null

//        val STATUS_playing = 1;
//        val STATUS_loading = 3;
//        val STATUS_paused = 2;

        val DetailsLinkMaxCharCount = 30

//        val DefaultPerformanceFileExtension = "wav"
    }

//    private var mediaPlayer : MediaPlayer? = null

    private var trackProgressPoll: TimeoutTask? = null

//    private var loadingArticle : Boolean = false
//    private var lastLoadedArticleID : String? = null

    // [dho] covers the case where the player is loading an article, or trying to launch
    // the media service - 17/06/20
    fun isLoading() = isLoadingArticle || (mediaControllerConnectionCallback != null);

    fun article() : DATA_Article? = viewModel.player_article.value

    val currentStatus : MediaPlayerStatus
        get() {
            val mc = mediaController
            if(mc != null)
            {
                return MediaPlayerStatus(
                    state =
                        if (isLoading()) LocalMediaPlayerService.STATE_loading
                        else parseState(mc.playerState),
                    duration = parseDuration(mc.duration),
                    progress = parseProgress(mc.currentPosition),
                    buffState = mc.bufferingState,
                    buffProgress = parseProgress(mc.bufferedPosition)
                )
            }
            else
            {
                return MediaPlayerStatus(
                    state = if (isLoading()) LocalMediaPlayerService.STATE_loading
                            else LocalMediaPlayerService.STATE_default,
                    duration = 0.0f,
                    progress = 0.0f,
                    buffState = SessionPlayer.BUFFERING_STATE_UNKNOWN,
                    buffProgress = 0.0f
                )
            }
        }


    private var mediaController : MediaController? = null;

    private var isLoadingArticle : Boolean = false
        set(value) {
            field = value
            updateObservers()
        }

    private var latestMediaPrepareParams : MediaPrepareParams? = null
    private var mediaControllerConnectionCallback : ((err : Exception?) -> Unit)? = null

    private var mediaPrepareResultObserver : Observer<MediaPrepareResult>? = null

    fun init(completion: (err : Exception?) -> Unit)
    {
        mediaControllerConnectionCallback = completion;

        updateObservers()

        val componentName = ComponentName(activity, LocalMediaPlayerService::class.java)
        val sessionToken = SessionToken(activity, componentName)

        mediaController = MediaController.Builder(activity).setControllerCallback(
            ContextCompat.getMainExecutor(activity),
            this
        )
            .setSessionToken(sessionToken)
//            .setSessionCompatToken()
            .build()

//        mediaController.prepare()
    }


//    fun state() : Int
//    {
//        return if(loadingArticle) {
//            LocalMediaPlayerService.STATE_loading
//        } else {
//            service.currentStatus().state
//        }
////        if(loadingArticle)
////        {
////            return STATUS_loading
////        }
////
////        if(mediaPlayer?.isPlaying == true)
////        {
////            return STATUS_playing
////        }
////        else
////        {
////            return STATUS_paused
////        }
//    }


    fun loadArticle(article : DATA_Article, useCache : Boolean, isCancellationRequested : (articleID : String) -> Boolean, completion : (lap : Analytics_UserLastAccessedPerformance?, error : Exception?) -> Unit) {
        ThreadUtils.ensureBGThreadExec {

            Log.d("loadArticle", "LOAD ARTICLE $article.id")

            viewModel.player_article.postValue(article)

            isLoadingArticle = true

            val prepStep =
                if(mediaController == null) this::init
                else this::pause;

            prepStep {
                err -> if(err != null)
                {
                    completion(null, err)
                }
                else
                {
                    var cacheHit = false

                    if(useCache)
                    {
                        Log.d("loadArticle", "CHECKING CACHE FOR $article.id")

                        val cachedPath = localCache.cachedPerformanceFilePathIfExists(article.id)

                        if(cachedPath != null)
                        {
                            cacheHit = true

                            val fileExt = Network.parseURLFileExt(cachedPath)
                            val contentType = if(fileExt != null) "audio/$fileExt" else LocalMediaPlayerService.DEFAULT_CONTENT_TYPE

                            loadURL(article/*.id*/, contentURL = cachedPath, contentType = contentType, isRemote = false)
                            {
                                    err ->
                                if(err != null)
                                {
                                    completion(null, err)
                                }
                                else
                                {
                                    val utc = DateTimeUtils.nowUTC()
                                    ThreadUtils.bgThreadExec {
                                        localCache.updateLocalFileLastAccessUTC(cachedPath, utc)
                                    }

                                    val lap = viewModel.user_lastAccessed.value?.performances?.entries?.firstOrNull {
                                            it -> it.value.articleID == article.id
                                    }?.value

                                    completion(lap, null)
                                }
                            }
                        }
                    }

                    if(!cacheHit)
                    {
                        api.accessPerformance(article.id)
                        {
                                admission, error ->

                            // [dho] check the article did not change in the meantime whilst we were
                            // waiting for the server - 22/05/20
                            // [dho] NOTE potential race condition here if the view model is updated
                            // on another thread and we get to this point before the article property
                            // has been updated on it - 22/05/20
//                    if(viewModel.player_article.value == null || viewModel.player_article.value!!.id == article.id)
                            if(!isCancellationRequested(article.id))
                            {
                                if(error != null)
                                {
                                    isLoadingArticle = false

                                    completion(null, error)
                                }
                                else if(admission != null)
                                {
//                            getPlaybackURL(article, admission)
//                            {
//                                url, error ->

                                    // [dho] check we still care about hearing this article, ie. the user
                                    // has not gone on to select a different one whilst the request was in flight - 22/05/20
//                                if(!isCancellationRequested(admission.articleID))
//                                {
//                                    if(error != null)
//                                    {
//                                        loadingArticle = false
//                                        pause()
//                                        completion(null, error)
//                                    }
//                                    else
//                                    {
//                                    waitUntilPlayable(admission.signedURL, admission.contentType) {
//                                            playable ->

                                        //                                            if(viewModel.player_article.value!!.id == admission.articleID)
//                                        if(!isCancellationRequested(admission.articleID))
////                                        {
//                                            if(playable)
//                                            {
                                                loadURL(article, contentURL = admission.signedURL, contentType = admission.contentType, isRemote = true)
                                                {
                                                        err ->
                                                    if(err != null)
                                                    {
                                                        completion(null, err)
                                                    }
                                                    else
                                                    {
                                                        completion(admission.lastAccessed, null)
                                                    }
                                                }

                                                // [dho] download file for offline - 23/06/20
                                                if(useCache)
                                                {
                                                    val title = article.details.title ?: "Article ${article.id}"
                                                    val cachedPath = localCache.cachedPerformanceFilePath(article.id)
                                                    network.downloadFile(
                                                        title,
                                                        "Saving article for offline playback",
                                                        admission.signedURL,
                                                        cachedPath
                                                    )
                                                    {
                                                        err ->
                                                        if(err == null) {
                                                            val utc = DateTimeUtils.nowUTC()
                                                            ThreadUtils.bgThreadExec {
                                                                localCache.updateLocalFileLastAccessUTC(
                                                                    cachedPath,
                                                                    utc
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
//                                            }
//                                            else
//                                            {
//                                                isLoadingArticle = false
//                                                completion(null, Exception("Could not play audio"))
//                                            }
//                                        }
//                                    }
//                                    }
//                                }
//                            }
                                }
                                else
                                {
                                    isLoadingArticle = false

                                    completion(null, Exception("Could not retrieve performance"))
                                }
                            }
                        }
                    }
                }
            }




        }
    }



    private fun loadURL(article : DATA_Article, contentURL : String, contentType : String, isRemote : Boolean, completion: (error : Exception?) -> Unit)
    {
        val mediaID = article.id
        val mediaContentURL = contentURL;
        val mediaContentType = contentType;
        val mediaTitle = article.details.title ?: StringUtils.ellipsize(article.source.canonicalURL ?: article.source.contentURL, DetailsLinkMaxCharCount) ?: ""
        val mediaArtist = article.details.author?.name ?: "Unknown Author"
        val mediaAlbumArtist = article.details.organization?.name ?: ""
        val mediaDate = article.details.modified ?: article.details.published ?: article.meta.creationUTC ?: null;
        val mediaArtworkURL = article.artwork.largeArtwork ?: article.artwork.standardArtwork ?: null

        val mediaDescriptor = MediaDescriptor(
            id = mediaID,
            title = mediaTitle,
            artist = mediaArtist,
            artworkURL = mediaArtworkURL,
            contentURL = mediaContentURL,
            contentType = mediaContentType,
            isRemote = isRemote
        )

        val idx = 0

        val wupMaxAttempts = viewModel.user_config.value?.accessPerformance_MaxAttempts ?: LocalMediaPlayerService.DEFAULT_WUP_MAX_ATTEMPTS
        val wupIntervalSeconds = viewModel.user_config.value?.accessPerformance_PollInterval_Seconds ?: LocalMediaPlayerService.DEFAULT_WUP_INTERVAL_SECONDS

        val wup = WaitUntilPlayableParams(
            maxAttempts = wupMaxAttempts,
            intervalSeconds = wupIntervalSeconds
        )

        // used to check whether this was the instance loaded in the callback - 12/06/20
        val params = MediaPrepareParams(
            items = listOf(mediaDescriptor),
            wup = wup
        )

        latestMediaPrepareParams = params;

        sendMediaPrepareRequest(params)
        {
            err ->

                if(latestMediaPrepareParams?.id == params.id)
                {
                    isLoadingArticle = false
//                    mediaController?.let {
//                        it.skipToPlaylistItem(idx)
//                        it.prepare()
//                    }
                }

                completion(err)
        };
    }

    private fun sendMediaPrepareRequest(params: MediaPrepareParams, completion: (error : Exception?) -> Unit)
    {
        val json = Gson().toJson(params)
        val intent = Intent(LocalMediaPlayerService.SET_PLAYLIST_INTENT)
        intent.putExtra(LocalMediaPlayerService.SET_PLAYLIST_KEY, json)

        // [dho] "Cannot invoke observe on a background thread" - 24/06/20
        ThreadUtils.ensureMainThreadExec {
            val observerToRemove = mediaPrepareResultObserver;
            mediaPrepareResultObserver = null

            if(observerToRemove !== null)
            {
                PlayerDataBridge.latestResult.removeObserver(observerToRemove)
            }


            mediaPrepareResultObserver?.let { PlayerDataBridge.latestResult.removeObserver(it) }

            val newObserver = Observer<MediaPrepareResult> {
                // [dho] check message is meant for us - 12/06/20
                if(it.params?.id == params.id)
                {
                    val observerToRemove = mediaPrepareResultObserver;
                    mediaPrepareResultObserver = null

                    if(observerToRemove !== null)
                    {
                        PlayerDataBridge.latestResult.removeObserver(observerToRemove)
                    }

                    ThreadUtils.ensureBGThreadExec { completion(it.err) }

                }
            }

            mediaPrepareResultObserver = newObserver

            PlayerDataBridge.latestResult.observe(activity, newObserver);

            activity.sendBroadcast(intent)
        }
    }

    private fun updateObservers(status : MediaPlayerStatus = currentStatus)
    {
        if(status.state != PlayerDataBridge.state.value)
        {
            PlayerDataBridge.state.postValue(status.state)
        }

        if(status.duration != PlayerDataBridge.duration.value)
        {
            PlayerDataBridge.duration.postValue(status.duration)
        }

        if(status.progress != PlayerDataBridge.progress.value)
        {
            PlayerDataBridge.progress.postValue(status.progress)
        }

        if(status.buffState != PlayerDataBridge.buffState.value)
        {
            PlayerDataBridge.buffState.postValue(status.buffState)
        }

        if(status.buffProgress != PlayerDataBridge.buffProgress.value)
        {
            PlayerDataBridge.buffProgress.postValue(status.buffProgress)
        }
    }


    // [dho] removing this because it was ported from iOS where the player has a concept
    // of 'waiting to play' which means it is collecting enough data to start - 17/06/20
//    fun isLoadingOrWaitingToPlay() : Boolean
//    {
//        var state = currentStatus.state
//        val itemWaiting = mediaController?.currentMediaItem !== null
//
//        return isLoadingArticle || (itemWaiting && state != LocalMediaPlayerService.STATE_playing)
//    }
    fun isLastLoadedArticleID(articleID : String) : Boolean
    {
        val mediaID = mediaController?.currentMediaItem?.metadata?.getString(MediaMetadata.METADATA_KEY_MEDIA_ID)

        return mediaID == articleID
    }

    private fun startTrackingProgress() {
        trackProgressPoll?.cancel?.invoke()

        trackProgressPoll = setTimeout({

//            Log.i("TRACKING PROGRESS", Gson().toJson(currentStatus));

            updateObservers();

            trackProgressPoll = null

            startTrackingProgress()

        }, 1000)
    }

    private fun stopTrackingProgress()
    {
        trackProgressPoll?.cancel?.invoke()
        trackProgressPoll = null
    }


    fun play(completion: (error : Exception?) -> Unit) {

        // [dho] TODO dynamic index when playlists are longer than 1 item - 01/07/20
        val idx = 0

        mediaController?.let {
            if(it.currentMediaItemIndex == idx)
            {
                when(it.playerState)
                {
                    SessionPlayer.PLAYER_STATE_PLAYING -> {}
                    else -> it.play()
                }
            }
            else
            {
                it.skipToPlaylistItem(idx)
                it.prepare()
                it.play()
            }

//            startTrackingProgress()
        }

        completion(null)
    }

    fun pause(completion: (error : Exception?) -> Unit) {
        mediaController?.let {
            when(it.playerState)
            {
                SessionPlayer.PLAYER_STATE_PLAYING -> it.pause()
                else -> {}
            }
        }

//        stopTrackingProgress()

        completion(null)
    }


    fun skipBackward(completion: (error : Exception?) -> Unit)
    {
        val newProgress = currentStatus.progress - LocalMediaPlayerService.SEEK_DELTA_SECS

        changeTime(clamp(newProgress, 0.0f, Float.MAX_VALUE), completion)
    }

    fun skipForward(completion: (error : Exception?) -> Unit)
    {
        val status = currentStatus
        val newProgress = status.progress + LocalMediaPlayerService.SEEK_DELTA_SECS
        val duration = status.duration

        changeTime(if(newProgress > duration) duration else newProgress, completion)
    }

    fun changeTime(progress : Float, completion: (error : Exception?) -> Unit)
    {
        val mc = mediaController

        if(mc != null)
        {
            runFutureAndParseResult(mc.seekTo(clamp(progress * 1000, 0F, Float.MAX_VALUE).toLong()), completion)
        }
        else
        {
            completion(null)
        }
    }

    fun clear()
    {
        PlayerDataBridge.currentMediaID.postValue(null)
        viewModel.player_article.postValue(null)

        pause {
            err -> if(err == null) {
                mediaController?.setPlaylist(listOf(), null)
            }
        }


//        service.reset()

//        try
//        {
//            if(mediaPlayer?.isPlaying == true)
//            {
//                mediaPlayer?.stop()
//            }
//
//            mediaPlayer?.release()
//            mediaPlayer = null
//        }
//        catch(e : Exception)
//        {
//
//        }
//
//        viewModel.player_progress.postValue(0.0f)
//        viewModel.player_article.postValue(null)
//        viewModel.player_duration.postValue(0.0f)
    }


    //////


    private fun parseState(@SessionPlayer.PlayerState state: Int) = when(state)
    {
        SessionPlayer.PLAYER_STATE_PLAYING -> {
            LocalMediaPlayerService.STATE_playing
        }
        SessionPlayer.PLAYER_STATE_PAUSED -> LocalMediaPlayerService.STATE_paused
//            SessionPlayer.PLAYER_STATE_IDLE -> LocalMediaPlayerService.STATE_stopped
//            SessionPlayer.PLAYER_STATE_ERROR -> LocalMediaPlayerService.STATE_stopped
        else -> LocalMediaPlayerService.STATE_stopped
    }

    // [dho] NOTE will be some max negative number when initializing - 20/06/20
    private fun parseProgress(position : Long) = clamp(position/1000.0F, 0.0F, Float.MAX_VALUE)

    private fun parseDuration(duration : Long) = clamp(duration/1000.0F, 0.0F, Float.MAX_VALUE)


    private fun runFutureAndParseResult(future : ListenableFuture<SessionResult>, completion : (err : Exception?) -> Unit)
    {
        future.addListener({
            if(future.isDone)
            {
                try {
                    completion(when(future.get().resultCode)
                    {
                        SessionResult.RESULT_SUCCESS -> null
                        SessionResult.RESULT_ERROR_UNKNOWN -> Exception("An unknown error occurred")
                        SessionResult.RESULT_ERROR_INVALID_STATE -> Exception("The media player is in an invalid state")
                        SessionResult.RESULT_ERROR_BAD_VALUE -> Exception("Bad value passed to media player")
                        SessionResult.RESULT_ERROR_PERMISSION_DENIED -> Exception("Permission denied to media player")
                        SessionResult.RESULT_ERROR_IO -> Exception("An error occurred loading the data")
                        SessionResult.RESULT_ERROR_SESSION_DISCONNECTED -> Exception("The media session was disconnected")
                        SessionResult.RESULT_ERROR_NOT_SUPPORTED -> Exception("The media player could not play the data")
                        SessionResult.RESULT_ERROR_SESSION_AUTHENTICATION_EXPIRED -> Exception("The media session authentication expired")
                        SessionResult.RESULT_ERROR_SESSION_PREMIUM_ACCOUNT_REQUIRED -> Exception("Premium account required")
                        SessionResult.RESULT_ERROR_SESSION_CONCURRENT_STREAM_LIMIT -> Exception("Concurrent stream limit reached")
                        SessionResult.RESULT_ERROR_SESSION_PARENTAL_CONTROL_RESTRICTED -> Exception("The selected media cannot be played due to parental controls")
                        SessionResult.RESULT_ERROR_SESSION_NOT_AVAILABLE_IN_REGION -> Exception("The selected media is not available in your region")
                        SessionResult.RESULT_ERROR_SESSION_SKIP_LIMIT_REACHED -> Exception("Skip limit reached")
                        SessionResult.RESULT_ERROR_SESSION_SETUP_REQUIRED -> Exception("Additional setup required")
//                                BaseResult.RESULT_INFO_SKIPPED -> PrepareResult(params, Exception("The request was cancelled"))
                        else -> null
                    })

                }
                catch(err : Exception)
                {
                    completion(err)
                }
            }

        }, { command -> command.run() })
    }
    /**
     * Called when the controller is successfully connected to the session. The controller
     * becomes available afterwards.
     *
     * @param controller the controller for this event
     * @param allowedCommands commands that's allowed by the session.
     */
    override fun onConnected(
        controller: MediaController,
        allowedCommands: SessionCommandGroup
    ) {
        val cb = mediaControllerConnectionCallback;
        mediaControllerConnectionCallback = null;
        updateObservers()
        cb?.invoke(null);
    }

    /**
     * Called when the session refuses the controller or the controller is disconnected from
     * the session. The controller becomes unavailable afterwards and the callback wouldn't
     * be called.
     *
     *
     * It will be also called after the [.close], so you can put clean up code here.
     * You don't need to call [.close] after this.
     *
     * @param controller the controller for this event
     */
    override fun onDisconnected(controller: MediaController) {
//        stopTrackingProgress()

        val cb = mediaControllerConnectionCallback;
        mediaControllerConnectionCallback = null;
        updateObservers()
        cb?.invoke(Exception("Could not connect to the the media player service"));
    }

    /**
     * Called when the session set the custom layout through the
     * [MediaSession.setCustomLayout].
     *
     *
     * Can be called before [.onConnected]
     * is called.
     *
     *
     * Default implementation returns [SessionResult.RESULT_ERROR_NOT_SUPPORTED].
     *
     * @param controller the controller for this event
     * @param layout
     */
    @SessionResult.ResultCode
    override fun onSetCustomLayout(
        controller: MediaController,
        layout: List<MediaSession.CommandButton?>
    ): Int {
        return SessionResult.RESULT_ERROR_NOT_SUPPORTED
    }

    /**
     * Called when the session has changed anything related with the [PlaybackInfo].
     *
     *
     * Interoperability: When connected to
     * [android.support.v4.media.session.MediaSessionCompat], this may be called when the
     * session changes playback info by calling
     * [android.support.v4.media.session.MediaSessionCompat.setPlaybackToLocal] or
     * [android.support.v4.media.session.MediaSessionCompat.setPlaybackToRemote]}. Specifically:
     *
     *  *  Prior to API 21, this will always be called whenever any of those two methods is
     * called.
     *  *  From API 21 to 22, this is called only when the playback type is changed from local
     * to remote (i.e. not from remote to local).
     *  *  From API 23, this is called only when the playback type is changed.
     *
     *
     * @param controller the controller for this event
     * @param info new playback info
     */
    override fun onPlaybackInfoChanged(
        controller: MediaController,
        info: MediaController.PlaybackInfo
    ) {

    }

    /**
     * Called when the allowed commands are changed by session.
     *
     * @param controller the controller for this event
     * @param commands newly allowed commands
     */
    override fun onAllowedCommandsChanged(
        controller: MediaController,
        commands: SessionCommandGroup
    ) {

    }

//    /**
//     * Called when the session sent a custom command. Returns a [SessionResult] for
//     * session to get notification back. If the `null` is returned,
//     * [SessionResult.RESULT_ERROR_UNKNOWN] will be returned.
//     *
//     *
//     * Default implementation returns [SessionResult.RESULT_ERROR_NOT_SUPPORTED].
//     *
//     * @param controller the controller for this event
//     * @param command
//     * @param args
//     * @return result of handling custom command
//     */
//    override fun onCustomCommand(
//        controller: MediaController,
//        command: SessionCommand, args: Bundle?
//    ): SessionResult {
//        return SessionResult(SessionResult.RESULT_ERROR_NOT_SUPPORTED)
//    }

    /**
     * Called when the player state is changed.
     *
     * @param controller the controller for this event
     * @param state the new player state
     */
    override fun onPlayerStateChanged(
        controller: MediaController,
        @SessionPlayer.PlayerState state: Int
    ) {
        if(state == SessionPlayer.PLAYER_STATE_PLAYING)
        {
            isLoadingArticle = false
            startTrackingProgress()
        }
        else
        {
            if(state == SessionPlayer.PLAYER_STATE_ERROR)
            {
                isLoadingArticle = false
            }

            stopTrackingProgress()
        }

        updateObservers();
    }

    /**
     * Called when playback speed is changed.
     *
     * @param controller the controller for this event
     * @param speed speed
     */
    override fun onPlaybackSpeedChanged(
        controller: MediaController,
        speed: Float
    ) {

    }

    /**
     * Called to report buffering events for a media item.
     *
     *
     * Use [.getBufferedPosition] for current buffering position.
     *
     * @param controller the controller for this event
     * @param item the media item for which buffering is happening.
     * @param state the new buffering state.
     */
    override fun onBufferingStateChanged(
        controller: MediaController,
        item: MediaItem, @SessionPlayer.BuffState state: Int
    ) {
        updateObservers()
    }

    /**
     * Called to indicate that seeking is completed.
     *
     * @param controller the controller for this event.
     * @param position the previous seeking request.
     */
    override fun onSeekCompleted(
        controller: MediaController,
        position: Long
    ) {
        updateObservers();
    }

    /**
     * Called when the current item is changed. It's also called after
     * [.setPlaylist] or [.setMediaItem].
     * Also called when [MediaItem.setMetadata] is called on the current
     * media item.
     *
     *
     * When it's called, you should invalidate previous playback information and wait for later
     * callbacks. Also, current, previous, and next media item indices may need to be updated.
     *
     * @param controller the controller for this event
     * @param item new current media item
     * @see .getPlaylist
     * @see .getPlaylistMetadata
     */
    override fun onCurrentMediaItemChanged(
        controller: MediaController,
        item: MediaItem?
    ) {
//        val metadata = item?.metadata

//        if(metadata !== null)
//        {
//            with(metadata)
//            {
//                val title = getString(MediaMetadata.METADATA_KEY_DISPLAY_TITLE)
//                val artist = getString(MediaMetadata.METADATA_KEY_ARTIST)
//                val album = getString(MediaMetadata.METADATA_KEY_ALBUM)
//                val mediaURI = getString(MediaMetadata.METADATA_KEY_MEDIA_URI)
//                val duration = getLong(MediaMetadata.METADATA_KEY_DURATION)
//
//                // [dho] convert ms to seconds - 12/06/20
//                val newDuration = (duration / 1000).toFloat()
//
//                val newStatus = MediaPlayerStatus(
//                    state = LocalMediaPlayerService.STATE_stopped,
//                    duration = newDuration,
//                    progress = 0.0f
//                )
//
//                PlayerDataBridge.status.postValue(newStatus)
//            }

//        }

        updateObservers();
    }

    /**
     * Called when a playlist is changed. It's also called after [.setPlaylist] or
     * [.setMediaItem].
     * Also called when [MediaItem.setMetadata] is called on a media item
     * that is contained in the current playlist.
     *
     *
     * When it's called, current, previous, and next media item indices may need to be updated.
     *
     * @param controller the controller for this event
     * @param list new playlist
     * @param metadata new metadata
     * @see .getPlaylist
     * @see .getPlaylistMetadata
     */
    override fun onPlaylistChanged(
        controller: MediaController,
        list: List<MediaItem?>?,
        metadata: MediaMetadata?
    ) {

    }

    /**
     * Called when a playlist metadata is changed.
     *
     * @param controller the controller for this event
     * @param metadata new metadata
     */
    override fun onPlaylistMetadataChanged(
        controller: MediaController,
        metadata: MediaMetadata?
    ) {

    }

    /**
     * Called when the shuffle mode is changed.
     *
     * @param controller the controller for this event
     * @param shuffleMode repeat mode
     * @see SessionPlayer.SHUFFLE_MODE_NONE
     *
     * @see SessionPlayer.SHUFFLE_MODE_ALL
     *
     * @see SessionPlayer.SHUFFLE_MODE_GROUP
     */
    override fun onShuffleModeChanged(
        controller: MediaController,
        @SessionPlayer.ShuffleMode shuffleMode: Int
    ) {

    }

    /**
     * Called when the repeat mode is changed.
     *
     * @param controller the controller for this event
     * @param repeatMode repeat mode
     * @see SessionPlayer.REPEAT_MODE_NONE
     *
     * @see SessionPlayer.REPEAT_MODE_ONE
     *
     * @see SessionPlayer.REPEAT_MODE_ALL
     *
     * @see SessionPlayer.REPEAT_MODE_GROUP
     */
    override fun onRepeatModeChanged(
        controller: MediaController,
        @SessionPlayer.RepeatMode repeatMode: Int
    ) {

    }

    /**
     * Called when the playback is completed.
     *
     * @param controller the controller for this event
     */
    override fun onPlaybackCompleted(controller: MediaController) {
        updateObservers();
    }
}