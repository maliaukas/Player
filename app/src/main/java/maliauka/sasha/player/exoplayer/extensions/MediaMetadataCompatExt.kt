package maliauka.sasha.player.exoplayer.extensions

import android.support.v4.media.MediaMetadataCompat
import maliauka.sasha.player.data.Song

fun MediaMetadataCompat.toSong() = description?.let {
    Song(
        it.title.toString(),
        it.subtitle.toString(),
        it.iconUri.toString(),
        it.mediaUri.toString(),
        it.mediaId.toString()
    )
}
