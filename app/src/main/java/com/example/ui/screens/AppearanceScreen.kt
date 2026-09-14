package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import com.example.ui.components.LiveCaptionsGlassPanel
import com.example.ui.components.MiraAiOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraTextPrimary
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun AppearanceScreen(
    currentStyle: String,
    captionFontSize: Int,
    onSelectStyle: (String) -> Unit,
    onUpdateFontSize: (Int) -> Unit,
    onBack: () -> Unit
) {
    val styles = listOf("Quantum Core", "Cyber Vortex", "Neon Pulse")
    var fontSizeSlider by remember { mutableFloatStateOf(captionFontSize.toFloat()) }

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
                        text = "FUTURISTIC VISUAL APPEARANCE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Customize 3D AI orb aesthetics & glass HUD styling",
                        color = MiraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Orb Style Selector
            Text(
                text = "AI CORE GRAPHICS PRESET",
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
                styles.forEach { style ->
                    val isSelected = style == currentStyle
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) Color(0x3300F0FF) else Color(0x1F141E33))
                            .border(
                                1.dp,
                                if (isSelected) MiraCyanNeon else Color(0x2200F0FF),
                                RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectStyle(style) }
                            .padding(vertical = 12.dp, horizontal = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = style,
                                color = if (isSelected) MiraCyanNeon else MiraTextPrimary,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                            if (isSelected) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = MiraCyanNeon,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Live Orb Preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0A1128))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(20.dp))
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LIVE GRAPHICS PREVIEW",
                        color = MiraCyanNeon,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    MiraAiOrb(
                        state = OrbState.SPEAKING,
                        amplitude = 0.5f,
                        size = 140.dp
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Caption Font Size
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0x1F141E33))
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FormatSize, contentDescription = null, tint = MiraCyanNeon, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Live Caption Font Scale", color = MiraTextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                        Text("${fontSizeSlider.toInt()} sp", color = MiraCyanNeon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = fontSizeSlider,
                        onValueChange = {
                            fontSizeSlider = it
                            onUpdateFontSize(it.toInt())
                        },
                        valueRange = 12f..24f,
                        colors = SliderDefaults.colors(
                            thumbColor = MiraCyanNeon,
                            activeTrackColor = MiraCyanNeon,
                            inactiveTrackColor = Color(0x3300F0FF)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Captions Preview Panel
            Text(
                text = "LIVE CAPTION HUD SAMPLE",
                color = MiraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            LiveCaptionsGlassPanel(
                captionsText = "Sure, I'll open YouTube and adjust your volume.",
                isSpeaking = true,
                detectedLanguage = "English",
                fontSize = fontSizeSlider.sp
            )
        }
    }
}
