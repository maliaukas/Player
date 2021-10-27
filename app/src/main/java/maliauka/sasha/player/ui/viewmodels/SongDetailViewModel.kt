package maliauka.sasha.player.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import maliauka.sasha.player.exoplayer.MusicService
import maliauka.sasha.player.exoplayer.MusicServiceConnection
import maliauka.sasha.player.exoplayer.extensions.currentPlaybackPosition
import maliauka.sasha.player.utils.Constants.UPDATE_PLAYER_POSITION_INTERVAL_MS
import javax.inject.Inject

@HiltViewModel
class SongDetailViewModel @Inject constructor(
    musicServiceConnection: MusicServiceConnection
) : ViewModel() {

    private val playbackState = musicServiceConnection.playbackState

    private val _currSongDuration = MutableLiveData<Long>()
    val currSongDuration: LiveData<Long>
        get() = _currSongDuration

    private val _currPlayerPosition = MutableLiveData<Long>()
    val currPlayerPosition: LiveData<Long>
        get() = _currPlayerPosition

    init {
        updateCurrPlayerPosition()
    }

    private fun updateCurrPlayerPosition() {
        viewModelScope.launch {
            while (true) {
                val position = playbackState.value?.currentPlaybackPosition
                if (currPlayerPosition.value != position) {
                    position?.let {
                        _currPlayerPosition.postValue(it)
                    }
                    _currSongDuration.postValue(MusicService.currSongDuration)
                }

                delay(UPDATE_PLAYER_POSITION_INTERVAL_MS)
            }
        }
    }
}
