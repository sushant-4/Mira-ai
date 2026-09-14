package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.MiraCyanGlow
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraTextPrimary
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun LiveCaptionsGlassPanel(
    captionsText: String,
    isSpeaking: Boolean,
    detectedLanguage: String = "English",
    fontSize: TextUnit = 16.sp,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = captionsText.isNotBlank(),
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = MiraCyanNeon,
                    spotColor = MiraVioletNeon
                )
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MiraNavyDark.copy(alpha = 0.92f),
                            MiraDeepBlack.copy(alpha = 0.96f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MiraCyanNeon.copy(alpha = 0.6f),
                            MiraVioletNeon.copy(alpha = 0.4f),
                            MiraCyanNeon.copy(alpha = 0.2f)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(16.dp)
        ) {
            Column {
                // Header badge row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Glowing indicator pill
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isSpeaking) MiraCyanNeon else MiraVioletNeon)
                            .shadow(4.dp, shape = CircleShape, ambientColor = MiraCyanNeon)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = if (isSpeaking) "MIRA SPEAKING" else "CAPTIONS",
                        color = MiraCyanNeon,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // Language indicator tag
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0x3300F0FF))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = detectedLanguage.uppercase(),
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Voice Indicator",
                        tint = if (isSpeaking) MiraCyanNeon else MiraTextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Streaming / progressively displayed text
                Text(
                    text = captionsText,
                    color = MiraTextPrimary,
                    fontSize = fontSize,
                    fontWeight = FontWeight.Medium,
                    lineHeight = (fontSize.value * 1.4f).sp,
                    fontFamily = FontFamily.SansSerif,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
