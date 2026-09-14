package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.MiraCyanGlow
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraElectricBlue
import com.example.ui.theme.MiraErrorRed
import com.example.ui.theme.MiraMagentaAccent
import com.example.ui.theme.MiraSuccessGreen
import com.example.ui.theme.MiraVioletNeon
import kotlin.math.cos
import kotlin.math.sin

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR
}

@Composable
fun MiraAiOrb(
    state: OrbState,
    amplitude: Float = 0.2f,
    modifier: Modifier = Modifier,
    size: Dp = 260.dp,
    onClick: () -> Unit = {}
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_infinite")

    // Continuous rotation for outer rings
    val rotationOuter by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_outer"
    )

    // Counter rotation for inner ring
    val rotationInner by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation_inner"
    )

    // Breathing pulse for idle state
    val idlePulse by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "idle_pulse"
    )

    // Fast ripple for listening / speaking
    val rippleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ripple_progress"
    )

    val interactionSource = remember { MutableInteractionSource() }

    Box(
        modifier = modifier
            .size(size)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)
            val baseRadius = (size.toPx() / 2f) * 0.72f

            // Dynamic scaling depending on amplitude and state
            val dynamicScale = when (state) {
                OrbState.IDLE -> idlePulse
                OrbState.LISTENING -> 1.0f + (amplitude * 0.45f)
                OrbState.SPEAKING -> 1.0f + (amplitude * 0.5f)
                OrbState.THINKING -> 1.05f
                OrbState.EXECUTING -> 1.02f
                OrbState.ERROR -> 0.98f
            }

            val currentCoreRadius = baseRadius * 0.52f * dynamicScale

            // Draw theme colors based on state
            val (primaryColor, secondaryColor, accentColor) = when (state) {
                OrbState.IDLE -> Triple(MiraCyanNeon, MiraElectricBlue, MiraVioletNeon)
                OrbState.LISTENING -> Triple(MiraCyanNeon, MiraSuccessGreen, MiraElectricBlue)
                OrbState.THINKING -> Triple(MiraVioletNeon, MiraMagentaAccent, MiraCyanNeon)
                OrbState.SPEAKING -> Triple(MiraCyanNeon, MiraVioletNeon, MiraMagentaAccent)
                OrbState.EXECUTING -> Triple(MiraSuccessGreen, MiraCyanNeon, MiraElectricBlue)
                OrbState.ERROR -> Triple(MiraErrorRed, Color(0xFFFF7B00), MiraErrorRed)
            }

            // 1. Ambient outer aura (soft radial bloom)
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = if (state == OrbState.ERROR) 0.35f else 0.28f),
                        secondaryColor.copy(alpha = 0.12f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * 1.35f
                ),
                radius = baseRadius * 1.35f,
                center = center
            )

            // 2. Outward Expanding Ripples (when listening or speaking)
            if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                val rippleRadius = currentCoreRadius + (baseRadius * 0.7f * rippleProgress)
                val rippleAlpha = (1f - rippleProgress).coerceIn(0f, 1f) * 0.45f
                drawCircle(
                    color = primaryColor.copy(alpha = rippleAlpha),
                    radius = rippleRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                val secondRipple = currentCoreRadius + (baseRadius * 0.7f * ((rippleProgress + 0.5f) % 1f))
                val secondAlpha = (1f - ((rippleProgress + 0.5f) % 1f)).coerceIn(0f, 1f) * 0.3f
                drawCircle(
                    color = accentColor.copy(alpha = secondAlpha),
                    radius = secondRipple,
                    center = center,
                    style = Stroke(width = 1.5.dp.toPx())
                )
            }

            // 3. Cybernetic Orbital Dashed Rings
            val ringRadius1 = baseRadius * 0.85f
            drawOuterRing(
                center = center,
                radius = ringRadius1,
                rotation = rotationOuter,
                color = primaryColor.copy(alpha = 0.55f),
                dashLength = 40f,
                gapLength = 20f,
                strokeWidth = 2.dp.toPx()
            )

            val ringRadius2 = baseRadius * 0.70f
            drawOuterRing(
                center = center,
                radius = ringRadius2,
                rotation = rotationInner,
                color = accentColor.copy(alpha = 0.5f),
                dashLength = 25f,
                gapLength = 35f,
                strokeWidth = 1.5.dp.toPx()
            )

            // 4. Revolving Quantum Particle Nodes
            val particleCount = if (state == OrbState.THINKING) 8 else 5
            val angleStep = 360f / particleCount
            for (i in 0 until particleCount) {
                val currentAngle = (rotationOuter * (if (i % 2 == 0) 1.2f else -0.8f) + (i * angleStep)) * (Math.PI / 180.0)
                val pRadius = ringRadius1 + (if (i % 2 == 0) 12f else -12f)
                val px = center.x + (pRadius * cos(currentAngle)).toFloat()
                val py = center.y + (pRadius * sin(currentAngle)).toFloat()

                // Particle node glow
                drawCircle(
                    color = if (i % 2 == 0) primaryColor else accentColor,
                    radius = 3.5.dp.toPx(),
                    center = Offset(px, py)
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    radius = 1.5.dp.toPx(),
                    center = Offset(px, py)
                )
            }

            // 5. Central 3D Glowing Core Sphere
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.95f),
                        primaryColor,
                        secondaryColor,
                        primaryColor.copy(alpha = 0.2f)
                    ),
                    center = Offset(center.x - currentCoreRadius * 0.25f, center.y - currentCoreRadius * 0.25f),
                    radius = currentCoreRadius * 1.1f
                ),
                radius = currentCoreRadius,
                center = center
            )

            // 6. Inner Core Holographic Grid Lines / Highlights
            drawCircle(
                color = Color.White.copy(alpha = 0.45f),
                radius = currentCoreRadius * 0.92f,
                center = center,
                style = Stroke(width = 1.5.dp.toPx())
            )

            // 7. Dynamic Audio Visualizer Bars inside Core (when speaking or listening)
            if (state == OrbState.SPEAKING || state == OrbState.LISTENING) {
                val barCount = 7
                val totalWidth = currentCoreRadius * 0.9f
                val barWidth = totalWidth / (barCount * 1.8f)
                val startX = center.x - (totalWidth / 2f)

                for (b in 0 until barCount) {
                    val factor = kotlin.math.sin((b + 1) * 0.7f + amplitude * 4f)
                    val barHeight = (currentCoreRadius * 0.65f * (0.25f + 0.75f * kotlin.math.abs(factor))).coerceIn(6f, currentCoreRadius * 0.8f)
                    val bx = startX + (b * barWidth * 1.8f)

                    drawLine(
                        color = Color.White.copy(alpha = 0.9f),
                        start = Offset(bx, center.y - barHeight / 2f),
                        end = Offset(bx, center.y + barHeight / 2f),
                        strokeWidth = barWidth,
                        cap = StrokeCap.Round
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawOuterRing(
    center: Offset,
    radius: Float,
    rotation: Float,
    color: Color,
    dashLength: Float,
    gapLength: Float,
    strokeWidth: Float
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center,
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLength, gapLength), rotation)
        )
    )
}
