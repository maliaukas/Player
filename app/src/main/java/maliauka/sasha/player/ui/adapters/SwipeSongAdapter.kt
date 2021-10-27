package maliauka.sasha.player.ui.adapters

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import maliauka.sasha.player.data.Song
import maliauka.sasha.player.databinding.ItemSongSwipeBinding

class SwipeSongAdapter : BaseSongAdapter<ItemSongSwipeBinding>() {

    override fun inflateViewHolderBinding(parent: ViewGroup) =
        ItemSongSwipeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

    override val onBind: (Song) -> Unit
        @SuppressLint("SetTextI18n")
        get() = { song ->
            binding.tvTitle.text = "${song.title} - ${song.artist}"
        }
}
