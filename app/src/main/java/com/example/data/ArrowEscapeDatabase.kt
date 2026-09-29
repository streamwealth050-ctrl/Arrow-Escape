package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.LevelProgressDao
import com.example.data.model.LevelProgressEntity

@Database(entities = [LevelProgressEntity::class], version = 1, exportSchema = false)
abstract class ArrowEscapeDatabase : RoomDatabase() {
    abstract fun levelProgressDao(): LevelProgressDao

    companion object {
        @Volatile
        private var INSTANCE: ArrowEscapeDatabase? = null

        fun getInstance(context: Context): ArrowEscapeDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArrowEscapeDatabase::class.java,
                    "arrow_escape_db"
                ).fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
