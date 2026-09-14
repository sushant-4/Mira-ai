package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val MiraDarkColorScheme = darkColorScheme(
    primary = MiraCyanNeon,
    onPrimary = MiraDeepBlack,
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = MiraCyanNeon,
    secondary = MiraElectricBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF1E3A8A),
    onSecondaryContainer = Color(0xFF93C5FD),
    tertiary = MiraVioletNeon,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF4C1D95),
    onTertiaryContainer = Color(0xFFDDD6FE),
    background = MiraDeepBlack,
    onBackground = MiraTextPrimary,
    surface = MiraNavyDark,
    onSurface = MiraTextPrimary,
    surfaceVariant = MiraCardDark,
    onSurfaceVariant = MiraTextSecondary,
    outline = MiraGlassBorder,
    outlineVariant = Color(0x3300F0FF),
    error = MiraErrorRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = MiraDarkColorScheme,
        typography = Typography,
        content = content
    )
}
