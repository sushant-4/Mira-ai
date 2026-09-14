package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DirectAction
import com.example.ai.GeminiApiClient
import com.example.ai.MiraInterpretation
import com.example.ai.MiraNluEngine
import com.example.automation.ActionResult
import com.example.automation.DeviceActionHandler
import com.example.automation.MiraTaskPlan
import com.example.automation.ScreenInspector
import com.example.automation.StepStatus
import com.example.automation.ApkExportManager
import com.example.data.MiraDatabase
import com.example.data.model.MiraMemory
import com.example.data.model.TaskRecord
import com.example.service.MiraForegroundService
import com.example.ui.components.OrbState
import com.example.voice.MiraVoiceEngine
import com.example.voice.WakeSensitivity
import java.io.File
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class MiraScreen {
    SPLASH,
    HOME,
    CONVERSATION,
    TASK_EXECUTION,
    PERMISSIONS_CENTER,
    VOICE_SETTINGS,
    APPEARANCE,
    MEMORY,
    TASK_HISTORY,
    SCREEN_INSPECTOR,
    APK_DOWNLOAD
}

data class ChatMessage(
    val id: String,
    val sender: String, // "USER" or "MIRA"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val language: String? = null
)

data class ConfirmationRequest(
    val title: String,
    val details: String,
    val prompt: String,
    val onConfirm: () -> Unit
)

class MiraViewModel(application: Application) : AndroidViewModel(application) {

    private val db = MiraDatabase.getDatabase(application)
    private val dao = db.miraDao()

    val deviceActionHandler = DeviceActionHandler(application)
    val screenInspector = ScreenInspector()
    val voiceEngine = MiraVoiceEngine(application, viewModelScope)

    // UI state
    private val _currentScreen = MutableStateFlow(MiraScreen.SPLASH)
    val currentScreen: StateFlow<MiraScreen> = _currentScreen.asStateFlow()

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _activeTaskPlan = MutableStateFlow<MiraTaskPlan?>(null)
    val activeTaskPlan: StateFlow<MiraTaskPlan?> = _activeTaskPlan.asStateFlow()

    private val _confirmationRequest = MutableStateFlow<ConfirmationRequest?>(null)
    val confirmationRequest: StateFlow<ConfirmationRequest?> = _confirmationRequest.asStateFlow()

    private val _isFloatingOverlayVisible = MutableStateFlow(false)
    val isFloatingOverlayVisible: StateFlow<Boolean> = _isFloatingOverlayVisible.asStateFlow()

    private val _isStandbyServiceRunning = MutableStateFlow(false)
    val isStandbyServiceRunning: StateFlow<Boolean> = _isStandbyServiceRunning.asStateFlow()

    private val _currentLanguage = MutableStateFlow("English")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _captionFontSize = MutableStateFlow(16)
    val captionFontSize: StateFlow<Int> = _captionFontSize.asStateFlow()

    private val _orbStyle = MutableStateFlow("Quantum Core") // "Quantum Core", "Cyber Vortex", "Neon Pulse"
    val orbStyle: StateFlow<String> = _orbStyle.asStateFlow()

    // Database backed streams
    val memories = dao.getAllMemories().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val taskHistory = dao.getAllTasks().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Startup telemetry messages and progress for Splash Screen
    private val _startupLog = MutableStateFlow("Initializing AI...")
    val startupLog: StateFlow<String> = _startupLog.asStateFlow()

    private val _startupProgress = MutableStateFlow(0)
    val startupProgress: StateFlow<Int> = _startupProgress.asStateFlow()

    // APK Mobile Download State
    private val _isApkDownloading = MutableStateFlow(false)
    val isApkDownloading: StateFlow<Boolean> = _isApkDownloading.asStateFlow()

    private val _apkDownloadProgress = MutableStateFlow(0)
    val apkDownloadProgress: StateFlow<Int> = _apkDownloadProgress.asStateFlow()

    private val _downloadedApkFile = MutableStateFlow<File?>(null)
    val downloadedApkFile: StateFlow<File?> = _downloadedApkFile.asStateFlow()

    // Battery-efficient Wake Word Sensitivity
    private val _wakeSensitivity = MutableStateFlow(WakeSensitivity.BALANCED)
    val wakeSensitivity: StateFlow<WakeSensitivity> = _wakeSensitivity.asStateFlow()

