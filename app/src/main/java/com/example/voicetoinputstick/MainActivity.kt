package com.example.voicetoinputstick

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import android.content.Intent
import okhttp3.*
import org.json.JSONObject
import java.util.concurrent.TimeUnit
import com.inputstick.api.ConnectionManager

class MainActivity : AppCompatActivity() {
    private lateinit var recordButton: Button
    private lateinit var stopButton: Button
    private lateinit var sendButton: Button
    private lateinit var settingsButton: Button
    private lateinit var timerTextView: TextView
    private lateinit var flashingIndicator: TextView
    private lateinit var autoSendCheckbox: CheckBox

    private var mediaRecorder: MediaRecorder? = null
    private var audioFilePath: String = ""
    private var isRecording = false
    private var timerJob: Job? = null
    private var flashingJob: Job? = null
    private var startTime: Long = 0

    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Init InputStick connection system
        ConnectionManager.init(applicationContext)
        ConnectionManager.getInstance().connect() // optional

        SettingsManager.init(applicationContext)

        recordButton = findViewById(R.id.recordButton)
        stopButton = findViewById(R.id.stopButton)
        sendButton = findViewById(R.id.sendButton)
        settingsButton = findViewById(R.id.settingsButton)
        timerTextView = findViewById(R.id.timerTextView)
        flashingIndicator = findViewById(R.id.flashingIndicator)
        autoSendCheckbox = findViewById(R.id.autoSendCheckbox)

        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()

        recordButton.setOnClickListener { startRecording() }
        stopButton.setOnClickListener { stopRecording() }
        sendButton.setOnClickListener { sendToWhisper(audioFilePath) }
        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        autoSendCheckbox.setOnCheckedChangeListener { _, isChecked ->
            SettingsManager.setAutoSendEnabled(isChecked)
        }

        stopButton.isEnabled = false
        flashingIndicator.text = ""

        checkPermissions()
    }

    override fun onResume() {
        super.onResume()
        // Refresh UI in case settings changed
        autoSendCheckbox.isChecked = SettingsManager.isAutoSendEnabled()
    }

    private fun checkPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 200)
        }
    }

    private fun startRecording() {
        val fileName = "recording_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())}.m4a"
        val audioDir = File(cacheDir, "recordings")
        if (!audioDir.exists()) audioDir.mkdirs()
        audioFilePath = File(audioDir, fileName).absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(audioFilePath)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            prepare()
            start()
        }

        isRecording = true
        startTime = System.currentTimeMillis()

        recordButton.isEnabled = false
        stopButton.isEnabled = true

        startTimer()
        startFlashing()
    }

    private fun stopRecording() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        isRecording = false

        stopTimer()
        stopFlashing()

        recordButton.isEnabled = true
        stopButton.isEnabled = false

        if (autoSendCheckbox.isChecked) {
            sendToWhisper(audioFilePath)
        }
    }

    private fun startTimer() {
        timerJob = coroutineScope.launch {
            while (isRecording) {
                val elapsed = System.currentTimeMillis() - startTime
                val seconds = (elapsed / 1000) % 60
                val minutes = (elapsed / 1000) / 60
                timerTextView.text = String.format("%02d:%02d", minutes, seconds)
                delay(500)
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerTextView.text = ""
    }

    private fun startFlashing() {
        flashingJob = coroutineScope.launch {
            var visible = false
            while (isRecording) {
                flashingIndicator.text = if (visible) "\u25cf" else ""
                flashingIndicator.setTextColor(android.graphics.Color.RED)
                visible = !visible
                delay(500)
            }
            flashingIndicator.text = ""
        }
    }

    private fun stopFlashing() {
        flashingJob?.cancel()
        flashingIndicator.text = ""
    }

    private fun sendToWhisper(filePath: String) {
        Toast.makeText(this, "Sending to Whisper...", Toast.LENGTH_SHORT).show()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val apiKey = SettingsManager.getOpenAiApiKey()
                val model = SettingsManager.getModel()
                val whisperUrl = SettingsManager.getWhisperUrl()
                val language = SettingsManager.getLanguage()
                val client = OkHttpClient.Builder().connectTimeout(60, TimeUnit.SECONDS).build()
                val file = File(filePath)
                val mediaType = MediaType.parse("audio/m4a")

                val body = MultipartBody.Builder().setType(MultipartBody.FORM)
                    .addFormDataPart("model", "whisper-1")
                    .addFormDataPart("file", file.name, RequestBody.create(mediaType, file))
                    .addFormDataPart("language", language)
                    .build()

                val request = Request.Builder()
                    .url(whisperUrl)
                    .addHeader("Authorization", "Bearer $apiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body()?.string()

                if (!response.isSuccessful) {
                    throw Exception("Whisper error: ${response.code()} ${response.message()}\n$responseBody")
                }

                val transcription = JSONObject(responseBody ?: "{}").optString("text")

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Transcribed: $transcription", Toast.LENGTH_LONG).show()
                    sendToChatGPT(transcription)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Whisper error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendToChatGPT(transcription: String) {
        val apiKey = SettingsManager.getOpenAiApiKey()
        val model = SettingsManager.getModel()
        val chatGptUrl = SettingsManager.getChatGptUrl()
        val client = OkHttpClient()

        val json = """
            {
                "model": "$model",
                "messages": [
                  {"role": "user", "content": "${transcription.replace("\"", "\\\"")}"}
                ]
            }
        """.trimIndent()

        val requestBody = RequestBody.create(MediaType.parse("application/json"), json)
        val request = Request.Builder()
            .url(chatGptUrl)
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                val responseText = response.body()?.string()

                if (!response.isSuccessful) {
                    throw Exception("ChatGPT error: ${response.code()} ${response.message()}\n$responseText")
                }

                val jsonResponse = JSONObject(responseText ?: "")
                val reply = jsonResponse.getJSONArray("choices")
                    .getJSONObject(0).getJSONObject("message")
                    .getString("content")

                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Response: $reply", Toast.LENGTH_LONG).show()
                    sendToInputStick(reply)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "ChatGPT error: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun sendToInputStick(text: String) {
        if (SettingsManager.isInputStickEnabled()) {
            try {
                InputStickWrapper.sendText(applicationContext, text)
                Toast.makeText(this, "Sent to InputStick", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "InputStick error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}