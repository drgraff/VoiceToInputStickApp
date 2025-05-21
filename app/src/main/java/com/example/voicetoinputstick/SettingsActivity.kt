package com.example.voicetoinputstick

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var whisperUrlInput: EditText
    private lateinit var chatGptUrlInput: EditText
    private lateinit var modelInput: EditText
    private lateinit var languageInput: EditText
    private lateinit var autoSendCheckbox: CheckBox
    private lateinit var inputStickCheckbox: CheckBox
    private lateinit var darkModeCheckbox: CheckBox
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        apiKeyInput = findViewById(R.id.apiKeyInput)
        whisperUrlInput = findViewById(R.id.whisperUrlInput)
        chatGptUrlInput = findViewById(R.id.chatGptUrlInput)
        modelInput = findViewById(R.id.modelInput)
        languageInput = findViewById(R.id.languageInput)
        autoSendCheckbox = findViewById(R.id.autoSendCheckbox)
        inputStickCheckbox = findViewById(R.id.inputStickCheckbox)
        darkModeCheckbox = findViewById(R.id.darkModeCheckbox)
        saveButton = findViewById(R.id.saveButton)

        // Populate UI with existing settings
        apiKeyInput.setText(SettingsManager.apiKey)
        whisperUrlInput.setText(SettingsManager.storedWhisperUrl)
        chatGptUrlInput.setText(SettingsManager.storedChatGptUrl)
        modelInput.setText(SettingsManager.storedModel)
        languageInput.setText(SettingsManager.storedLanguage)
        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()
        inputStickCheckbox.isChecked = SettingsManager.isInputStickEnabled()
        darkModeCheckbox.isChecked = SettingsManager.isDarkModeEnabled()

        // Save settings on button click
        saveButton.setOnClickListener {
            SettingsManager.apiKey = apiKeyInput.text.toString()
            SettingsManager.storedWhisperUrl = whisperUrlInput.text.toString()
            SettingsManager.storedChatGptUrl = chatGptUrlInput.text.toString()
            SettingsManager.storedModel = modelInput.text.toString()
            SettingsManager.storedLanguage = languageInput.text.toString()
            SettingsManager.autoSendEnabled = autoSendCheckbox.isChecked
            SettingsManager.inputStickEnabled = inputStickCheckbox.isChecked
            
            // Save dark mode setting and apply it immediately
            val darkModeWasEnabled = SettingsManager.isDarkModeEnabled()
            SettingsManager.darkModeEnabled = darkModeCheckbox.isChecked
            
            // Apply theme change if the dark mode setting changed
            if (darkModeWasEnabled != darkModeCheckbox.isChecked) {
                if (darkModeCheckbox.isChecked) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                } else {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                }
            }
            
            finish()  // Close the activity
        }
    }
}