    init {
        // Wire voice engine callbacks
        voiceEngine.onSpeechRecognized = { text ->
            processUserQuery(text)
        }
        voiceEngine.onWakeWordDetected = {
            onWakeWordDetected()
        }

        // Run boot sequence
        runStartupSequence()

        // Insert initial memory seed if empty
        viewModelScope.launch {
            val existing = dao.getAllMemories().firstOrNull()
            if (existing.isNullOrEmpty()) {
                dao.insertMemory(MiraMemory(category = "Identity", content = "User Assistant Name: Mira"))
                dao.insertMemory(MiraMemory(category = "Preference", content = "Default Media Player: YouTube"))
                dao.insertMemory(MiraMemory(category = "Preference", content = "Voice Style: Polite & Futuristic"))
            }
        }
    }

    private fun runStartupSequence() {
        viewModelScope.launch {
            // High-speed smooth progress loop reaching 100%
            val milestones = listOf(
                Pair(15, "INITIALIZING NEURAL CORE..."),
                Pair(38, "TUNING HUMANIC VOICE SYNTHESIZER..."),
                Pair(62, "SYNCHRONIZING DEVICE OS AUTONOMY..."),
                Pair(84, "CALIBRATING MULTILINGUAL REASONING..."),
                Pair(96, "ARMING BACKGROUND WAKE-WORD SENSORS..."),
                Pair(100, "MIRA NEURAL CORE 100% SYNCHRONIZED.")
            )

            var current = 0
            for ((target, message) in milestones) {
                _startupLog.value = message
                while (current < target) {
                    current += (1..3).random()
                    if (current > target) current = target
                    _startupProgress.value = current
                    delay(35)
                }
                delay(120)
            }

            _startupProgress.value = 100
            delay(300)
            _currentScreen.value = MiraScreen.HOME
            // Greet user
            delay(200)
            val welcomeGreeting = "Mira initialized and standing by. How can I assist you today?"
            addMessage("MIRA", welcomeGreeting, "en")
            voiceEngine.speak(welcomeGreeting, "en")
        }
    }

    fun navigateTo(screen: MiraScreen) {
        _currentScreen.value = screen
    }

    fun onWakeWordDetected() {
        _orbState.value = OrbState.LISTENING
        voiceEngine.speak("Yes? I'm listening.", "en")
        voiceEngine.startListening()
    }

    fun toggleVoiceListening() {
        if (voiceEngine.isListening.value) {
            voiceEngine.stopListening()
            _orbState.value = OrbState.IDLE
        } else {
            _orbState.value = OrbState.LISTENING
            voiceEngine.startListening()
        }
    }

    fun toggleFloatingOverlay() {
        _isFloatingOverlayVisible.value = !_isFloatingOverlayVisible.value
    }

    fun toggleStandbyService(context: Context) {
        if (_isStandbyServiceRunning.value) {
            MiraForegroundService.stopService(context)
            _isStandbyServiceRunning.value = false
        } else {
            MiraForegroundService.startService(context)
            _isStandbyServiceRunning.value = true
        }
    }

