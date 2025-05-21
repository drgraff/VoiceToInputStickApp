package com.example.voicetoinputstick

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var whisperUrlInput: EditText
    private lateinit var chatGptUrlInput: EditText
    private lateinit var modelSpinner: Spinner
    private lateinit var languageInput: EditText
    private lateinit var autoSendCheckbox: CheckBox
    private lateinit var inputStickCheckbox: CheckBox
    private lateinit var saveButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize UI elements
        apiKeyInput = findViewById(R.id.apiKeyInput)
        whisperUrlInput = findViewById(R.id.whisperUrlInput)
        chatGptUrlInput = findViewById(R.id.chatGptUrlInput)
        modelSpinner = findViewById(R.id.modelSpinner)
        languageInput = findViewById(R.id.languageInput)
        autoSendCheckbox = findViewById(R.id.autoSendCheckbox)
        inputStickCheckbox = findViewById(R.id.inputStickCheckbox)
        saveButton = findViewById(R.id.saveButton)

        // Setup model spinner with available models
        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            SettingsManager.AVAILABLE_MODELS
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modelSpinner.adapter = adapter
        
        // Set current selected model
        val currentModel = SettingsManager.getModel()
        val modelPosition = SettingsManager.AVAILABLE_MODELS.indexOf(currentModel)
        if (modelPosition >= 0) {
            modelSpinner.setSelection(modelPosition)
        }

        // Populate UI with existing settings
        apiKeyInput.setText(SettingsManager.apiKey)
        whisperUrlInput.setText(SettingsManager.storedWhisperUrl)
        chatGptUrlInput.setText(SettingsManager.storedChatGptUrl)
        languageInput.setText(SettingsManager.storedLanguage)
        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()
        inputStickCheckbox.isChecked = SettingsManager.isInputStickEnabled()

        // Save settings on button click
        saveButton.setOnClickListener {
            SettingsManager.apiKey = apiKeyInput.text.toString()
            SettingsManager.storedWhisperUrl = whisperUrlInput.text.toString()
            SettingsManager.storedChatGptUrl = chatGptUrlInput.text.toString()
            SettingsManager.storedModel = modelSpinner.selectedItem.toString()
            SettingsManager.storedLanguage = languageInput.text.toString()
            SettingsManager.autoSendEnabled = autoSendCheckbox.isChecked
            SettingsManager.inputStickEnabled = inputStickCheckbox.isChecked
            finish()  // Close the activity
        }
    }
}