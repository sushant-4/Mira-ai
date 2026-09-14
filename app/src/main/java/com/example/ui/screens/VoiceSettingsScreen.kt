package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraSuccessGreen
import com.example.ui.theme.MiraTextPrimary
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun VoiceSettingsScreen(
    currentPitch: Float,
    currentSpeed: Float,
    currentVolume: Float,
    isWakeWordEnabled: Boolean,
    onUpdatePitch: (Float) -> Unit,
    onUpdateSpeed: (Float) -> Unit,
    onUpdateVolume: (Float) -> Unit,
    onToggleWakeWord: (Boolean) -> Unit,
    onTestVoice: (String) -> Unit,
    onOpenApkDeployment: (() -> Unit)? = null,
    onBack: () -> Unit
) {
    var pitch by remember { mutableFloatStateOf(currentPitch) }
    var speed by remember { mutableFloatStateOf(currentSpeed) }
    var volume by remember { mutableFloatStateOf(currentVolume) }
    var wakeWord by remember { mutableStateOf(isWakeWordEnabled) }

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
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MiraCyanNeon
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column {
                    Text(
                        text = "VOICE & SYNTHESIS CALIBRATION",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Customize Mira's natural female acoustic profile",
                        color = MiraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wake word switch card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F1A34))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "\"HEY MIRA\" WAKE-WORD",
                            color = MiraCyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Respond automatically when 'Hey Mira' is spoken nearby.",
                            color = MiraTextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }

                    Switch(
                        checked = wakeWord,
                        onCheckedChange = {
                            wakeWord = it
                            onToggleWakeWord(it)
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MiraDeepBlack,
                            checkedTrackColor = MiraCyanNeon,
                            uncheckedTrackColor = Color(0x3364748B)
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x2200F0FF))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Battery Saver Active: 16kHz low-duty energy sampling consumes <0.5% battery/hr.",
                        color = MiraCyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Sliders Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x1F141E33))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    // Pitch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Voice Pitch (Female Intonation)", color = MiraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text = "%.2fx".format(pitch), color = MiraCyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = pitch,
                        onValueChange = {
                            pitch = it
                            onUpdatePitch(it)
                        },
                        valueRange = 0.8f..1.6f,
                        colors = SliderDefaults.colors(
                            thumbColor = MiraCyanNeon,
                            activeTrackColor = MiraCyanNeon,
                            inactiveTrackColor = Color(0x3300F0FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Speed
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Speed, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Speech Cadence (Speed)", color = MiraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text = "%.2fx".format(speed), color = MiraCyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = speed,
                        onValueChange = {
                            speed = it
                            onUpdateSpeed(it)
                        },
                        valueRange = 0.7f..1.4f,
                        colors = SliderDefaults.colors(
                            thumbColor = MiraCyanNeon,
                            activeTrackColor = MiraCyanNeon,
                            inactiveTrackColor = Color(0x3300F0FF)
                        )
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Volume
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Synthesizer Output Volume", color = MiraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text(text = "${(volume * 100).toInt()}%", color = MiraCyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = volume,
                        onValueChange = {
                            volume = it
                            onUpdateVolume(it)
                        },
                        valueRange = 0.2f..1.0f,
                        colors = SliderDefaults.colors(
                            thumbColor = MiraCyanNeon,
                            activeTrackColor = MiraCyanNeon,
                            inactiveTrackColor = Color(0x3300F0FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Test Buttons Row
            Text(
                text = "ACOUSTIC PREVIEW SAMPLES",
                color = MiraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        onTestVoice("Hello! I am Mira, your personal AI companion. All vocal synthesizers are running smoothly.")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x3300F0FF), contentColor = MiraCyanNeon),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("English Test", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = {
                        onTestVoice("नमस्ते! म मिरा हुँ, तपाईंको व्यक्तिगत एआई सहायक।")
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0x339D4EDD), contentColor = Color.White),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Nepali Test", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    onTestVoice("नमस्ते! मैं मीरा हूँ, आपकी व्यक्तिगत एआई साथी।")
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0x2200F0FF), contentColor = MiraCyanNeon),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Hindi Acoustic Test", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }

            if (onOpenApkDeployment != null) {
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F1B36))
                        .border(1.dp, Color(0x4400F0FF), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "DIRECT MOBILE APK EXPORT",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Download Mira-AI-Assistant.apk directly to phone storage for offline installation.",
                                    color = MiraTextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = onOpenApkDeployment,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MiraCyanNeon,
                                contentColor = MiraDeepBlack
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Download APK & Always-On Manager ➔", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
