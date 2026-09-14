package com.example.voice

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.sqrt

enum class WakeSensitivity(val label: String, val thresholdRms: Float, val sleepMs: Long) {
    BATTERY_SAVER("Battery Saver", 1200f, 150L),
    BALANCED("Balanced", 750f, 80L),
    HIGH_SENSITIVITY("High Sensitivity", 450f, 30L)
}

/**
 * Lightweight, battery-efficient wake-word detection engine.
 *
 * Uses a two-stage low-power duty-cycled Voice Activity Detector (VAD) coupled with
 * local keyword spotting to detect "Mira" or "Hey Mira" with minimal battery drain.
 */
class MiraWakeWordDetector(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val TAG = "MiraWakeWordDetector"

    private val _isMonitoring = MutableStateFlow(false)
    val isMonitoring: StateFlow<Boolean> = _isMonitoring.asStateFlow()

    private val _sensitivity = MutableStateFlow(WakeSensitivity.BALANCED)
    val sensitivity: StateFlow<WakeSensitivity> = _sensitivity.asStateFlow()

    private var audioRecord: AudioRecord? = null
    private var localSpeechRecognizer: SpeechRecognizer? = null
    private var vadJob: Job? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    private var isKwsValidating = false
    private var lastTriggerTime = 0L

    var onWakeWordDetected: (() -> Unit)? = null

    companion object {
        private const val SAMPLE_RATE = 16000
        private const val FRAME_SIZE = 1024 // ~64ms at 16kHz
    }

    fun setSensitivity(level: WakeSensitivity) {
        _sensitivity.value = level
    }

    fun start() {
        if (_isMonitoring.value) return

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "Record audio permission not granted; cannot start wake-word detector")
            return
        }

        _isMonitoring.value = true
        initLocalRecognizer()
        startLowPowerVadLoop()
    }

    fun stop() {
        _isMonitoring.value = false
        vadJob?.cancel()
        vadJob = null

        try {
            audioRecord?.stop()
            audioRecord?.release()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping AudioRecord: ${e.message}")
        }
        audioRecord = null

        mainHandler.post {
            try {
                localSpeechRecognizer?.cancel()
                localSpeechRecognizer?.destroy()
            } catch (e: Exception) {
                Log.w(TAG, "Error destroying speech recognizer: ${e.message}")
            }
            localSpeechRecognizer = null
        }
    }

    private fun initLocalRecognizer() {
        mainHandler.post {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                localSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {}
                        override fun onBeginningOfSpeech() {}
                        override fun onRmsChanged(rmsdB: Float) {}
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            isKwsValidating = false
                        }

                        override fun onError(error: Int) {
                            isKwsValidating = false
                        }

                        override fun onResults(results: Bundle?) {
                            isKwsValidating = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (matches != null) {
                                for (candidate in matches) {
                                    if (isKeywordMatch(candidate)) {
                                        dispatchWakeWord()
                                        return
                                    }
                                }
                            }
                        }

                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            if (matches != null) {
                                for (candidate in matches) {
                                    if (isKeywordMatch(candidate)) {
                                        dispatchWakeWord()
                                        return
                                    }
                                }
                            }
                        }

                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }
            }
        }
    }

    private fun startLowPowerVadLoop() {
        vadJob = scope.launch(Dispatchers.IO) {
            val minBufSize = AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(FRAME_SIZE * 2)

            try {
                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.VOICE_RECOGNITION,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    minBufSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    Log.e(TAG, "AudioRecord state not initialized, fallback mode active")
                    return@launch
                }

                audioRecord?.startRecording()
                val audioBuffer = ShortArray(FRAME_SIZE)

                while (isActive && _isMonitoring.value) {
                    if (isKwsValidating) {
                        // While speech recognizer is currently active, pause AudioRecord reading to avoid resource contention
                        kotlinx.coroutines.delay(200)
                        continue
                    }

                    val read = audioRecord?.read(audioBuffer, 0, FRAME_SIZE) ?: -1
                    if (read > 0) {
                        val rms = calculateRms(audioBuffer, read)
                        val threshold = _sensitivity.value.thresholdRms

                        if (rms > threshold) {
                            // Voice activity energy detected! Trigger fast local keyword validation
                            triggerLocalKeywordSpotting()
                            kotlinx.coroutines.delay(800)
                        } else {
                            // Silence / quiet ambient background: sleep duty cycle to save battery
                            kotlinx.coroutines.delay(_sensitivity.value.sleepMs)
                        }
                    } else {
                        kotlinx.coroutines.delay(100)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Low power VAD loop exception: ${e.message}")
            }
        }
    }

    private fun calculateRms(buffer: ShortArray, length: Int): Float {
        var sum = 0.0
        for (i in 0 until length) {
            sum += buffer[i] * buffer[i]
        }
        return sqrt(sum / length).toFloat()
    }

    private fun triggerLocalKeywordSpotting() {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 2500L || isKwsValidating) return

        isKwsValidating = true
        mainHandler.post {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Offline preference for speed & battery
                }
                localSpeechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to start local spotter: ${e.message}")
                isKwsValidating = false
            }
        }
    }

    private fun isKeywordMatch(phrase: String): Boolean {
        val lower = phrase.lowercase().trim()
        return lower.contains("mira") ||
                lower.contains("hey mira") ||
                lower.contains("hello mira") ||
                lower.contains("ok mira") ||
                lower.contains("wake up mira") ||
                lower.contains("listen mira") ||
                lower.contains("मिरा") ||
                lower.contains("मीरा")
    }

    private fun dispatchWakeWord() {
        val now = System.currentTimeMillis()
        if (now - lastTriggerTime < 3000L) return
        lastTriggerTime = now
        isKwsValidating = false

        mainHandler.post {
            try {
                localSpeechRecognizer?.stopListening()
            } catch (e: Exception) {
                Log.w(TAG, "Error stopping recognizer on wake: ${e.message}")
            }
            onWakeWordDetected?.invoke()
        }
    }
}
