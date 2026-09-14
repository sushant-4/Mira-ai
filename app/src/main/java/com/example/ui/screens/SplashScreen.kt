package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.MiraAiOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraSuccessGreen
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun SplashScreen(
    startupLog: String,
    progress: Int = 100,
    onSkip: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

    val counterSpinAngle by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "counter_spin_angle"
    )

    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    val animatedProgress by animateFloatAsState(
        targetValue = (progress.coerceIn(0, 100) / 100f),
        animationSpec = tween(100, easing = LinearEasing),
        label = "animated_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(MiraDeepBlack, MiraNavyDark, MiraDeepBlack)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Central Glowing Orb with Dynamic Radial Progress Ring
            Box(contentAlignment = Alignment.Center) {
                // Outer rotating ring
                Box(
                    modifier = Modifier
                        .size(240.dp)
                        .rotate(spinAngle)
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(MiraCyanNeon, Color.Transparent, MiraVioletNeon, Color.Transparent)
                            ),
                            shape = CircleShape
                        )
                )

                // Secondary counter-rotating dashed aura
                Box(
                    modifier = Modifier
                        .size(210.dp)
                        .rotate(counterSpinAngle)
                        .border(
                            width = 1.dp,
                            brush = Brush.sweepGradient(
                                listOf(Color.Transparent, MiraCyanNeon.copy(alpha = 0.5f), Color.Transparent, MiraVioletNeon.copy(alpha = 0.6f))
                            ),
                            shape = CircleShape
                        )
                )

                // Radial Progress Indicator around Orb
                CircularProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier.size(225.dp),
                    color = MiraCyanNeon,
                    trackColor = Color(0x2200F0FF),
                    strokeWidth = 3.dp
                )

                MiraAiOrb(
                    state = OrbState.THINKING,
                    amplitude = 0.65f * pulseGlow,
                    size = 170.dp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Mira Title with futuristic letterspacing
            Text(
                text = "M I R A",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 8.sp,
                fontFamily = FontFamily.SansSerif,
                modifier = Modifier.shadow(16.dp, ambientColor = MiraCyanNeon)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "JARVIS-INSPIRED AI MOBILE COMPANION",
                color = MiraCyanNeon,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Percentage Counter Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0x2A00F0FF))
                    .border(1.dp, MiraCyanNeon.copy(alpha = 0.8f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 24.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "[ $progress% ]",
                    color = MiraCyanNeon,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Cybernetic telemetry status box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0x3300F0FF))
                    .border(1.dp, Color(0x6600F0FF), RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp, vertical = 12.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "✦ $startupLog",
                        color = MiraCyanNeon,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MiraCyanNeon,
                        trackColor = Color(0x3300F0FF)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // System Initialization Micro-Checklist
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                CheckItem(label = "Neural Core", isDone = progress >= 15)
                CheckItem(label = "Voice Synth", isDone = progress >= 38)
                CheckItem(label = "OS Link", isDone = progress >= 62)
                CheckItem(label = "Standby", isDone = progress >= 96)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0x2200F0FF),
                    contentColor = MiraTextSecondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Enter Assistant", fontSize = 12.sp, color = MiraCyanNeon)
            }
        }
    }
}

@Composable
private fun CheckItem(label: String, isDone: Boolean) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(if (isDone) MiraSuccessGreen.copy(alpha = 0.2f) else Color(0x22FFFFFF))
                .border(
                    1.dp,
                    if (isDone) MiraSuccessGreen else Color(0x44FFFFFF),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MiraSuccessGreen,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isDone) MiraSuccessGreen else MiraTextSecondary,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            fontWeight = if (isDone) FontWeight.Bold else FontWeight.Normal
        )
    }
}

