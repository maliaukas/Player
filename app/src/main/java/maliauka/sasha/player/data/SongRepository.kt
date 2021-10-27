package maliauka.sasha.player.data

import android.content.Context
import android.support.v4.media.MediaBrowserCompat
import android.support.v4.media.MediaBrowserCompat.MediaItem.FLAG_PLAYABLE
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.MediaMetadataCompat.*
import androidx.core.net.toUri
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import maliauka.sasha.player.R
import java.lang.reflect.ParameterizedType

class SongRepository(context: Context) {
    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val type: ParameterizedType by lazy {
        Types.newParameterizedType(
            List::class.java,
            Song::class.java
        )
    }

    private val adapter by lazy {
        return@lazy moshi.adapter<List<Song>>(type)
    }

    private val recordsJson: String by lazy {
        context.resources
            .openRawResource(R.raw.records)
            .bufferedReader()
            .use { it.readText() }
    }

    private val songList: List<Song> by lazy {
        return@lazy adapter.fromJson(recordsJson) ?: emptyList()
    }

    var songs = songList.map { song ->
        MediaMetadataCompat.Builder()
            .putString(METADATA_KEY_ARTIST, song.artist)
            .putString(METADATA_KEY_MEDIA_URI, song.trackUri)
            .putString(METADATA_KEY_TITLE, song.title)
            .putString(METADATA_KEY_ART_URI, song.bitmapUri)
            .putString(METADATA_KEY_DISPLAY_ICON_URI, song.bitmapUri)
            .putString(METADATA_KEY_DISPLAY_TITLE, song.title)
            .putString(METADATA_KEY_DISPLAY_SUBTITLE, song.artist)
            .putString(METADATA_KEY_MEDIA_ID, song.mediaId)
            .build()
    }

    fun asMediaSource(dataSourceFactory: DefaultDataSourceFactory): ConcatenatingMediaSource {
        val concatenatingMediaSource = ConcatenatingMediaSource()
        songs.forEach { song ->
            val mediaSource = ProgressiveMediaSource.Factory(dataSourceFactory)
                .createMediaSource(
                    MediaItem.fromUri(song.getString(METADATA_KEY_MEDIA_URI))
                )

            concatenatingMediaSource.addMediaSource(mediaSource)
        }

        return concatenatingMediaSource
    }

    fun asMediaItems() = songs.map {
        val desc = MediaDescriptionCompat.Builder()
            .setMediaUri(it.getString(METADATA_KEY_MEDIA_URI).toUri())
            .setTitle(it.description.title)
            .setSubtitle(it.description.subtitle)
            .setIconUri(it.description.iconUri)
            .setMediaId(it.description.mediaId)
            .build()
        MediaBrowserCompat.MediaItem(desc, FLAG_PLAYABLE)
    }.toMutableList()
}
