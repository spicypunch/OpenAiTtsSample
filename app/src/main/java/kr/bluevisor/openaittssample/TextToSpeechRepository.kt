package kr.bluevisor.openaittssample

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TextToSpeechRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openAIService: OpenAIService
) {
    private val gson = Gson()
    private val cacheDir = File(context.cacheDir, "tts_cache")

    init {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }

    suspend fun generateSpeech(
        apiKey: String,
        text: String,
        voice: String,
        model: String = "tts-1",
        voiceInstruction: String? = null
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val cacheFileName = generateCacheFileName(text, voice, voiceInstruction)
            val cacheFile = File(cacheDir, cacheFileName)

            if (cacheFile.exists()) {
                return@withContext Result.success(cacheFile)
            }

            val request = TextToSpeechRequest(
                model = model,
                input = text,
                voice = voice,
                voiceInstruction = voiceInstruction
            )

            val requestJson = gson.toJson(request)
            val requestBody = requestJson.toRequestBody("application/json".toMediaTypeOrNull())

            val response = openAIService.createSpeech(
                apiKey = "Bearer $apiKey",
                requestBody = requestBody
            )

            if (response.isSuccessful) {
                response.body()?.let { responseBody ->
                    val outputStream = FileOutputStream(cacheFile)
                    responseBody.byteStream().use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    return@withContext Result.success(cacheFile)
                } ?: return@withContext Result.failure(Exception("Empty response body"))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                Log.e("TTS", "API Error: $errorMessage")
                return@withContext Result.failure(Exception("API Error: $errorMessage"))
            }
        } catch (e: Exception) {
            Log.e("TTS", "Exception: ${e.message}")
            return@withContext Result.failure(e)
        }
    }

    private fun generateCacheFileName(text: String, voice: String, voiceInstruction: String?): String {
        val hash = "${text}_${voice}_${voiceInstruction}".hashCode()
        return "$hash.mp3"
    }
}