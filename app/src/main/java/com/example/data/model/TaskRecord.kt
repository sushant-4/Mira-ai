package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "task_history")
data class TaskRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val command: String,
    val stepsCount: Int,
    val status: String, // "COMPLETED", "CANCELLED", "FAILED"
    val summary: String,
    val timestamp: Long = System.currentTimeMillis()
)
