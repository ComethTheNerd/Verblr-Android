package com.quantumcommune.verblr

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.KeyEvent
import androidx.core.content.ContextCompat
import androidx.media.AudioAttributesCompat
import androidx.media2.common.*
import androidx.media2.common.SessionPlayer.BuffState
import androidx.media2.common.SessionPlayer.PlayerState
import androidx.media2.player.MediaPlayer
import androidx.media2.session.MediaLibraryService
import androidx.media2.session.MediaSession
import androidx.media2.session.SessionCommand
import androidx.media2.session.SessionCommandGroup
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.quantumcommune.verblr.ui.main.ViewUtils
import java.io.File

class LocalMediaPlayerService() : MediaLibraryService()//, MediaPlayer.OnPreparedListener, MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener, MediaPlayer.OnBufferingUpdateListener {
{
    companion object {
        //        val shared = MediaPlayerService()
//
        const val STATE_playing = 1;
        const val STATE_loading = 3;
        const val STATE_paused = 2;
        const val STATE_stopped = -1;
        const val STATE_default = STATE_stopped

        const val SET_PLAYLIST_INTENT = "media.player.set.playlist"
        const val SET_PLAYLIST_KEY = "set.playlist"

        const val SEEK_DELTA_MS = 10000
        const val SEEK_DELTA_SECS = SEEK_DELTA_MS / 1000

        val ARTWORK_BITMAP_DIM = ViewUtils.toXScaledPX(320F)

        // [dho] WUP == 'wait until playable' - 13/06/20
        const val DEFAULT_WUP_MAX_ATTEMPTS = 7
        const val DEFAULT_WUP_INTERVAL_SECONDS = 1.4f
        const val DEFAULT_CONTENT_TYPE = "audio/wav";
    }

    private val intentFilter = IntentFilter(SET_PLAYLIST_INTENT)

    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var mediaSession: MediaLibrarySession

    //    private lateinit var controllerInfo: MediaSession.ControllerInfo
    private lateinit var network: Network

    //    private var sessionId = ""
    private var broadcastReceiver: LocalMediaPlayerServiceBroadcastReceiver? = null
    private var pendingFuture: ListenableFuture<SessionPlayer.PlayerResult>? = null
    private var latestRequestParams: MediaPrepareParams? = null
    private var waitUntilPlayablePoll: TimeoutTask? = null


    override fun onCreate() {
        super.onCreate()

        mediaPlayer = createPlayer()
        mediaSession = createMediaSession(mediaPlayer)
//        notifications = Notifications(this)
        broadcastReceiver = LocalMediaPlayerServiceBroadcastReceiver()
        registerReceiver(broadcastReceiver, intentFilter)

        network = Network(this)
    }

    inner class LocalMediaPlayerServiceBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {
            val json = intent?.getStringExtra(LocalMediaPlayerService.SET_PLAYLIST_KEY) ?: return

            // [dho] offload from main thread to process - 28/06/20
            ThreadUtils.ensureBGThreadExec { handleRequest(json) }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? {
        // [dho] based on membership? - 12/06/20
        val allowedCommands = SessionCommandGroup.Builder()
            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_PLAY))
            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_PAUSE))
