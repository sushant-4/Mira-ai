package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.service.MiraForegroundService
import com.example.ui.MiraScreen
import com.example.ui.MiraViewModel
import com.example.ui.components.FloatingMiraOverlay
import com.example.ui.components.MiraConfirmationDialog
import com.example.ui.screens.AppearanceScreen
import com.example.ui.screens.ApkDownloadScreen
import com.example.ui.screens.ConversationScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryScreen
import com.example.ui.screens.PermissionsCenterScreen
import com.example.ui.screens.ScreenInspectorScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TaskExecutionScreen
import com.example.ui.screens.TaskHistoryScreen
import com.example.ui.screens.VoiceSettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private val viewModel: MiraViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            MyApplicationTheme {
                MiraAppContent(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        if (intent?.getBooleanExtra(MiraForegroundService.EXTRA_TRIGGER_LISTENING, false) == true) {
            viewModel.onWakeWordDetected()
        }
    }
}

@Composable
fun MiraAppContent(viewModel: MiraViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val orbState by viewModel.orbState.collectAsState()
    val amplitude by viewModel.voiceEngine.audioAmplitude.collectAsState()
    val captionsText by viewModel.voiceEngine.liveCaptions.collectAsState()
    val isSpeaking by viewModel.voiceEngine.isSpeaking.collectAsState()
    val detectedLanguage by viewModel.currentLanguage.collectAsState()
    val confirmationRequest by viewModel.confirmationRequest.collectAsState()
    val isFloatingVisible by viewModel.isFloatingOverlayVisible.collectAsState()
    val isStandbyRunning by viewModel.isStandbyServiceRunning.collectAsState()
    val startupLog by viewModel.startupLog.collectAsState()
    val startupProgress by viewModel.startupProgress.collectAsState()
    val chatMessages by viewModel.chatMessages.collectAsState()
    val activeTaskPlan by viewModel.activeTaskPlan.collectAsState()
    val memories by viewModel.memories.collectAsState()
    val taskHistory by viewModel.taskHistory.collectAsState()
    val orbStyle by viewModel.orbStyle.collectAsState()
    val captionFontSize by viewModel.captionFontSize.collectAsState()
    val isWakeWordEnabled by viewModel.voiceEngine.isWakeWordEnabled.collectAsState()
    val isApkDownloading by viewModel.isApkDownloading.collectAsState()
    val apkDownloadProgress by viewModel.apkDownloadProgress.collectAsState()
    val downloadedApkFile by viewModel.downloadedApkFile.collectAsState()
    val wakeSensitivity by viewModel.wakeSensitivity.collectAsState()

    val batteryLevel = viewModel.deviceActionHandler.getBatteryInfo()

    Box(modifier = Modifier.fillMaxSize()) {
        when (currentScreen) {
            MiraScreen.SPLASH -> {
                SplashScreen(
                    startupLog = startupLog,
                    progress = startupProgress,
                    onSkip = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.HOME -> {
                HomeScreen(
                    orbState = orbState,
                    amplitude = amplitude,
                    captionsText = captionsText,
                    isSpeaking = isSpeaking,
                    detectedLanguage = detectedLanguage,
                    batteryLevel = batteryLevel,
                    onMicClick = { viewModel.toggleVoiceListening() },
                    onSubmitText = { viewModel.processUserQuery(it) },
                    onNavigate = { viewModel.navigateTo(it) },
                    onToggleFloating = { viewModel.toggleFloatingOverlay() }
                )
            }

            MiraScreen.CONVERSATION -> {
                ConversationScreen(
                    messages = chatMessages,
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) },
                    onSendMessage = { viewModel.processUserQuery(it) },
                    onReplaySpeech = { viewModel.voiceEngine.speak(it) }
                )
            }

            MiraScreen.TASK_EXECUTION -> {
                TaskExecutionScreen(
                    plan = activeTaskPlan,
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.PERMISSIONS_CENTER -> {
                val context = androidx.compose.ui.platform.LocalContext.current
                PermissionsCenterScreen(
                    isStandbyServiceRunning = isStandbyRunning,
                    onToggleStandbyService = {
                        viewModel.toggleStandbyService(context)
                    },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.VOICE_SETTINGS -> {
                VoiceSettingsScreen(
                    currentPitch = viewModel.voiceEngine.pitch,
                    currentSpeed = viewModel.voiceEngine.speechRate,
                    currentVolume = viewModel.voiceEngine.voiceVolume,
                    isWakeWordEnabled = isWakeWordEnabled,
                    onUpdatePitch = { viewModel.updateVoicePitch(it) },
                    onUpdateSpeed = { viewModel.updateVoiceSpeed(it) },
                    onUpdateVolume = { viewModel.updateVoiceVolume(it) },
                    onToggleWakeWord = { viewModel.voiceEngine.setWakeWordEnabled(it) },
                    onTestVoice = { viewModel.voiceEngine.speak(it) },
                    onOpenApkDeployment = { viewModel.navigateTo(MiraScreen.APK_DOWNLOAD) },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.APPEARANCE -> {
                AppearanceScreen(
                    currentStyle = orbStyle,
                    captionFontSize = captionFontSize,
                    onSelectStyle = { viewModel.setOrbStyle(it) },
                    onUpdateFontSize = { viewModel.setCaptionFontSize(it) },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.MEMORY -> {
                MemoryScreen(
                    memories = memories,
                    onAddMemory = { cat, content -> viewModel.addMemory(cat, content) },
                    onDeleteMemory = { viewModel.deleteMemory(it) },
                    onClearAll = { viewModel.clearAllMemories() },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.TASK_HISTORY -> {
                TaskHistoryScreen(
                    tasks = taskHistory,
                    onClearHistory = { viewModel.clearTaskHistory() },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.SCREEN_INSPECTOR -> {
                ScreenInspectorScreen(
                    inspector = viewModel.screenInspector,
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }

            MiraScreen.APK_DOWNLOAD -> {
                ApkDownloadScreen(
                    apkSizeMb = viewModel.getApkSizeMb(),
                    isDownloading = isApkDownloading,
                    downloadProgress = apkDownloadProgress,
                    downloadedFile = downloadedApkFile,
                    wakeSensitivity = wakeSensitivity,
                    onDownloadApk = { viewModel.downloadApkToDevice() },
                    onInstallApk = { viewModel.installDownloadedApk() },
                    onShareApk = { viewModel.shareApkFile() },
                    onSetWakeSensitivity = { viewModel.setWakeSensitivity(it) },
                    onAskWhoMadeYou = {
                        viewModel.navigateTo(MiraScreen.HOME)
                        viewModel.processUserQuery("Who made you?")
                    },
                    onBack = { viewModel.navigateTo(MiraScreen.HOME) }
                )
            }
        }

        // Floating Mira HUD (Overlay on top of any screen)
        FloatingMiraOverlay(
            visible = isFloatingVisible,
            orbState = orbState,
            captionsText = captionsText,
            amplitude = amplitude,
            onMicClick = { viewModel.toggleVoiceListening() },
            onDismiss = { viewModel.toggleFloatingOverlay() }
        )

        // Sensitive Action Confirmation Dialog (Security protocol)
        confirmationRequest?.let { req ->
            MiraConfirmationDialog(
                title = req.title,
                details = req.details,
                prompt = req.prompt,
                onConfirm = req.onConfirm,
                onDismiss = { viewModel.dismissConfirmation() }
            )
        }
    }
}
