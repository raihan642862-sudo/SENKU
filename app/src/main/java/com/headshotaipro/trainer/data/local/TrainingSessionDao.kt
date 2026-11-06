package com.headshotaipro.trainer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TrainingSessionDao {

    @Query("SELECT * FROM training_sessions ORDER BY timestampEpochMs DESC")
    fun observeAllSessions(): Flow<List<TrainingSessionEntity>>

    @Query("SELECT * FROM training_sessions WHERE modeId = :modeId ORDER BY timestampEpochMs DESC")
    fun observeSessionsByMode(modeId: String): Flow<List<TrainingSessionEntity>>

    @Query("SELECT COUNT(*) FROM training_sessions")
    suspend fun getSessionCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(entity: TrainingSessionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entities: List<TrainingSessionEntity>)

    @Query("DELETE FROM training_sessions")
    suspend fun clearAll()
}
