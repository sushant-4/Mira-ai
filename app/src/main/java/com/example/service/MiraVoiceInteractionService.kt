package com.example.service

import android.content.Intent
import android.os.Bundle
import android.service.voice.VoiceInteractionService
import android.service.voice.VoiceInteractionSession
import android.service.voice.VoiceInteractionSessionService
import android.util.Log

class MiraVoiceInteractionService : VoiceInteractionService() {

    private val TAG = "MiraVoiceService"

    override fun onReady() {
        super.onReady()
        Log.i(TAG, "Mira VoiceInteractionService is ready for assistant hotword events")
    }

    override fun onShutdown() {
        super.onShutdown()
        Log.i(TAG, "Mira VoiceInteractionService shutdown")
    }
}

class MiraVoiceInteractionSessionService : VoiceInteractionSessionService() {
    override fun onNewSession(args: Bundle?): VoiceInteractionSession {
        return object : VoiceInteractionSession(this) {
            override fun onHandleAssist(data: Bundle?, structure: android.app.assist.AssistStructure?, content: android.app.assist.AssistContent?) {
                super.onHandleAssist(data, structure, content)
                // Start foreground standby or trigger listening
                MiraForegroundService.startService(context)
            }
        }
    }
}
