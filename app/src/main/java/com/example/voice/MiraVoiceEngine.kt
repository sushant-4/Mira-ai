package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Locale

class MiraVoiceEngine(
    private val context: Context,
    private val scope: CoroutineScope
) : TextToSpeech.OnInitListener {

    private val TAG = "MiraVoiceEngine"

    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null
    private var isTtsReady = false

    // State flows
    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _isListening = MutableStateFlow(false)
    val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _liveCaptions = MutableStateFlow("")
    val liveCaptions: StateFlow<String> = _liveCaptions.asStateFlow()

    private val _audioAmplitude = MutableStateFlow(0.15f)
    val audioAmplitude: StateFlow<Float> = _audioAmplitude.asStateFlow()

    private val _recognizedText = MutableStateFlow("")
    val recognizedText: StateFlow<String> = _recognizedText.asStateFlow()

    private val _isWakeWordEnabled = MutableStateFlow(true)
    val isWakeWordEnabled: StateFlow<Boolean> = _isWakeWordEnabled.asStateFlow()

    // Configurable voice parameters - tuned for warm, natural humanic female vocal resonance
    var pitch: Float = 1.04f
    var speechRate: Float = 0.98f
    var voiceVolume: Float = 1.0f

    private var captionJob: Job? = null
    private var amplitudeSimulationJob: Job? = null

    // Callbacks
    var onSpeechRecognized: ((String) -> Unit)? = null
    var onWakeWordDetected: (() -> Unit)? = null

    init {
        tts = TextToSpeech(context, this)
        initSpeechRecognizer()
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isTtsReady = true
            tts?.language = Locale.US
            tts?.setPitch(pitch)
            tts?.setSpeechRate(speechRate)

            // Optimize audio pipeline for AI Assistant clarity & humanic fidelity
            try {
                val audioAttributes = android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_ASSISTANT)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()
                tts?.setAudioAttributes(audioAttributes)
            } catch (e: Exception) {
                Log.w(TAG, "AudioAttributes setup fallback: ${e.message}")
            }

            // Find and bind best natural female voice
            selectBestFemaleVoice(Locale.US)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                    startSpeakingAmplitudeSimulation()
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopAmplitudeSimulation()
                }

                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                    stopAmplitudeSimulation()
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed")
        }
    }

    private fun selectBestFemaleVoice(locale: Locale) {
        try {
            val voices = tts?.voices
            if (!voices.isNullOrEmpty()) {
                val femaleVoice = voices.firstOrNull { v ->
                    v.locale.language == locale.language &&
                    !v.isNetworkConnectionRequired &&
                    (v.name.contains("female", ignoreCase = true) ||
                     v.name.contains("en-us-x-sfg", ignoreCase = true) ||
                     v.name.contains("en-us-x-iob", ignoreCase = true) ||
                     v.name.contains("en-us-x-tpf", ignoreCase = true) ||
                     v.name.contains("en-us-x-iom", ignoreCase = true) ||
                     v.features.contains("female"))
                } ?: voices.firstOrNull { v ->
                    v.locale.language == locale.language &&
                    v.name.contains("female", ignoreCase = true)
                } ?: voices.firstOrNull { v ->
                    v.locale.language == locale.language &&
                    v.quality >= android.speech.tts.Voice.QUALITY_HIGH
                }

                if (femaleVoice != null) {
                    tts?.voice = femaleVoice
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Voice selection note: ${e.message}")
        }
    }

    private fun initSpeechRecognizer() {
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            Log.w(TAG, "SpeechRecognizer not available on this device")
            return
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    _isListening.value = true
                }

                override fun onBeginningOfSpeech() {
                    _isListening.value = true
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Convert RMS dB (-2 to 10 typical) to 0.1f..1.0f normalized amplitude
                    val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.15f, 1.0f)
                    _audioAmplitude.value = normalized
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    _isListening.value = false
                    _audioAmplitude.value = 0.15f
                }

                override fun onError(error: Int) {
                    _isListening.value = false
                    _audioAmplitude.value = 0.15f
                    Log.w(TAG, "Speech recognition error code: $error")
                }

                override fun onResults(results: Bundle?) {
                    _isListening.value = false
                    _audioAmplitude.value = 0.15f
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = matches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _recognizedText.value = text
                        checkForWakeWordOrDispatch(text)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partialMatches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val text = partialMatches?.firstOrNull() ?: ""
                    if (text.isNotBlank()) {
                        _recognizedText.value = text
                        if (checkWakeWordOnly(text)) {
                            onWakeWordDetected?.invoke()
                        }
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })
        }
    }

    fun updateCaptions(text: String) {
        _liveCaptions.value = text
    }

    fun startListening() {
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            speechRecognizer?.startListening(intent)
            _isListening.value = true
        } catch (e: Exception) {
            Log.e(TAG, "Error starting voice recognition: ${e.message}")
            _isListening.value = false
        }
    }

    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            _isListening.value = false
            _audioAmplitude.value = 0.15f
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recognition: ${e.message}")
        }
    }

    fun speak(text: String, languageCode: String = "en") {
        if (!isTtsReady || text.isBlank()) return

        // Configure language if needed
        try {
            val targetLocale = when (languageCode) {
                "hi" -> Locale.forLanguageTag("hi-IN")
                "ne" -> Locale.forLanguageTag("ne-NP")
                "es" -> Locale.forLanguageTag("es-ES")
                "fr" -> Locale.forLanguageTag("fr-FR")
                else -> Locale.US
            }
            if (tts?.isLanguageAvailable(targetLocale) == TextToSpeech.LANG_AVAILABLE) {
                tts?.language = targetLocale
                selectBestFemaleVoice(targetLocale)
            } else {
                tts?.language = Locale.US
                selectBestFemaleVoice(Locale.US)
            }
        } catch (e: Exception) {
            tts?.language = Locale.US
        }

        tts?.setPitch(pitch)
        tts?.setSpeechRate(speechRate)

        val params = Bundle().apply {
            putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, voiceVolume)
        }

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, "mira_speech_${System.currentTimeMillis()}")

        // Progressive word-by-word streaming live captions
        streamCaptions(text)
    }

    fun stopSpeaking() {
        captionJob?.cancel()
        tts?.stop()
        _isSpeaking.value = false
        stopAmplitudeSimulation()
    }

    private fun streamCaptions(fullText: String) {
        captionJob?.cancel()
        captionJob = scope.launch(Dispatchers.Main) {
            _liveCaptions.value = ""
            val words = fullText.split(" ")
            val sb = StringBuilder()
            val delayPerWord = (450L / speechRate.coerceAtLeast(0.5f)).toLong().coerceIn(120L, 500L)

            for (word in words) {
                if (sb.isNotEmpty()) sb.append(" ")
                sb.append(word)
                _liveCaptions.value = sb.toString()
                delay(delayPerWord)
            }
        }
    }

    private fun startSpeakingAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        amplitudeSimulationJob = scope.launch(Dispatchers.Default) {
            var step = 0
            while (_isSpeaking.value) {
                step++
                val base = 0.4f + 0.35f * kotlin.math.sin(step * 0.4).toFloat()
                val jitter = ((0..20).random() - 10) / 100f
                _audioAmplitude.value = (base + jitter).coerceIn(0.2f, 1.0f)
                delay(60)
            }
            _audioAmplitude.value = 0.15f
        }
    }

    private fun stopAmplitudeSimulation() {
        amplitudeSimulationJob?.cancel()
        _audioAmplitude.value = 0.15f
    }

    private fun checkForWakeWordOrDispatch(text: String) {
        val lower = text.lowercase()
        if (checkWakeWordOnly(lower)) {
            onWakeWordDetected?.invoke()
            // If the query contains command right after wake word, e.g. "Hey Mira open YouTube"
            val command = lower.replace("hey mira", "")
                .replace("hello mira", "")
                .replace("wake up mira", "")
                .replace("mira", "")
                .trim()
            if (command.isNotBlank()) {
                onSpeechRecognized?.invoke(command)
            }
        } else {
            onSpeechRecognized?.invoke(text)
        }
    }

    private fun checkWakeWordOnly(text: String): Boolean {
        val lower = text.lowercase()
        return lower.contains("hey mira") ||
                lower.contains("hello mira") ||
                lower.contains("wake up mira") ||
                lower.startsWith("mira ") ||
                lower == "mira"
    }

    fun setWakeWordEnabled(enabled: Boolean) {
        _isWakeWordEnabled.value = enabled
    }

    fun release() {
        captionJob?.cancel()
        amplitudeSimulationJob?.cancel()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
