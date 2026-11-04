package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.LevelProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getProgressForLevel(levelNumber: Int): LevelProgressEntity?

    @Query("SELECT MAX(levelNumber) FROM level_progress WHERE completed = 1")
    fun getMaxCompletedLevel(): Flow<Int?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProgress(progress: LevelProgressEntity)

    @Query("SELECT * FROM level_progress WHERE levelNumber < 1000 ORDER BY levelNumber ASC")
    suspend fun getRegularProgressSync(): List<LevelProgressEntity>

    @Query("DELETE FROM level_progress WHERE levelNumber >= 1000")
    suspend fun deleteCorruptedDailyLevels()

    @Query("DELETE FROM level_progress")
    suspend fun clearAllProgress()
}
