package kr.bluevisor.openaittssample

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface OpenAIService {
    @POST("audio/speech")
    suspend fun createSpeech(
        @Header("Authorization") apiKey: String,
        @Body requestBody: RequestBody
    ): Response<ResponseBody>
}