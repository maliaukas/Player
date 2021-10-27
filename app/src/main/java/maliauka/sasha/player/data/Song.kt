package maliauka.sasha.player.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Song(
    val title: String,
    val artist: String,
    val bitmapUri: String,
    val trackUri: String,
    val mediaId: String = (++counterMediaId).toString()
) {
    private companion object {
        var counterMediaId = 0L
    }
}
