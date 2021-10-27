package maliauka.sasha.player.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.bumptech.glide.RequestManager
import maliauka.sasha.player.data.Song
import maliauka.sasha.player.databinding.ItemSongBinding
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter<ItemSongBinding>() {

    override fun inflateViewHolderBinding(parent: ViewGroup) =
        ItemSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

    override val onBind: (Song) -> Unit
        get() = { song ->
            binding.tvArtist.text = song.artist
            binding.tvTitle.text = song.title

            glide.load(song.bitmapUri).into(binding.ivSongListImage)
        }
}
