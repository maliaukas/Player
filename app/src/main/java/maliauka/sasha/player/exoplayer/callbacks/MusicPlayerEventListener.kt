package maliauka.sasha.player.exoplayer.callbacks

import android.widget.Toast
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import maliauka.sasha.player.R
import maliauka.sasha.player.exoplayer.MusicService

class MusicPlayerEventListener(
    private val musicService: MusicService
) : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)

        Toast.makeText(
            musicService,
            musicService.getString(R.string.error_message),
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        if (playbackState == Player.STATE_READY && !musicService.exoPlayer.playWhenReady) {
            musicService.stopForeground(false)
        }
    }
}
