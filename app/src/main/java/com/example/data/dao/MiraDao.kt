package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.MiraMemory
import com.example.data.model.TaskRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MiraDao {
    // Memories
    @Query("SELECT * FROM mira_memories ORDER BY timestamp DESC")
    fun getAllMemories(): Flow<List<MiraMemory>>

    @Query("SELECT * FROM mira_memories WHERE content LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchMemories(query: String): Flow<List<MiraMemory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMemory(memory: MiraMemory): Long

    @Delete
    suspend fun deleteMemory(memory: MiraMemory)

    @Query("DELETE FROM mira_memories")
    suspend fun clearAllMemories()

    // Task History
    @Query("SELECT * FROM task_history ORDER BY timestamp DESC")
    fun getAllTasks(): Flow<List<TaskRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskRecord): Long

    @Query("DELETE FROM task_history")
    suspend fun clearAllTasks()
}