//            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_SKIP_TO_NEXT_PLAYLIST_ITEM))
//            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_PLAYER_SKIP_TO_PREVIOUS_PLAYLIST_ITEM))
            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_SESSION_SKIP_FORWARD))
            .addCommand(SessionCommand(SessionCommand.COMMAND_CODE_SESSION_SKIP_BACKWARD))
            .build()

        mediaSession.setAllowedCommands(controllerInfo, allowedCommands)

        return mediaSession
    }

    inner class SessionServiceCallback : MediaLibrarySession.MediaLibrarySessionCallback() {
        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): SessionCommandGroup? {
//            sessionId = mediaSession.id
            return super.onConnect(session, controller)
        }

    }

    override fun onDestroy() {
        super.onDestroy() // Cancel the persistent notification.
//        notifications?.notificationManager?.cancel(NOTIFICATION_id)

        mediaPlayer.close()

        unregisterReceiver(broadcastReceiver)

        // Tell the user we stopped.
//        Toast.makeText(this, R.string.local_service_stopped, Toast.LENGTH_SHORT).show()
    }


    private fun createMediaSession(mp: MediaPlayer): MediaLibrarySession {

        val intent = Intent(this, MainActivity::class.java)

        // [dho] this intent will be passed to `MainActivity` when the user taps the media
        // notification from the system tray... so we nee to put anything in the extras that will
        // tell the activity how to set the UI up to reflect we are partway through playback, not initial
        // launch - 12/06/20
        intent.putExtra("test", 1235)


        val ms = MediaLibrarySession.Builder(
            this,
            mp,
            ContextCompat.getMainExecutor(this),
            SessionServiceCallback()
        )
            .setSessionActivity(
                PendingIntent.getActivity(
                    this,
                    0,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
            // [dho] fix for "java.lang.IllegalStateException: Session ID must be unique" error - 24/06/20
            .setId(uuid())
            .build()


        return ms
    }

    private fun createPlayer(): MediaPlayer {
        val mp = MediaPlayer(this)
        mp.registerPlayerCallback({ command -> command.run() }, MediaPlayerCallback());
        initializePlayer(mp);

        return mp;
    }

    private fun initializePlayer(mp: MediaPlayer) {
        mp.reset()
        mp.setAudioAttributes(
            AudioAttributesCompat
                .Builder()
                .setUsage(AudioAttributesCompat.USAGE_MEDIA)
                .setContentType(AudioAttributesCompat.CONTENT_TYPE_MUSIC)
                .setLegacyStreamType(AudioManager.STREAM_MUSIC)
                .build()
        )

//        mediaPlayer.playerVolume = 1.0f

//        mediaPlayer.prepare()
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // [dho] adapted from : https://developer.android.com/reference/androidx/media/session/MediaButtonReceiver - 12/06/20
//    MediaButtonReceiver.handleIntent(mediaSession.getSessionCompat(), intent);
//    return super.onStartCommand(intent, flags, startId);

        intent?.let {
            when {
                KeyEventHelper.isEvent(intent, KeyEvent.KEYCODE_MEDIA_PLAY) -> {
                    mediaPlayer.play()
                }

                KeyEventHelper.isEvent(intent, KeyEvent.KEYCODE_MEDIA_PAUSE) -> {
                    mediaPlayer.pause()
                }

                KeyEventHelper.isEvent(intent, KeyEvent.KEYCODE_MEDIA_SKIP_BACKWARD) -> {
                    val position = mediaPlayer.currentPosition - SEEK_DELTA_MS

                    mediaPlayer.seekTo(position)
                }

                KeyEventHelper.isEvent(intent, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD) -> {
                    val position = mediaPlayer.currentPosition + SEEK_DELTA_MS

                    mediaPlayer.seekTo(position)
                }


//                KeyEventHelper.isEvent(intent, KeyEvent.KEYCODE_MEDIA_SKIP_FORWARD) -> {
//                    mediaPlayer.pause()
//                    mediaPlayer.skipToNextPlaylistItem()
//                    mediaPlayer.play()
//                }
//
//                KeyEventHelper.isEvent(intent) -> {
//                    mediaPlayer.pause()
//                    mediaPlayer.skipToPreviousPlaylistItem()
//                    mediaPlayer.play()
//                }

                else -> {

                }
            }
        }

        return super.onStartCommand(intent, flags, startId)
    }



    fun handleRequest(json: String) {
        val params = try {
            Gson().fromJson(json, MediaPrepareParams::class.java)
        } catch (err: Exception) {
            Log.e(
                "LocalMediaPlayerService",
                "Could not parse JSON payload (error : ${err.localizedMessage})"
            )

            PlayerDataBridge.latestResult.postValue(MediaPrepareResult(null, err))
            return
        }

        val playlist = try {
            params.items.map { if (it.isRemote) toUriMediaItem(it) else toFileMediaItem(it) }
        } catch (err: Exception) {
            PlayerDataBridge.latestResult.postValue(MediaPrepareResult(params, err))
            return
        }

        latestRequestParams = params

        if (playlist.isNotEmpty()) {
            val idx = 0

            waitUntilResourceIsPlayable(params, idx) { playable, err ->
                if (latestRequestParams?.id == params.id) {
                    PlayerDataBridge.latestResult.postValue(
                        MediaPrepareResult(
                            params,
                            err ?: (
                                    if (playable == true) {
                                        try {
                                            mediaPlayer.setPlaylist(playlist, null)
                                            mediaPlayer.prepare()
                                            null
                                        } catch (err: Exception) {
                                            err
                                        }
                                    } else {
                                        Exception("Could not play audio")
                                    }
                                    )
                        )
                    )
                }
            }
        } else {
            PlayerDataBridge.latestResult.postValue(MediaPrepareResult(params, null))
        }
    }

    private fun waitUntilResourceIsPlayable(
        params: MediaPrepareParams,
        idx: Int,
        completion: (playable: Boolean?, err: Exception?) -> Unit
    ) {
        val descriptor = params.items[idx];

        if (descriptor.isRemote) {
            if (!StringUtils.isValidURL(descriptor.contentURL)) {
                completion(null, Exception("Content URL is invalid : '${descriptor.contentURL}'"));
                return
            }

            waitUntilRemoteResourceIsPlayable(params, descriptor, attempt = 0)
            { playable, err ->

                // [dho] does the user still care about this request - 13/06/20
                if (latestRequestParams?.id == params.id) {
                    completion(playable, err)
                }
            }
        } else {
            val playable = File(descriptor.contentURL).exists()

            completion(playable, if (playable) null else Exception("File not found"))
        }
    }

    private fun waitUntilRemoteResourceIsPlayable(
        params: MediaPrepareParams,
        descriptor: MediaDescriptor,
        attempt: Int = 0,
        completion: (playable: Boolean?, err: Exception?) -> Unit
    ) {
        if (latestRequestParams?.id != params.id) {
            completion(null, Exception("Request superseded"))
            return
        }

        waitUntilPlayablePoll?.cancel?.invoke()
        waitUntilPlayablePoll = null

        val wup = params.wup

        if (attempt >= wup.maxAttempts) {
            Log.e("waitUntilPlayable", "Max attempts exceeded : ${descriptor.contentURL}")
            completion(
                null,
                Exception("Maximum number of attempts exceeded trying to retrieve audio")
            )
            return
        }

//        val fileExtension = viewModel.user_config.value?.performance_FileExtension ?: DefaultPerformanceFileExtension
//        val contentType = "audio/$fileExtension"

        network.isPlayableURL(descriptor.contentURL, descriptor.contentType)
        { playable, err ->

            if (err != null && (attempt + 1) >= wup.maxAttempts) {
                completion(playable, err)
            } else if (playable == true) {
                Log.d(
                    "waitUntilPlayable",
                    "URL is playable after ${attempt + 1} attempt(s) : ${descriptor.contentURL}"
                )
                completion(true, null)
            } else {
                Log.d(
                    "waitUntilPlayable",
                    "Schedule recheck for playable URL : ${descriptor.contentURL}"
                )

                val delayMS = (wup.intervalSeconds * 1000).toLong()

                waitUntilPlayablePoll = setTimeout({
                    waitUntilRemoteResourceIsPlayable(params, descriptor, attempt + 1, completion)
                }, delayMS)
            }
        }
    }

    fun toFileMediaItem(m: MediaDescriptor) = FileMediaItem.Builder(
        ParcelFileDescriptor.open(
            File(m.contentURL),
            ParcelFileDescriptor.MODE_READ_ONLY
        )
    )
        .setMetadata(toMediaItemMetadata(m))
        .build()

    fun toUriMediaItem(m: MediaDescriptor) = UriMediaItem.Builder(Uri.parse(m.contentURL))
        .setMetadata(toMediaItemMetadata(m))
        .build()

    fun toMediaItemMetadata(m: MediaDescriptor): MediaMetadata {
        val metadata = MediaMetadata.Builder()
            .putText(MediaMetadata.METADATA_KEY_MEDIA_ID, m.id)
            .putText(MediaMetadata.METADATA_KEY_DISPLAY_TITLE, m.title)
            .putText(MediaMetadata.METADATA_KEY_ARTIST, m.artist)
//                    .putText(MediaMetadata.METADATA_KEY_ALBUM, m.metadata!!.getString(MediaMetadata.METADATA_KEY_ALBUM))
            .putText(MediaMetadata.METADATA_KEY_MEDIA_URI, m.contentURL);

        toArtworkBitmap(m)?.let { bmp ->
            metadata.putBitmap(MediaMetadata.METADATA_KEY_ART, bmp)
            metadata.putBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART, bmp)
            metadata.putBitmap(MediaMetadata.METADATA_KEY_DISPLAY_ICON, bmp)

            toArtworkFile(m, bmp)?.let { file ->
                val path = Uri.parse("content://${file.absolutePath}").toString()
                metadata.putString(MediaMetadata.METADATA_KEY_ART_URI, path)
                metadata.putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, path)
                metadata.putString(MediaMetadata.METADATA_KEY_DISPLAY_ICON_URI, path)
            }
        }

        return metadata.build()
    }

    fun toArtworkBitmap(m: MediaDescriptor): Bitmap? {
        if (m.artworkURL != null) {
            try {
                return ImageUtils.squareCrop(m.artworkURL, ARTWORK_BITMAP_DIM);
            } catch (err: Exception) {
                Log.e("artwork bitmap error", err.localizedMessage)
                Log.e("artwork bitmap error", err.stackTrace.toString())
            }
        }

        // [dho] fallback if we did not have artwork to load or it failed for some reason - 28/06/20
        return try {
            return BitmapFactory.decodeResource(resources, R.drawable.blank_article)
        } catch (err: Exception) {
            Log.e("toArtworkBitmap", err.localizedMessage)
            Log.e("toArtworkBitmap", err.stackTrace.toString())

            null
        }
    }

    fun toArtworkFile(m: MediaDescriptor, artwork: Bitmap): File? = try {
        val filename = "${cacheDir.absolutePath}/notification/artwork/${m.id}.bmp"

        ImageUtils.writeBitmapToPNG(filename, artwork, overwrite = false)
    } catch (err: Exception) {
        Log.e("toArtworkFile", err.localizedMessage)
        Log.e("toArtworkFile", err.stackTrace.toString())

        null
    }

    fun runFutureAndParseResult(
        future: ListenableFuture<SessionPlayer.PlayerResult>,
        completion: (err: Exception?) -> Unit
    ) {
        future.addListener({
            if (future.isDone) {
                try {
                    completion(
                        when (future.get().resultCode) {
                            SessionPlayer.PlayerResult.RESULT_SUCCESS -> null
                            SessionPlayer.PlayerResult.RESULT_ERROR_UNKNOWN -> Exception("An unknown error occurred")
                            SessionPlayer.PlayerResult.RESULT_ERROR_INVALID_STATE -> Exception("The media player is in an invalid state")
                            SessionPlayer.PlayerResult.RESULT_ERROR_BAD_VALUE -> Exception("Bad value passed to media player")
                            SessionPlayer.PlayerResult.RESULT_ERROR_PERMISSION_DENIED -> Exception("Permission denied to media player")
                            SessionPlayer.PlayerResult.RESULT_ERROR_IO -> Exception("An error occurred loading the data")
                            SessionPlayer.PlayerResult.RESULT_ERROR_NOT_SUPPORTED -> Exception("The media player could not play the data")
//                                BaseResult.RESULT_INFO_SKIPPED -> PrepareResult(params, Exception("The request was cancelled"))
                            else -> null
                        }
                    )

                } catch (err: Exception) {
                    completion(err)
                }
            }

        }, { command -> command.run() })
    }

    inner class MediaPlayerCallback : SessionPlayer.PlayerCallback() {
        /**
         * Called when the state of the player has changed.
         *
         * @param player the player whose state has changed.
         * @param playerState the new state of the player.
         * @see .getPlayerState
         */
        override fun onPlayerStateChanged(
            player: SessionPlayer,
            @PlayerState playerState: Int
        ) {
//            latestRequestParams?.
            
        }

        /**
         * Called when a buffering events for a media item happened.
         *
         * @param player the player that is buffering
         * @param item the media item for which buffering is happening.
         * @param buffState the new buffering state.
         * @see .getBufferingState
         */
        override fun onBufferingStateChanged(
            player: SessionPlayer,
            item: MediaItem?, @BuffState buffState: Int
        ) {
            
        }

        /**
         * Called when the playback speed has changed.
         *
         * @param player the player that has changed the playback speed.
         * @param playbackSpeed the new playback speed.
         * @see .getPlaybackSpeed
         */
        override fun onPlaybackSpeedChanged(
            player: SessionPlayer,
            playbackSpeed: Float
        ) {
            
        }

        /**
         * Called when [.seekTo] is completed.
         *
         * @param player the player that has completed seeking.
         * @param position the previous seeking request.
         * @see .getCurrentPosition
         */
        override fun onSeekCompleted(
            player: SessionPlayer,
            position: Long
        ) {
            
        }

        /**
         * Called when a playlist is changed. It's also called after [.setPlaylist] or
         * [.setMediaItem].
         *
         * @param player the player that has changed the playlist and playlist metadata.
         * @param list new playlist
         * @param metadata new metadata
         * @see .getPlaylist
         * @see .getPlaylistMetadata
         */
        override fun onPlaylistChanged(
            player: SessionPlayer,
            list: List<MediaItem?>?,
            metadata: MediaMetadata?
        ) {
            
        }

        /**
         * Called when a playlist metadata is changed.
         *
         * @param player the player that has changed the playlist metadata.
         * @param metadata new metadata
         * @see .getPlaylistMetadata
         */
        override fun onPlaylistMetadataChanged(
            player: SessionPlayer,
            metadata: MediaMetadata?
        ) {
            
        }

        /**
         * Called when the shuffle mode is changed.
         *
         *
         * [SessionPlayer.getPreviousMediaItemIndex] and
         * [SessionPlayer.getNextMediaItemIndex] values can be outdated when this callback
         * is called if the current media item is the first or last item in the playlist.
         *
         * @param player playlist agent for this event
         * @param shuffleMode shuffle mode
         * @see .SHUFFLE_MODE_NONE
         *
         * @see .SHUFFLE_MODE_ALL
         *
         * @see .SHUFFLE_MODE_GROUP
         *
         * @see .getShuffleMode
         */
        override fun onShuffleModeChanged(
            player: SessionPlayer,
            @SessionPlayer.ShuffleMode shuffleMode: Int
        ) {
            
        }

        /**
         * Called when the repeat mode is changed.
         *
         *
         * [SessionPlayer.getPreviousMediaItemIndex] and
         * [SessionPlayer.getNextMediaItemIndex] values can be outdated when this callback
         * is called if the current media item is the first or last item in the playlist.
         *
         * @param player player for this event
         * @param repeatMode repeat mode
         * @see .REPEAT_MODE_NONE
         *
         * @see .REPEAT_MODE_ONE
         *
         * @see .REPEAT_MODE_ALL
         *
         * @see .REPEAT_MODE_GROUP
         *
         * @see .getRepeatMode
         */
        override fun onRepeatModeChanged(
            player: SessionPlayer,
            @SessionPlayer.RepeatMode repeatMode: Int
        ) {
            
        }

        /**
         * Called when the player's current media item has changed. It's also called after
         * [.setPlaylist] or [.setMediaItem].
         *
         * @param player the player whose media item changed.
         * @param item the new current media item.
         * @see .getCurrentMediaItem
         */
        override fun onCurrentMediaItemChanged(
            player: SessionPlayer,
            item: MediaItem
        ) {
            PlayerDataBridge.currentMediaID.postValue(item.metadata?.mediaId)
        }

        /**
         * Called when the player finished playing. Playback state would be also set
         * [.PLAYER_STATE_PAUSED] with it.
         *
         *
         * This will be called only when the repeat mode is set to [.REPEAT_MODE_NONE].
         *
         * @param player the player whose playback is completed.
         * @see .REPEAT_MODE_NONE
         */
        override fun onPlaybackCompleted(player: SessionPlayer) {
            
        }

        /**
         * Called when the player's current audio attributes are changed.
         *
         * @param player the player whose audio attributes are changed.
         * @param attributes the new current audio attributes
         * @see .getAudioAttributes
         */
        override fun onAudioAttributesChanged(
            player: SessionPlayer,
            attributes: AudioAttributesCompat?
        ) {
            
        }

    }
}

