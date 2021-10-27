package maliauka.sasha.player.ui.adapters

import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import maliauka.sasha.player.data.Song

abstract class BaseSongAdapter<VBinding : ViewBinding> :
    ListAdapter<Song, BaseSongAdapter<VBinding>.SongViewHolder>(diffUtil) {

    private var _binding: ViewBinding? = null

    @Suppress("UNCHECKED_CAST")
    protected val binding: VBinding
        get() = _binding as VBinding

    protected abstract val onBind: (Song) -> Unit

    inner class SongViewHolder(binding: VBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(song: Song) {
            onBind(song)
        }
    }

    protected abstract fun inflateViewHolderBinding(parent: ViewGroup): ViewBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        _binding = inflateViewHolderBinding(parent)

        return SongViewHolder(binding)
    }

    private var onItemClick: ((Song) -> Unit)? = null

    fun setOnClickListener(onItemClick: ((Song) -> Unit)) {
        this.onItemClick = onItemClick
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song: Song = getItem(position)
        holder.bind(song)

        holder.itemView.setOnClickListener {
            onItemClick?.let { onClick ->
                onClick(song)
            }
        }
    }

    companion object {
        private val diffUtil = object : DiffUtil.ItemCallback<Song>() {
            override fun areItemsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem.mediaId == newItem.mediaId
            }

            override fun areContentsTheSame(oldItem: Song, newItem: Song): Boolean {
                return oldItem == newItem
            }
        }
    }

    class MarginItemDecoration(private val spaceSize: Int) : RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            with(outRect) {
                if (parent.getChildAdapterPosition(view) == 0) {
                    top = spaceSize
                }
                left = spaceSize
                right = spaceSize
                bottom = spaceSize
            }
        }
    }
}
