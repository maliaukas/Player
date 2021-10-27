package maliauka.sasha.player.ui.fragments

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import com.bumptech.glide.RequestManager
import dagger.hilt.android.AndroidEntryPoint
import maliauka.sasha.player.R
import maliauka.sasha.player.data.Song
import maliauka.sasha.player.databinding.FragmentSongDetailBinding
import maliauka.sasha.player.exoplayer.extensions.isPlaying
import maliauka.sasha.player.exoplayer.extensions.toSong
import maliauka.sasha.player.ui.viewmodels.MainViewModel
import maliauka.sasha.player.ui.viewmodels.SongDetailViewModel
import maliauka.sasha.player.utils.Status
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class SongDetailFragment : Fragment(R.layout.fragment_song_detail) {

    private val binding by viewBinding<FragmentSongDetailBinding>()

    private val mainViewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    private val songViewModel: SongDetailViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SongDetailViewModel::class.java)
    }

    private var currSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private var shouldUpdateSeekBar = true

    @Inject
    lateinit var glide: RequestManager

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupObservers()

        setupBinding()
    }

    private fun setupBinding() {
        with(binding) {
            ibPlayPauseDetail.setOnClickListener {
                currSong?.let {
                    mainViewModel.playOrToggleSong(it, true)
                }
            }

            ibSkipNext.setOnClickListener {
                mainViewModel.skipToNextSong()
            }

            ibSkipPrevious.setOnClickListener {
                mainViewModel.skipToPrevSong()
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(p0: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        setTimeToTextView(progress.toLong(), tvCurTime)
                    }
                }

                override fun onStartTrackingTouch(p0: SeekBar?) {
                    shouldUpdateSeekBar = false
                }

                override fun onStopTrackingTouch(seekBar1: SeekBar?) {
                    seekBar1?.let {
                        mainViewModel.seekTo(it.progress.toLong())
                    }
                    shouldUpdateSeekBar = true
                }

            })
        }
    }

    private fun updateTitleAndImage(song: Song) {
        val title = "${song.title} - ${song.artist}"
        with(binding) {
            tvSongName.text = title
            glide.load(song.bitmapUri).into(ivSongDetailImage)
        }
    }

    private fun setupObservers() {
        mainViewModel.mediaItems.observe(viewLifecycleOwner) {
            it?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            if (currSong == null && songs.isNotEmpty()) {
                                currSong = songs[0]
                                updateTitleAndImage(songs[0])
                            }
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        mainViewModel.currSong.observe(viewLifecycleOwner) {
            it?.let { song ->
                currSong = song.toSong()
                updateTitleAndImage(currSong!!)
            }
        }

        mainViewModel.playbackState.observe(viewLifecycleOwner) {
            playbackState = it
            binding.ibPlayPauseDetail.setImageResource(
                if (playbackState?.isPlaying == true)
                    R.drawable.ic_pause
                else
                    R.drawable.ic_play
            )

            binding.seekBar.progress = it?.position?.toInt() ?: 0
        }

        songViewModel.currPlayerPosition.observe(viewLifecycleOwner) {
            if (shouldUpdateSeekBar) {
                binding.seekBar.progress = it.toInt()
                setTimeToTextView(it, binding.tvCurTime)
            }
        }

        songViewModel.currSongDuration.observe(viewLifecycleOwner) {
            binding.seekBar.max = it.toInt()
            setTimeToTextView(it, binding.tvSongDuration)
        }
    }

    private val dateFormat = SimpleDateFormat("mm:ss", Locale.getDefault())

    private fun setTimeToTextView(timeMs: Long, tv: TextView) {
        tv.text = dateFormat.format(timeMs)
    }
}
