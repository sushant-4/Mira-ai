package com.example.automation

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    FAILED,
    SKIPPED
}

data class TaskStep(
    val id: String,
    val title: String,
    val description: String,
    val actionType: String,
    var status: StepStatus = StepStatus.PENDING,
    var resultMessage: String? = null
)

data class MiraTaskPlan(
    val taskId: String,
    val userQuery: String,
    val steps: List<TaskStep>,
    val requiresConfirmation: Boolean = false,
    val confirmationPrompt: String? = null,
    var isExecuting: Boolean = false,
    var isCompleted: Boolean = false
)
