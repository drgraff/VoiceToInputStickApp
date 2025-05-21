package com.example.voicetoinputstick

import android.content.Context
import android.widget.Toast
import com.inputstick.api.basic.InputStickHID
import com.inputstick.api.basic.InputStickKeyboard
import com.inputstick.api.InputStickError

object InputStickWrapper {
    fun sendText(context: Context, text: String, layoutCode: String = "en-US") {
        try {
            if (!InputStickHID.isReady()) {
                // Get detailed error information
                val errorCode = InputStickHID.getErrorCode()
                val disconnectReason = InputStickHID.getDisconnectReason()
                val errorMessage = InputStickError.getFullErrorMessage(errorCode)
                
                // Create a more descriptive error message
                val detailedMessage = when {
                    errorCode != 0 -> "InputStick not ready: $errorMessage (Error code: $errorCode)"
                    disconnectReason != 0 -> "InputStick disconnected: Reason code $disconnectReason"
                    else -> "InputStick not ready: Unknown reason"
                }
                
                Toast.makeText(context, detailedMessage, Toast.LENGTH_LONG).show()
                return
            }
            InputStickKeyboard.type(text, layoutCode)
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to send to InputStick: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}