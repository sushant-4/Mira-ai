package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ai.GeminiApiClient
import com.example.ui.MiraScreen
import com.example.ui.components.LiveCaptionsGlassPanel
import com.example.ui.components.MiraAiOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.MiraCyanGlow
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraSuccessGreen
import com.example.ui.theme.MiraTextPrimary
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun HomeScreen(
    orbState: OrbState,
    amplitude: Float,
    captionsText: String,
    isSpeaking: Boolean,
    detectedLanguage: String,
    batteryLevel: String,
    onMicClick: () -> Unit,
    onSubmitText: (String) -> Unit,
    onNavigate: (MiraScreen) -> Unit,
    onToggleFloating: () -> Unit
) {
    var isTextMode by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    val isOnline = GeminiApiClient.isApiKeyConfigured()

    val quickCommands = listOf(
        "Who made you?",
        "Who created you?",
        "Download Mira APK",
        "Open YouTube",
        "Download Instagram",
        "Open YouTube, search relaxing music, play first video, volume down",
        "Message Mom that I'll be 20 minutes late",
        "Find flight from Kathmandu to Delhi",
        "Turn on Flashlight",
        "Volume down",
        "मिरा, युट्युब खोल।",
        "मिरा, कसले बनाएको?",
        "मिरा, किसने बनाया?",
        "Hey Mira, I'm bored.",
        "Inspect screen"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MiraDeepBlack, MiraNavyDark, MiraDeepBlack)
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Title and Online/Offline beacon
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "M I R A",
                            color = Color.White,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 4.sp
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        // Online/Offline Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isOnline) Color(0x3300F59B) else Color(0x33FFB703))
                                .border(1.dp, if (isOnline) MiraSuccessGreen else Color(0xFFFFB703), RoundedCornerShape(10.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = if (isOnline) "ONLINE AI" else "AUTONOMOUS",
                                color = if (isOnline) MiraSuccessGreen else Color(0xFFFFB703),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        text = "Battery $batteryLevel • Wake Word Ready",
                        color = MiraTextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Quick Navigation Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleFloating,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = "Floating HUD",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(MiraScreen.SCREEN_INSPECTOR) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Screen Inspector",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(MiraScreen.CONVERSATION) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat,
                            contentDescription = "Conversation",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(MiraScreen.PERMISSIONS_CENTER) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Permissions",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(MiraScreen.APK_DOWNLOAD) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Download APK",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = { onNavigate(MiraScreen.VOICE_SETTINGS) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = MiraCyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Scrollable Content Area for the Center Orb and Quick Prompts
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Central AI Orb / Core
                MiraAiOrb(
                    state = orbState,
                    amplitude = amplitude,
                    size = 230.dp,
                    onClick = onMicClick
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Orb State Status Banner
                Text(
                    text = when (orbState) {
                        OrbState.LISTENING -> "● LISTENING..."
                        OrbState.SPEAKING -> "✦ MIRA SPEAKING..."
                        OrbState.THINKING -> "◈ SYNTHESIZING COMMAND..."
                        OrbState.EXECUTING -> "⚙ EXECUTING MULTI-STEP TASK..."
                        OrbState.ERROR -> "⚠ ATTENTION REQUIRED"
                        else -> "✦ STANDBY • SAY \"HEY MIRA\""
                    },
                    color = when (orbState) {
                        OrbState.LISTENING -> MiraSuccessGreen
                        OrbState.SPEAKING -> MiraCyanNeon
                        OrbState.THINKING -> MiraVioletNeon
                        OrbState.EXECUTING -> MiraCyanNeon
                        OrbState.ERROR -> Color(0xFFFF3366)
                        else -> MiraCyanNeon
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )

                Text(
                    text = if (orbState == OrbState.LISTENING) "Speak clearly into the microphone" else "Tap orb or choose an action below",
                    color = MiraTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Secondary Navigation Pill Row (Memory, History, Appearance)
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2200F0FF))
                            .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp))
                            .clickable { onNavigate(MiraScreen.MEMORY) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Psychology, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Memory", color = MiraTextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2200F0FF))
                            .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp))
                            .clickable { onNavigate(MiraScreen.TASK_HISTORY) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Activity History", color = MiraTextPrimary, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x2200F0FF))
                            .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(12.dp))
                            .clickable { onNavigate(MiraScreen.APPEARANCE) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Settings, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Appearance", color = MiraTextPrimary, fontSize = 11.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Action Suggestion Chips
                Text(
                    text = "COMMAND SHORTCUTS",
                    color = MiraTextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    modifier = Modifier.align(Alignment.Start)
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    quickCommands.forEach { cmd ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0x1F1E2D4D))
                                .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(14.dp))
                                .clickable { onSubmitText(cmd) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = cmd,
                                color = MiraTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Live Captions Panel (Anchored above input controls)
            LiveCaptionsGlassPanel(
                captionsText = captionsText,
                isSpeaking = isSpeaking,
                detectedLanguage = detectedLanguage
            )

            // Bottom Control Area (Voice button or Text Input Field)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                if (isTextMode) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { isTextMode = false },
                            modifier = Modifier.size(44.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Switch to Voice",
                                tint = MiraCyanNeon
                            )
                        }

                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Command Mira (e.g. Open YouTube, download app)...", fontSize = 13.sp, color = MiraTextSecondary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MiraCyanNeon,
                                unfocusedBorderColor = Color(0x4400F0FF),
                                focusedTextColor = MiraTextPrimary,
                                unfocusedTextColor = MiraTextPrimary,
                                focusedContainerColor = Color(0x330F1A34),
                                unfocusedContainerColor = Color(0x220F1A34)
                            )
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        IconButton(
                            onClick = {
                                if (textInput.isNotBlank()) {
                                    onSubmitText(textInput)
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(MiraCyanNeon)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = "Send",
                                tint = MiraDeepBlack,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = { isTextMode = true },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x2200F0FF))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Keyboard,
                                contentDescription = "Switch to Text",
                                tint = MiraCyanNeon
                            )
                        }

                        // Big Central Glowing Mic Button
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .shadow(
                                    elevation = if (orbState == OrbState.LISTENING) 20.dp else 8.dp,
                                    shape = CircleShape,
                                    ambientColor = MiraCyanNeon,
                                    spotColor = MiraVioletNeon
                                )
                                .clip(CircleShape)
                                .background(
                                    if (orbState == OrbState.LISTENING)
                                        Brush.linearGradient(listOf(MiraSuccessGreen, MiraCyanNeon))
                                    else
                                        Brush.linearGradient(listOf(MiraCyanNeon, MiraVioletNeon))
                                )
                                .clickable { onMicClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Activate Voice",
                                tint = MiraDeepBlack,
                                modifier = Modifier.size(30.dp)
                            )
                        }

                        IconButton(
                            onClick = { onNavigate(MiraScreen.CONVERSATION) },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0x2200F0FF))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Chat,
                                contentDescription = "Open Chat Feed",
                                tint = MiraCyanNeon
                            )
                        }
                    }
                }
            }
        }
    }
}
