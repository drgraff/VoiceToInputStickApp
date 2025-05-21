# VoiceToInputStickApp

VoiceToInputStickApp is an Android application that enables voice-driven transcription, AI-powered response generation (via OpenAI), and optional automated typing to a connected device using InputStick.

---

## ✨ Features

- 🎙️ Record audio and transcribe using OpenAI Whisper API
- 🤖 Send transcribed text to ChatGPT (configurable model)
- ⌨️ Type response to PC via InputStick USB HID emulation
- ✅ Persistent app settings (API key, Whisper URL, model, language, etc.)
- 🔘 Toggle InputStick functionality on/off
- ⏱️ Visual timer + flashing indicator while recording
- 📨 Auto-send transcription after stop (configurable)
- ⚙️ Settings UI with input fields for OpenAI and Whisper configuration

---

## 📸 UI Overview

- **Start Recording** button — begins capturing audio
- **Stop Recording** button — ends capture and optionally auto-sends
- **Send to Whisper** button — manually sends last recording
- **Settings** — configure API key, model, Whisper endpoint, language, InputStick toggle
- **Checkbox** — enable/disable auto-send after recording
- **Timer and Red Dot** — visible feedback during recording

---

## 🏗️ Project Structure

```plaintext
VoiceToInputStickApp/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/voicetoinputstick/
│   │   │   ├── MainActivity.kt
│   │   │   ├── SettingsActivity.kt
│   │   │   ├── SettingsManager.kt
│   │   │   ├── WhisperService.kt
│   │   │   ├── ChatGptService.kt
│   │   │   └── InputStickWrapper.kt
│   │   └── res/layout/
│   │       ├── activity_main.xml
│   │       └── activity_settings.xml
├── InputStickAPI/
│   └── (Java source files from official SDK)
└── README.md
```

---

## 🧪 Requirements

- Android Studio Arctic Fox or newer
- Min SDK: 24  
- Target SDK: 34
- Kotlin 1.9+
- OpenAI API key
- InputStick device + USB receiver (optional)

---

## 🔐 Setup

1. Clone the project:

```bash
git clone https://github.com/YOUR_USERNAME/VoiceToInputStickApp.git
cd VoiceToInputStickApp
```

2. Open in Android Studio.

3. Add your OpenAI API key via Settings screen or hardcode in `SettingsManager.kt`.

4. Ensure your Whisper and ChatGPT endpoints are correct:
    - Whisper default: `https://api.openai.com/v1/audio/transcriptions`
    - GPT default: `https://api.openai.com/v1/chat/completions`

5. If using InputStick:
    - Connect your USB receiver to target computer
    - Enable InputStick toggle in app settings
    - Grant Bluetooth permissions when prompted

---

## 🛠️ Dependencies

```kotlin
implementation("com.squareup.okhttp3:okhttp:4.12.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
implementation("org.json:json:20231013")
implementation(project(":InputStickAPI"))
```

---

## 📤 Sending Flow

```plaintext
[Voice] → [Whisper API] → [Transcript] → [ChatGPT API] → [Response]
                                                   ↓
                                               (Optional)
                                             [InputStick]
```

---

## 🚧 Roadmap

- [x] Settings screen (API key, model, language)
- [x] Whisper and GPT integration
- [x] InputStick text output support
- [ ] Whisper local fallback (offline STT)
- [ ] Multi-language support
- [ ] Chat history and export

---

## 🤝 Acknowledgments

- [OpenAI API](https://platform.openai.com/)
- [InputStick](https://www.inputstick.com/)
- [JetBrains Kotlin](https://kotlinlang.org/)

---

## 📄 License

MIT License © 2025 David Graff
