package kr.bluevisor.openaittssample

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class TextToSpeechViewModel @Inject constructor(
    private val repository: TextToSpeechRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _audioFile = MutableStateFlow<File?>(null)
    val audioFile: StateFlow<File?> = _audioFile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun generateSpeech(
        apiKey: String,
        text: String,
        voice: String,
        voiceInstruction: String? = null
    ) {
        _isLoading.value = true
        _error.value = null

        viewModelScope.launch {
            repository.generateSpeech(apiKey, text, voice, voiceInstruction = voiceInstruction)
                .onSuccess { file ->
                    _audioFile.value = file
                    audioPlayer.playAudio(file)
                }
                .onFailure { e ->
                    _error.value = e.message
                }

            _isLoading.value = false
        }
    }

    fun stopPlayback() {
        audioPlayer.stop()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
    }
}