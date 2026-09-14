package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.automation.MiraTaskPlan
import com.example.automation.StepStatus
import com.example.ui.components.MiraAiOrb
import com.example.ui.components.OrbState
import com.example.ui.theme.MiraCyanNeon
import com.example.ui.theme.MiraDeepBlack
import com.example.ui.theme.MiraErrorRed
import com.example.ui.theme.MiraNavyDark
import com.example.ui.theme.MiraSuccessGreen
import com.example.ui.theme.MiraTextPrimary
import com.example.ui.theme.MiraTextSecondary
import com.example.ui.theme.MiraVioletNeon

@Composable
fun TaskExecutionScreen(
    plan: MiraTaskPlan?,
    onBack: () -> Unit
) {
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
                        text = "TASK EXECUTION ENGINE",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Multi-step autonomous orchestration",
                        color = MiraTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }

            if (plan == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No active multi-step task in queue", color = MiraTextSecondary)
                }
                return@Column
            }

            val completedCount = plan.steps.count { it.status == StepStatus.COMPLETED }
            val totalSteps = plan.steps.size
            val progress = if (totalSteps > 0) completedCount.toFloat() / totalSteps.toFloat() else 0f

            // Top Status Hero Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0F1A34), Color(0xFF162544))
                        )
                    )
                    .border(1.dp, Color(0x3300F0FF), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MiraAiOrb(
                        state = if (plan.isCompleted) OrbState.IDLE else OrbState.EXECUTING,
                        amplitude = 0.4f,
                        size = 64.dp
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (plan.isCompleted) "SEQUENCE COMPLETED" else "EXECUTING SUBTASKS ($completedCount / $totalSteps)",
                            color = if (plan.isCompleted) MiraSuccessGreen else MiraCyanNeon,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = plan.userQuery,
                            color = MiraTextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = if (plan.isCompleted) MiraSuccessGreen else MiraCyanNeon,
                            trackColor = Color(0x3300F0FF),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "ACTION PLAN PIPELINE",
                color = MiraTextSecondary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Step timeline
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(plan.steps) { index, step ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                when (step.status) {
                                    StepStatus.COMPLETED -> Color(0x1F00F59B)
                                    StepStatus.IN_PROGRESS -> Color(0x2200F0FF)
                                    StepStatus.FAILED -> Color(0x22FF3366)
                                    else -> Color(0x1A141E33)
                                }
                            )
                            .border(
                                width = 1.dp,
                                color = when (step.status) {
                                    StepStatus.COMPLETED -> MiraSuccessGreen.copy(alpha = 0.4f)
                                    StepStatus.IN_PROGRESS -> MiraCyanNeon
                                    StepStatus.FAILED -> MiraErrorRed
                                    else -> Color(0x2200F0FF)
                                },
                                shape = RoundedCornerShape(16.dp)
                            )
                            .padding(14.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Step Status Icon / Badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (step.status) {
                                            StepStatus.COMPLETED -> MiraSuccessGreen
                                            StepStatus.IN_PROGRESS -> MiraCyanNeon
                                            StepStatus.FAILED -> MiraErrorRed
                                            else -> Color(0x3364748B)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                when (step.status) {
                                    StepStatus.COMPLETED -> Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MiraDeepBlack,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    StepStatus.IN_PROGRESS -> CircularProgressIndicator(
                                        color = MiraDeepBlack,
                                        strokeWidth = 2.5.dp,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    StepStatus.FAILED -> Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    else -> Text(
                                        text = "${index + 1}",
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "STEP ${index + 1}: ${step.title.uppercase()}",
                                        color = when (step.status) {
                                            StepStatus.COMPLETED -> MiraSuccessGreen
                                            StepStatus.IN_PROGRESS -> MiraCyanNeon
                                            else -> MiraTextSecondary
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    )

                                    Text(
                                        text = step.status.name,
                                        color = when (step.status) {
                                            StepStatus.COMPLETED -> MiraSuccessGreen
                                            StepStatus.IN_PROGRESS -> MiraCyanNeon
                                            else -> MiraTextSecondary
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = step.description,
                                    color = MiraTextPrimary,
                                    fontSize = 13.sp
                                )

                                if (step.resultMessage != null) {
                                    Text(
                                        text = "Result: ${step.resultMessage}",
                                        color = MiraSuccessGreen,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onBack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MiraCyanNeon,
                    contentColor = MiraDeepBlack
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = if (plan.isCompleted) "Return to Home Hub" else "Dismiss to Background",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
