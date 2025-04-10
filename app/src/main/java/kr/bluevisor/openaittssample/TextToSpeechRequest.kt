package kr.bluevisor.openaittssample

import com.google.gson.annotations.SerializedName

data class TextToSpeechRequest(
    val model: String,
    val input: String,
    val voice: String,
    @SerializedName("response_format") val responseFormat: String = "mp3",
    val speed: Float = 1.0f,
    @SerializedName("voice_instruction") val voiceInstruction: String? = null
)
