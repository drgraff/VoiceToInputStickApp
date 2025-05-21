package com.example.voicetoinputstick

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

class WhisperService(private val context: Context) {
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()
    
    suspend fun transcribeAudio(audioFilePath: String): Result<String> = withContext(Dispatchers.IO) {
        try {
            val apiKey = SettingsManager.getOpenAiApiKey()
            if (apiKey.isBlank()) {
                return@withContext Result.failure(Exception("API key not set"))
            }
            
            val file = File(audioFilePath)
            if (!file.exists()) {
                return@withContext Result.failure(IOException("Audio file does not exist"))
            }
            
            // Call the OpenAI Whisper API
            Log.d("WhisperService", "Sending audio file to Whisper API: ${file.name}")
            
            val whisperUrl = SettingsManager.getWhisperUrl()
            val language = SettingsManager.getLanguage()
            
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(
                    "file", 
                    file.name,
                    RequestBody.create("audio/mpeg".toMediaTypeOrNull(), file)
                )
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("language", language)
                .build()
