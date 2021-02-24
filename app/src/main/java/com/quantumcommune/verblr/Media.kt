package com.quantumcommune.verblr

import androidx.lifecycle.MutableLiveData
import androidx.media2.common.SessionPlayer

// [dho] adapted from : https://stackoverflow.com/a/59565726/300037 - 08/06/20
object PlayerDataBridge {
//    val status by lazy {
//        MutableLiveData<MediaPlayerStatus>()
//    }
    val duration by lazy {
        MutableLiveData<Float>()
    }

    val progress by lazy {
        MutableLiveData<Float>()
    }

    val state by lazy {
        MutableLiveData<Int>()
    }

    val buffState by lazy {
        MutableLiveData<Int>()
    }

    val buffProgress by lazy {
        MutableLiveData<Float>()
    }

    val latestResult by lazy {
        MutableLiveData<MediaPrepareResult>()
    }

    val currentMediaID by lazy {
        MutableLiveData<String>()
    }
}

data class MediaPrepareResult(
    val params : MediaPrepareParams?,
    val err : Exception?
)

data class MediaDescriptor(
    val id : String,
    val title : String,
    val artist : String,
    val artworkURL : String?,
    val contentURL : String,
    val contentType : String,
    val isRemote : Boolean
)




data class PlaylistDescriptor(
    val id : String
    // TODO
)

data class MediaPrepareParams(
    val id : String = uuid(),
    val items : List<MediaDescriptor>,
    val metadata : PlaylistDescriptor? = null,
    val wup : WaitUntilPlayableParams = WaitUntilPlayableParams()
);

data class WaitUntilPlayableParams(
    val maxAttempts : Int = LocalMediaPlayerService.DEFAULT_WUP_MAX_ATTEMPTS,
    val intervalSeconds : Float = LocalMediaPlayerService.DEFAULT_WUP_INTERVAL_SECONDS
)

data class MediaPlayerStatus(
    val state: Int,
    val duration: Float,
    val progress: Float,
    @SessionPlayer.BuffState val buffState: Int,
    val buffProgress : Float
);

