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
import com.inputstick.api.basic.InputStickHID

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

        // Initialize InputStick using the official API call
        InputStickHID.connect(application)

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
            SettingsManager.autoSendEnabled = isChecked
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
        timerJob?.cancel()
        flashingJob?.cancel()

        recordButton.isEnabled = true
        stopButton.isEnabled = false
        flashingIndicator.text = ""
    }

    private fun startTimer() {
        timerJob = coroutineScope.launch {
            while (isRecording) {
                val elapsed = System.currentTimeMillis() - startTime
                timerTextView.text = String.format("%02d:%02d", (elapsed / 1000) / 60, (elapsed / 1000) % 60)
                delay(500)
            }
        }
    }

    private fun startFlashing() {
        flashingJob = coroutineScope.launch {
            var visible = false
            while (isRecording) {
                flashingIndicator.text = if (visible) "●" else ""
                visible = !visible
                delay(500)
            }
            flashingIndicator.text = ""
        }
    }

    private fun sendToWhisper(filePath: String) {
        if (filePath.isEmpty()) {
            Toast.makeText(this, "No recording found", Toast.LENGTH_SHORT).show()
            return
        }
        
        Toast.makeText(this, "Processing audio...", Toast.LENGTH_SHORT).show()
        
        coroutineScope.launch {
            val whisperService = WhisperService(this@MainActivity)
            val transcriptionResult = whisperService.transcribeAudio(filePath)
            
            transcriptionResult.fold(
                onSuccess = { transcription ->
                    // Show transcription to user
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Transcription: $transcription", Toast.LENGTH_LONG).show()
                    }
                    
                    // Send to ChatGPT
                    val chatGptService = ChatGptService(this@MainActivity)
                    val chatResult = chatGptService.getChatResponse(transcription)
                    
                    chatResult.fold(
                        onSuccess = { chatResponse ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "Response received from ChatGPT", Toast.LENGTH_SHORT).show()
                                
                                // Check if InputStick is enabled
                                if (SettingsManager.isInputStickEnabled()) {
                                    // Send to InputStick
                                    InputStickWrapper.sendText(this@MainActivity, chatResponse)
                                } else {
                                    Toast.makeText(this@MainActivity, "InputStick is disabled", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onFailure = { error ->
                            withContext(Dispatchers.Main) {
                                Toast.makeText(this@MainActivity, "ChatGPT error: ${error.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    )
                },
                onFailure = { error ->
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Transcription error: ${error.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }
    }
}