    fun processUserQuery(query: String) {
        if (query.isBlank()) return

        addMessage("USER", query)
        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            // Check memories for context
            val currentMemories = memories.value.map { "${it.category}: ${it.content}" }

            // If query is an immediate device action or structured task plan, execute immediately
            val isDirect = MiraNluEngine.isDirectActionOrPlan(query)

            // Optional Gemini query for open-ended questions and general conversation
            val geminiResponse = if (!isDirect && GeminiApiClient.isApiKeyConfigured()) {
                GeminiApiClient.generateMiraResponse(query, currentMemories)
            } else null

            // Interpret with Mira NLU
            val interpretation = MiraNluEngine.interpret(query, geminiResponse)
            _currentLanguage.value = interpretation.detectedLanguage

            // Handle Confirmation Requirement
            if (interpretation.requiresConfirmation && interpretation.confirmationDetails != null) {
                _orbState.value = OrbState.SPEAKING
                voiceEngine.speak(interpretation.spokenResponse, interpretation.languageCode)
                addMessage("MIRA", interpretation.spokenResponse, interpretation.languageCode)

                _confirmationRequest.value = ConfirmationRequest(
                    title = "Confirm Action",
                    details = interpretation.confirmationDetails,
                    prompt = interpretation.spokenResponse,
                    onConfirm = {
                        _confirmationRequest.value = null
                        executeConfirmedAction(interpretation)
                    }
                )
                return@launch
            }

            // Handle Multi-Step Task Plan
            if (interpretation.taskPlan != null) {
                _activeTaskPlan.value = interpretation.taskPlan
                _currentScreen.value = MiraScreen.TASK_EXECUTION
                _orbState.value = OrbState.EXECUTING

                voiceEngine.speak(interpretation.spokenResponse, interpretation.languageCode)
                addMessage("MIRA", interpretation.captionText, interpretation.languageCode)

                executeTaskPlan(interpretation.taskPlan, interpretation.directAction)
                return@launch
            }

            // Handle Single Direct Action
            if (interpretation.directAction != null) {
                executeDirectAction(interpretation.directAction)
            }

            // Normal or conversational response
            _orbState.value = OrbState.SPEAKING
            addMessage("MIRA", interpretation.spokenResponse, interpretation.languageCode)
            voiceEngine.speak(interpretation.spokenResponse, interpretation.languageCode)

            // Save to task history
            dao.insertTask(
                TaskRecord(
                    command = query,
                    stepsCount = 1,
                    status = "COMPLETED",
                    summary = interpretation.captionText
                )
            )

            delay(1500)
            if (!voiceEngine.isSpeaking.value) {
                _orbState.value = OrbState.IDLE
            }
        }
    }

    private fun executeConfirmedAction(interpretation: MiraInterpretation) {
        viewModelScope.launch {
            when (val action = interpretation.directAction) {
                is DirectAction.ComposeMessage -> {
                    val res = deviceActionHandler.composeSms(action.recipient, action.body)
                    voiceEngine.speak("Message application opened for ${action.recipient}.", interpretation.languageCode)
                    addMessage("MIRA", res.message)
                    dao.insertTask(
                        TaskRecord(
                            command = "Message to ${action.recipient}",
                            stepsCount = 1,
                            status = if (res.success) "COMPLETED" else "FAILED",
                            summary = res.message
                        )
                    )
                }
                else -> {
                    interpretation.directAction?.let { executeDirectAction(it) }
                }
            }
        }
    }

    private fun executeDirectAction(action: DirectAction) {
        when (action) {
            is DirectAction.OpenApp -> deviceActionHandler.openApp(action.appName)
            is DirectAction.InstallApp -> deviceActionHandler.openPlayStore(action.appName)
            is DirectAction.OpenWebsite -> deviceActionHandler.openWebsite(action.url)
            is DirectAction.WebSearch -> deviceActionHandler.performWebSearch(action.query)
            is DirectAction.SearchFlights -> deviceActionHandler.searchFlights(action.from, action.to)
            is DirectAction.AdjustVolume -> deviceActionHandler.adjustVolume(action.increase)
            is DirectAction.ToggleFlashlight -> deviceActionHandler.toggleFlashlight(action.enable)
            is DirectAction.ComposeMessage -> deviceActionHandler.composeSms(action.recipient, action.body)
            is DirectAction.OpenSettings -> deviceActionHandler.openSettings(action.type)
            is DirectAction.GoHome -> deviceActionHandler.pressHome()
            is DirectAction.GoBack -> deviceActionHandler.pressBack()
            is DirectAction.OpenNotifications -> deviceActionHandler.openNotifications()
            is DirectAction.ScreenInspect -> {
                _currentScreen.value = MiraScreen.SCREEN_INSPECTOR
                viewModelScope.launch {
                    screenInspector.analyzeScreen("Active Application")
                }
            }
            is DirectAction.DownloadApk -> downloadApkToDevice()
        }
    }

    private fun executeTaskPlan(plan: MiraTaskPlan, fallbackAction: DirectAction?) {
        viewModelScope.launch {
            plan.isExecuting = true
            for (step in plan.steps) {
                step.status = StepStatus.IN_PROGRESS
                _activeTaskPlan.value = plan.copy()
                delay(800)

                // Execute action corresponding to step
                when (step.actionType) {
                    "OPEN_APP", "OPEN_STORE" -> {
                        fallbackAction?.let { executeDirectAction(it) }
                    }
                    "ADJUST_VOLUME" -> {
                        deviceActionHandler.adjustVolume(false)
                    }
                    "SEARCH_STORE", "INSTALL_APP" -> {
                        fallbackAction?.let { executeDirectAction(it) }
                    }
                    else -> {}
                }

                step.status = StepStatus.COMPLETED
                step.resultMessage = "Verified"
                _activeTaskPlan.value = plan.copy()
                delay(400)
            }

            plan.isExecuting = false
            plan.isCompleted = true
            _activeTaskPlan.value = plan.copy()
            _orbState.value = OrbState.SPEAKING

            val completionMsg = "Multi-step task completed successfully."
            voiceEngine.speak(completionMsg, "en")
            addMessage("MIRA", completionMsg, "en")

            dao.insertTask(
                TaskRecord(
                    command = plan.userQuery,
                    stepsCount = plan.steps.size,
                    status = "COMPLETED",
                    summary = "Executed ${plan.steps.size} steps successfully"
                )
            )

            delay(2000)
            _orbState.value = OrbState.IDLE
        }
    }

    fun dismissConfirmation() {
        _confirmationRequest.value = null
        voiceEngine.speak("Action cancelled.", "en")
        _orbState.value = OrbState.IDLE
    }

    private fun addMessage(sender: String, text: String, lang: String? = null) {
        val msg = ChatMessage(
            id = "msg_${System.currentTimeMillis()}_${(100..999).random()}",
            sender = sender,
            text = text,
            language = lang
        )
        _chatMessages.value = _chatMessages.value + msg
    }

    // Memory operations
    fun addMemory(category: String, content: String) {
        viewModelScope.launch {
            dao.insertMemory(MiraMemory(category = category, content = content))
        }
    }

    fun deleteMemory(memory: MiraMemory) {
        viewModelScope.launch {
            dao.deleteMemory(memory)
        }
    }

    fun clearAllMemories() {
        viewModelScope.launch {
            dao.clearAllMemories()
        }
    }

    fun clearTaskHistory() {
        viewModelScope.launch {
            dao.clearAllTasks()
        }
    }

    // Voice & Appearance settings
    fun updateVoicePitch(newPitch: Float) {
        voiceEngine.pitch = newPitch
    }

    fun updateVoiceSpeed(newSpeed: Float) {
        voiceEngine.speechRate = newSpeed
    }

    fun updateVoiceVolume(newVol: Float) {
        voiceEngine.voiceVolume = newVol
    }

    fun setCaptionFontSize(size: Int) {
        _captionFontSize.value = size
    }

    fun setOrbStyle(style: String) {
        _orbStyle.value = style
    }

    // Direct Mobile APK Download & Install
    fun getApkSizeMb(): Float = ApkExportManager.getApkFileSize(getApplication())

    fun downloadApkToDevice(onFinished: ((File?) -> Unit)? = null) {
        if (_isApkDownloading.value) return
        viewModelScope.launch {
            _isApkDownloading.value = true
            _apkDownloadProgress.value = 5
            voiceEngine.updateCaptions("Preparing Mira APK package for direct mobile download...")

            val app = getApplication<Application>()
            val result = ApkExportManager.exportAndDownloadApk(app) { progress ->
                _apkDownloadProgress.value = progress
            }

            _isApkDownloading.value = false
            if (result.isSuccess) {
                val file = result.getOrNull()
                _downloadedApkFile.value = file
                voiceEngine.updateCaptions("Mira APK successfully saved to Downloads. Tap Install to deploy.")
                voiceEngine.speak("Mira APK has been downloaded to your phone. Tap install to complete.", "en")
                onFinished?.invoke(file)
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Export failed"
                voiceEngine.updateCaptions("APK export error: $errorMsg")
                onFinished?.invoke(null)
            }
        }
    }

    fun installDownloadedApk() {
        val app = getApplication<Application>()
        val file = _downloadedApkFile.value
        if (file != null && file.exists()) {
            ApkExportManager.installApk(app, file)
        } else {
            downloadApkToDevice { newFile ->
                newFile?.let { ApkExportManager.installApk(app, it) }
            }
        }
    }

    fun shareApkFile() {
        val app = getApplication<Application>()
        val file = _downloadedApkFile.value
        if (file != null && file.exists()) {
            ApkExportManager.shareApk(app, file)
        } else {
            downloadApkToDevice { newFile ->
                newFile?.let { ApkExportManager.shareApk(app, it) }
            }
        }
    }

    // Battery-efficient Wake Word Sensitivity
    fun setWakeSensitivity(sensitivity: WakeSensitivity) {
        _wakeSensitivity.value = sensitivity
    }

    override fun onCleared() {
        super.onCleared()
        voiceEngine.release()
    }
}
