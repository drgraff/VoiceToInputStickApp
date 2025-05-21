package com.example.voicetoinputstick

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    private lateinit var apiKeyInput: EditText
    private lateinit var whisperUrlInput: EditText
    private lateinit var chatGptUrlInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var languageInput: EditText
    private lateinit var autoSendCheckbox: CheckBox
    private lateinit var inputStickCheckbox: CheckBox
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        apiKeyInput = findViewById(R.id.apiKeyInput)
        whisperUrlInput = findViewById(R.id.whisperUrlInput)
        chatGptUrlInput = findViewById(R.id.chatGptUrlInput)
        modelInput = findViewById(R.id.modelInput)
        languageInput = findViewById(R.id.languageInput)
        autoSendCheckbox = findViewById(R.id.autoSendCheckbox)
        inputStickCheckbox = findViewById(R.id.inputStickCheckbox)
        saveButton = findViewById(R.id.saveButton)

        apiKeyInput.setText(SettingsManager.getOpenAiApiKey())
        whisperUrlInput.setText(SettingsManager.getWhisperUrl())
        chatGptUrlInput.setText(SettingsManager.getChatGptUrl())
        modelInput.setText(SettingsManager.getModel())
        languageInput.setText(SettingsManager.getLanguage())
        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()
        inputStickCheckbox.isChecked = SettingsManager.isInputStickEnabled()

        saveButton.setOnClickListener {
            SettingsManager.apiKey = apiKeyInput.text.toString()
            SettingsManager.whisperUrl = whisperUrlInput.text.toString()
            SettingsManager.chatGptUrl = chatGptUrlInput.text.toString()
            SettingsManager.model = modelInput.text.toString()
            SettingsManager.language = languageInput.text.toString()
            SettingsManager.setAutoSendEnabled(autoSendCheckbox.isChecked)
            SettingsManager.setInputStickEnabled(inputStickCheckbox.isChecked)
            finish()
        }
    }
}