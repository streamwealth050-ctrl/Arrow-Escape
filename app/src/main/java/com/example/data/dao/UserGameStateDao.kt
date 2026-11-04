package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.UserGameStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameStateDao {
    @Query("SELECT * FROM user_game_state WHERE id = 1 LIMIT 1")
    fun getUserGameState(): Flow<UserGameStateEntity?>

    @Query("SELECT * FROM user_game_state WHERE id = 1 LIMIT 1")
    suspend fun getUserGameStateSync(): UserGameStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserGameState(state: UserGameStateEntity)

    @Query("UPDATE user_game_state SET currentLevelNumber = :levelNumber, lastPlayedTimestamp = :timestamp WHERE id = 1")
    suspend fun updateCurrentLevel(levelNumber: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE user_game_state SET maxUnlockedLevel = :unlockedLevel WHERE id = 1")
    suspend fun updateMaxUnlockedLevel(unlockedLevel: Int)

    @Query("DELETE FROM user_game_state")
    suspend fun clearGameState()
}
