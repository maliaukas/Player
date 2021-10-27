package maliauka.sasha.player.exoplayer

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.ext.mediasession.TimelineQueueNavigator
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import dagger.hilt.android.AndroidEntryPoint
import maliauka.sasha.player.data.SongRepository
import maliauka.sasha.player.exoplayer.callbacks.MusicNotificationListener
import maliauka.sasha.player.exoplayer.callbacks.MusicPlaybackPreparer
import maliauka.sasha.player.exoplayer.callbacks.MusicPlayerEventListener
import maliauka.sasha.player.utils.Constants.MEDIA_ROOT_ID
import maliauka.sasha.player.utils.Constants.SERVICE_TAG
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaBrowserServiceCompat() {

    @Inject
    lateinit var dataSourceFactory: DefaultDataSourceFactory

    @Inject
    lateinit var exoPlayer: SimpleExoPlayer

    @Inject
    lateinit var songRepository: SongRepository

    private lateinit var notificationManager: MusicNotificationManager
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var mediaSessionConnector: MediaSessionConnector

    var isForegroundService = false

    private var currentPlayingSong: MediaMetadataCompat? = null

    private var isPlayerInitialized = false

    private lateinit var musicEventListener: MusicPlayerEventListener

    companion object {
        var currSongDuration = 0L
            private set
    }

    @SuppressLint("UnspecifiedImmutableFlag")
    override fun onCreate() {
        super.onCreate()

        val activityIntent = packageManager?.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, 0)
        }

        mediaSession = MediaSessionCompat(this, SERVICE_TAG).apply {
            setSessionActivity(activityIntent)
            isActive = true
        }

        sessionToken = mediaSession.sessionToken

        notificationManager = MusicNotificationManager(
            this,
            mediaSession.sessionToken,
            MusicNotificationListener(this)
        ) {
            if (exoPlayer.duration != C.TIME_UNSET) {
                currSongDuration = exoPlayer.duration
            }
        }

        val musicPlaybackPreparer = MusicPlaybackPreparer(songRepository) {
            currentPlayingSong = it

            preparePlayer(
                songRepository.songs,
                it,
                true
            )
        }

        mediaSessionConnector = MediaSessionConnector(mediaSession)
        mediaSessionConnector.setPlaybackPreparer(musicPlaybackPreparer)
        mediaSessionConnector.setQueueNavigator(MusicQueueNavigator())
        mediaSessionConnector.setPlayer(exoPlayer)

        musicEventListener = MusicPlayerEventListener(this)
        exoPlayer.addListener(musicEventListener)

        notificationManager.showNotification(exoPlayer)
    }

    private inner class MusicQueueNavigator : TimelineQueueNavigator(mediaSession) {
        override fun getMediaDescription(player: Player, windowIndex: Int): MediaDescriptionCompat {
            return songRepository.songs[windowIndex].description
        }
    }

    private fun preparePlayer(
        songs: List<MediaMetadataCompat>,
        itemToPlay: MediaMetadataCompat?,
        playNow: Boolean
    ) {
        val currSongIdx = if (itemToPlay == null) 0 else songs.indexOf(itemToPlay)
        exoPlayer.setMediaSource(songRepository.asMediaSource(dataSourceFactory))
        exoPlayer.prepare()

        exoPlayer.seekTo(currSongIdx, 0L)
        exoPlayer.playWhenReady = playNow
    }

    override fun onDestroy() {
        super.onDestroy()

        exoPlayer.removeListener(musicEventListener)
        exoPlayer.release()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        exoPlayer.stop()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        return BrowserRoot(MEDIA_ROOT_ID, null)
    }

    override fun onLoadChildren(
        parentId: String,
        result: Result<MutableList<MediaBrowserCompat.MediaItem>>
    ) {
        when (parentId) {
            MEDIA_ROOT_ID -> {
                result.sendResult(songRepository.asMediaItems())
                if (!isPlayerInitialized && songRepository.songs.isNotEmpty()) {
                    preparePlayer(
                        songRepository.songs,
                        songRepository.songs[0],
                        false
                    )

                    isPlayerInitialized = true
                }
            }
        }
    }
}
