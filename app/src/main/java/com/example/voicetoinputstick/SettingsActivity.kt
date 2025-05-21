package com.example.voicetoinputstick

import android.content.Intent
import android.os.Bundle
import android.widget.*
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class SettingsActivity : AppCompatActivity() {

    private lateinit var apiKeyInput: EditText
    private lateinit var whisperUrlInput: EditText
    private lateinit var chatGptUrlInput: EditText
    private lateinit var modelInput: Spinner
    private lateinit var languageInput: EditText
    private lateinit var autoSendCheckbox: CheckBox
    private lateinit var inputStickCheckbox: CheckBox
    private lateinit var darkModeCheckbox: CheckBox
    private lateinit var saveButton: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Set up toolbar
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Initialize navigation drawer components
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.nav_view)

        // Set up the navigation drawer
        val toggle = ActionBarDrawerToggle(
            this, 
            drawerLayout, 
            toolbar, 
            R.string.navigation_drawer_open, 
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Set up navigation item click listener
        navigationView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Go to main activity
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() // Close this activity
                }
                R.id.nav_settings -> {
                    // We're already on settings screen, just close drawer
                    drawerLayout.closeDrawer(GravityCompat.START)
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

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
        
        // Setup model spinner
        val modelAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.openai_models,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            modelInput.adapter = adapter
        }
        
        // Select the currently stored model
        val currentModel = SettingsManager.storedModel
        val modelPosition = resources.getStringArray(R.array.openai_models).indexOf(currentModel)
        if (modelPosition >= 0) {
            modelInput.setSelection(modelPosition)
        }
        
        languageInput.setText(SettingsManager.storedLanguage)
        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()
        inputStickCheckbox.isChecked = SettingsManager.isInputStickEnabled()
        darkModeCheckbox.isChecked = SettingsManager.isDarkModeEnabled()

        // Save settings on button click
        saveButton.setOnClickListener {
            SettingsManager.apiKey = apiKeyInput.text.toString()
            SettingsManager.storedWhisperUrl = whisperUrlInput.text.toString()
            SettingsManager.storedChatGptUrl = chatGptUrlInput.text.toString()
            SettingsManager.storedModel = modelInput.selectedItem.toString()
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

    override fun onBackPressed() {
        // Close drawer on back press if it's open
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}