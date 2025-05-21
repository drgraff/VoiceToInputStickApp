package com.example.voicetoinputstick

import android.content.Context
import android.widget.Toast
import com.inputstick.api.basic.InputStickHID
import com.inputstick.api.basic.InputStickKeyboard

object InputStickWrapper {
    fun sendText(context: Context, text: String, layoutCode: String = "en-US") {
        try {
            if (!InputStickHID.isReady()) {
                Toast.makeText(context, "InputStick not ready", Toast.LENGTH_SHORT).show()
                return
            }
            InputStickKeyboard.type(text, layoutCode)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send to InputStick: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}