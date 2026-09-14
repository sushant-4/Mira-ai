package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mira_memories")
data class MiraMemory(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val category: String, // e.g. "Preference", "Fact", "Contact", "Routine"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
