package maliauka.sasha.player.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import by.kirich1409.viewbindingdelegate.viewBinding
import dagger.hilt.android.AndroidEntryPoint
import maliauka.sasha.player.R
import maliauka.sasha.player.databinding.FragmentSongListBinding
import maliauka.sasha.player.ui.adapters.BaseSongAdapter
import maliauka.sasha.player.ui.adapters.SongAdapter
import maliauka.sasha.player.ui.viewmodels.MainViewModel
import maliauka.sasha.player.utils.Status
import javax.inject.Inject

@AndroidEntryPoint
class SongListFragment : Fragment(R.layout.fragment_song_list) {

    private val binding by viewBinding<FragmentSongListBinding>()

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(requireActivity()).get(MainViewModel::class.java)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupAdapter()
        setupRecyclerView()
        setupObservers()
    }

    @Inject
    lateinit var songAdapter: SongAdapter

    private fun setupAdapter() {
        songAdapter.setOnClickListener {
            viewModel.playOrToggleSong(it)
        }
    }

    private fun setupRecyclerView() = binding.rvSongList.apply {
        adapter = songAdapter
        layoutManager = LinearLayoutManager(requireContext())

        val marginSize = resources.getDimension(R.dimen.padding).toInt()
        addItemDecoration(BaseSongAdapter.MarginItemDecoration(marginSize))
    }

    private fun setupObservers() {
        viewModel.mediaItems.observe(viewLifecycleOwner) { result ->
            when (result.status) {
                Status.SUCCESS -> {
                    binding.progressBar.isVisible = false

                    result.data?.let {
                        songAdapter.submitList(it)
                    }
                }
                Status.ERROR -> Unit
                Status.LOADING -> {
                    binding.progressBar.isVisible = true
                }
            }

        }
    }
}
