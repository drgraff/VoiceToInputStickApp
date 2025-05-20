package com.example.voicetoinputstick

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var apiKeyInput: EditText
    private lateinit var whisperUrlInput: EditText
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        apiKeyInput = findViewById(R.id.apiKeyInput)
        whisperUrlInput = findViewById(R.id.whisperUrlInput)
        saveButton = findViewById(R.id.saveButton)

        // Use updated accessors from SettingsManager
        apiKeyInput.setText(SettingsManager.getOpenAiApiKey())
        whisperUrlInput.setText(SettingsManager.getWhisperUrl())

        saveButton.setOnClickListener {
            SettingsManager.apiKey = apiKeyInput.text.toString()
            SettingsManager.whisperUrl = whisperUrlInput.text.toString()
            finish()
        }
    }
}
