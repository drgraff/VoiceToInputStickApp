package com.example.voicetoinputstick

import android.content.Context
import android.content.SharedPreferences

object SettingsManager {
    private const val PREFS_NAME = "AppSettings"
    private const val KEY_API_KEY = "api_key"
    private const val KEY_MODEL = "model"
    private const val KEY_LANGUAGE = "language"
    private const val KEY_AUTO_SEND = "auto_send"
    private const val KEY_INPUTSTICK_ENABLED = "inputstick_enabled"
    private const val KEY_WHISPER_URL = "whisper_url"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    var apiKey: String?
        get() = prefs.getString(KEY_API_KEY, "")
        set(value) = prefs.edit().putString(KEY_API_KEY, value).apply()

    var model: String?
        get() = prefs.getString(KEY_MODEL, "gpt-3.5-turbo")
        set(value) = prefs.edit().putString(KEY_MODEL, value).apply()

    var language: String?
        get() = prefs.getString(KEY_LANGUAGE, "en")
        set(value) = prefs.edit().putString(KEY_LANGUAGE, value).apply()

    var autoSendToWhisper: Boolean
        get() = prefs.getBoolean(KEY_AUTO_SEND, true)
        set(value) = prefs.edit().putBoolean(KEY_AUTO_SEND, value).apply()

    var inputStickEnabled: Boolean
        get() = prefs.getBoolean(KEY_INPUTSTICK_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_INPUTSTICK_ENABLED, value).apply()

    var whisperUrl: String
        get() = prefs.getString(KEY_WHISPER_URL, "https://api.openai.com/v1/audio/transcriptions") ?: "https://api.openai.com/v1/audio/transcriptions"
        set(value) = prefs.edit().putString(KEY_WHISPER_URL, value).apply()

    fun isAutoSendEnabled(): Boolean {
        return autoSendToWhisper
    }

    fun setAutoSendEnabled(enabled: Boolean) {
        autoSendToWhisper = enabled
    }

    fun getOpenAiApiKey(): String {
        return apiKey ?: ""
    }

    fun getModel(): String {
        return model ?: "gpt-3.5-turbo"
    }

    fun getWhisperUrl(): String {
        return whisperUrl
    }
}
