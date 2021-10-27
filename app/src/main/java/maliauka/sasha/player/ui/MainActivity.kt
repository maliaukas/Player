package maliauka.sasha.player.ui

import android.os.Bundle
import android.support.v4.media.session.PlaybackStateCompat
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.Navigation
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.RequestManager
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import maliauka.sasha.player.R
import maliauka.sasha.player.data.Song
import maliauka.sasha.player.databinding.ActivityMainBinding
import maliauka.sasha.player.ui.adapters.SwipeSongAdapter
import maliauka.sasha.player.exoplayer.extensions.isPlaying
import maliauka.sasha.player.exoplayer.extensions.toSong
import maliauka.sasha.player.ui.viewmodels.MainViewModel
import maliauka.sasha.player.ui.viewmodels.SongDetailViewModel
import maliauka.sasha.player.utils.Status
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    private val songViewModel: SongDetailViewModel by viewModels()

    @Inject
    lateinit var glide: RequestManager

    @Inject
    lateinit var swipeSongAdapter: SwipeSongAdapter

    private var currSong: Song? = null

    private var playbackState: PlaybackStateCompat? = null

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityMainBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(binding.root)

        setupObservers()
        setupBinding()
        setupNavigation()
    }

    private fun setupNavigation() {
        val navController = Navigation.findNavController(this, R.id.nav_host_fragment)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.songDetailFragment -> hideBottomBar()
                R.id.songListFragment -> showBottomBar()
                else -> showBottomBar()
            }
        }

        swipeSongAdapter.setOnClickListener {
            navController.navigate(R.id.globalActionToSongDetailFragment)
        }
    }

    private fun setupBinding() {
        with(binding) {
            vpSong.adapter = swipeSongAdapter

            vpSong.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)

                    if (playbackState?.isPlaying == true) {
                        viewModel.playOrToggleSong(swipeSongAdapter.currentList[position])
                    } else {
                        currSong = swipeSongAdapter.currentList[position]
                        glide.load(currSong?.bitmapUri).into(binding.ivCurSongImage)
                    }
                }
            })

            ibPlayPause.setOnClickListener {
                currSong?.let {
                    viewModel.playOrToggleSong(it, true)
                }
            }
        }
    }

    private fun switchViewPagerToCurrentSong(song: Song) {
        val idxOfSong = swipeSongAdapter.currentList.indexOf(song)
        if (idxOfSong != -1) {
            binding.vpSong.currentItem = idxOfSong
            currSong = song
        }
    }

    private fun setupObservers() {
        viewModel.mediaItems.observe(this) { result ->
            result?.let {
                when (result.status) {
                    Status.SUCCESS -> {
                        result.data?.let { songs ->
                            swipeSongAdapter.submitList(songs)

                            if (songs.isNotEmpty()) {
                                glide.load((currSong ?: songs[0]).bitmapUri)
                                    .into(binding.ivCurSongImage)
                            }

                            switchViewPagerToCurrentSong(currSong ?: return@observe)
                        }
                    }
                    Status.ERROR -> Unit
                    Status.LOADING -> Unit
                }
            }
        }

        viewModel.currSong.observe(this) {
            it?.let {
                currSong = it.toSong()
                glide.load(currSong?.bitmapUri).into(binding.ivCurSongImage)
                switchViewPagerToCurrentSong(currSong ?: return@observe)
            }
        }

        songViewModel.currPlayerPosition.observe(this) {
            binding.progressBar.progress = it.toInt()
        }

        songViewModel.currSongDuration.observe(this) {
            binding.progressBar.max = it.toInt()
        }

        viewModel.playbackState.observe(this) {
            playbackState = it

            binding.ibPlayPause.setImageResource(
                if (playbackState?.isPlaying == true) R.drawable.ic_pause else R.drawable.ic_play
            )

            binding.progressBar.progress = it?.position?.toInt() ?: 0
        }

        viewModel.isConnected.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> Unit
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An error occured!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Status.LOADING -> Unit
                }
            }
        }

        viewModel.networkError.observe(this) {
            it?.getContentIfNotHandled()?.let { result ->
                when (result.status) {
                    Status.SUCCESS -> Unit
                    Status.ERROR -> Snackbar.make(
                        binding.root,
                        result.message ?: "An error occured!",
                        Snackbar.LENGTH_SHORT
                    ).show()
                    Status.LOADING -> Unit
                }
            }
        }
    }

    private fun hideBottomBar() {
        with(binding) {
            ibPlayPause.isVisible = false
            ivCurSongImage.isVisible = false
            vpSong.isVisible = false
            progressBar.isVisible = false
        }
    }

    private fun showBottomBar() {
        with(binding) {
            ibPlayPause.isVisible = true
            ivCurSongImage.isVisible = true
            vpSong.isVisible = true
            progressBar.isVisible = true
        }
    }
}
