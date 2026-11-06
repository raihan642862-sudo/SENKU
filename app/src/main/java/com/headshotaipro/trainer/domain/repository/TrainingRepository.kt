package com.headshotaipro.trainer.domain.repository

import com.headshotaipro.trainer.domain.model.DeviceSpecs
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession
import kotlinx.coroutines.flow.Flow

interface TrainingRepository {
    fun observeAllSessions(): Flow<List<TrainingSession>>
    fun observeSessionsByMode(mode: TrainingMode): Flow<List<TrainingSession>>
    suspend fun insertSession(session: TrainingSession): Long
    suspend fun clearAllSessions()
    suspend fun seedDemoSessionsIfEmpty()
    fun getPreloadedDevicePresets(): List<DeviceSpecs>
}
