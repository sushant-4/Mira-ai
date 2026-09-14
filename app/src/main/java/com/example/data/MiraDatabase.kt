package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.MiraDao
import com.example.data.model.MiraMemory
import com.example.data.model.TaskRecord

@Database(entities = [MiraMemory::class, TaskRecord::class], version = 1, exportSchema = false)
abstract class MiraDatabase : RoomDatabase() {
    abstract fun miraDao(): MiraDao

    companion object {
        @Volatile
        private var INSTANCE: MiraDatabase? = null

        fun getDatabase(context: Context): MiraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MiraDatabase::class.java,
                    "mira_assistant.db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
