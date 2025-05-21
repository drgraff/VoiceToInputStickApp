package com.example.voicetoinputstick

import android.content.Context
import android.widget.Toast
import com.inputstick.api.ConnectionManager
import com.inputstick.api.basic.InputStickKeyboard

object InputStickWrapper {
    fun sendText(context: Context, text: String) {
        try {
            val manager = ConnectionManager.getInstance()
            if (!manager.isConnected) {
                Toast.makeText(context, "InputStick not connected", Toast.LENGTH_SHORT).show()
                return
            }

            val keyboard = InputStickKeyboard()
            keyboard.type(text)

        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send to InputStick: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}