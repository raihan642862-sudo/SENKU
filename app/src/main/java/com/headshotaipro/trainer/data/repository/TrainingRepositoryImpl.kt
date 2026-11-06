package com.headshotaipro.trainer.data.repository

import com.headshotaipro.trainer.data.local.TrainingSessionDao
import com.headshotaipro.trainer.data.local.TrainingSessionEntity
import com.headshotaipro.trainer.domain.model.DeviceSpecs
import com.headshotaipro.trainer.domain.model.ProcessorTier
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession
import com.headshotaipro.trainer.domain.repository.TrainingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TrainingRepositoryImpl(
    private val dao: TrainingSessionDao
) : TrainingRepository {

    override fun observeAllSessions(): Flow<List<TrainingSession>> =
        dao.observeAllSessions().map { list -> list.map { it.toDomain() } }

    override fun observeSessionsByMode(mode: TrainingMode): Flow<List<TrainingSession>> =
        dao.observeSessionsByMode(mode.id).map { list -> list.map { it.toDomain() } }

    override suspend fun insertSession(session: TrainingSession): Long =
        dao.insertSession(TrainingSessionEntity.fromDomain(session))

    override suspend fun clearAllSessions() {
        dao.clearAll()
    }

    override suspend fun seedDemoSessionsIfEmpty() {
        if (dao.getSessionCount() > 0) return
        val now = System.currentTimeMillis()
        val hourMs = 3_600_000L
        val sampleEntities = listOf(
            TrainingSessionEntity(
                timestampEpochMs = now - 18 * hourMs,
                modeId = TrainingMode.BEGINNER.id,
                durationSec = 45,
                totalShots = 28,
                headshots = 12,
                bodyShots = 11,
                missedShots = 5,
                headshotAccuracyPercent = 42.8f,
                overallAccuracyPercent = 82.1f,
                avgReactionTimeMs = 465L,
                bestReactionTimeMs = 330L,
                aimingPrecisionScore = 68.5f,
                avgDragVelocityPxPerSec = 540f,
                avgDragAngleDeg = 18.4f,
                score = 2190,
                aiFeedbackSummary = "Solid beginner baseline. Increase upward drag velocity by 15% to convert upper-chest hits into red-number headshots."
            ),
            TrainingSessionEntity(
                timestampEpochMs = now - 14 * hourMs,
                modeId = TrainingMode.DRAG_HEADSHOT.id,
                durationSec = 60,
                totalShots = 36,
                headshots = 19,
                bodyShots = 12,
                missedShots = 5,
                headshotAccuracyPercent = 52.8f,
                overallAccuracyPercent = 86.1f,
                avgReactionTimeMs = 412L,
                bestReactionTimeMs = 285L,
                aimingPrecisionScore = 74.2f,
                avgDragVelocityPxPerSec = 685f,
                avgDragAngleDeg = 14.2f,
                score = 3420,
                aiFeedbackSummary = "Upward drag timing improved by +10% headshot rate. Keep vertical swipe angle within 12° on mid-range targets."
            ),
            TrainingSessionEntity(
                timestampEpochMs = now - 9 * hourMs,
                modeId = TrainingMode.MOVING_TARGET.id,
                durationSec = 60,
                totalShots = 34,
                headshots = 16,
                bodyShots = 11,
                missedShots = 7,
                headshotAccuracyPercent = 47.1f,
                overallAccuracyPercent = 79.4f,
                avgReactionTimeMs = 398L,
                bestReactionTimeMs = 270L,
                aimingPrecisionScore = 71.8f,
                avgDragVelocityPxPerSec = 720f,
                avgDragAngleDeg = 19.5f,
                score = 2980,
                aiFeedbackSummary = "Good lateral tracking on strafing targets. Use a J-curve drag toward the target's stride direction."
            ),
            TrainingSessionEntity(
                timestampEpochMs = now - 5 * hourMs,
                modeId = TrainingMode.REACTION_SPEED.id,
                durationSec = 45,
                totalShots = 30,
                headshots = 18,
                bodyShots = 8,
                missedShots = 4,
                headshotAccuracyPercent = 60.0f,
                overallAccuracyPercent = 86.7f,
                avgReactionTimeMs = 338L,
                bestReactionTimeMs = 215L,
                aimingPrecisionScore = 79.6f,
                avgDragVelocityPxPerSec = 760f,
                avgDragAngleDeg = 11.8f,
                score = 3690,
                aiFeedbackSummary = "Fast 338ms average reflex time! Pre-aiming at shoulder height yielded 60% headshots."
            ),
            TrainingSessionEntity(
                timestampEpochMs = now - 2 * hourMs,
                modeId = TrainingMode.ADVANCED.id,
                durationSec = 60,
                totalShots = 40,
                headshots = 26,
                bodyShots = 9,
                missedShots = 5,
                headshotAccuracyPercent = 65.0f,
                overallAccuracyPercent = 87.5f,
                avgReactionTimeMs = 324L,
                bestReactionTimeMs = 198L,
                aimingPrecisionScore = 84.3f,
                avgDragVelocityPxPerSec = 815f,
                avgDragAngleDeg = 9.4f,
                score = 4680,
                aiFeedbackSummary = "Tournament-level 65% headshot accuracy in Advanced Mode with tight 9.4° vertical drag discipline."
            )
        )
        dao.insertAll(sampleEntities)
    }

    override fun getPreloadedDevicePresets(): List<DeviceSpecs> = listOf(
        DeviceSpecs(
            phoneModel = "POCO X6 Pro 5G",
            processorName = "MediaTek Dimensity 8300-Ultra",
            processorTier = ProcessorTier.HIGH_END,
            ramGb = 12,
            screenWidthPx = 1220,
            screenHeightPx = 2712,
            screenDpi = 446,
            refreshRateHz = 120
        ),
        DeviceSpecs(
            phoneModel = "Redmi Note 13 Pro+",
            processorName = "MediaTek Dimensity 7200-Ultra",
            processorTier = ProcessorTier.MID_RANGE,
            ramGb = 8,
            screenWidthPx = 1220,
            screenHeightPx = 2712,
            screenDpi = 446,
            refreshRateHz = 120
        ),
        DeviceSpecs(
            phoneModel = "iQOO Neo 9 Pro",
            processorName = "Snapdragon 8 Gen 2",
            processorTier = ProcessorTier.HIGH_END,
            ramGb = 12,
            screenWidthPx = 1260,
            screenHeightPx = 2800,
            screenDpi = 453,
            refreshRateHz = 144
        ),
        DeviceSpecs(
            phoneModel = "Samsung Galaxy A55 5G",
            processorName = "Exynos 1480",
            processorTier = ProcessorTier.MID_RANGE,
            ramGb = 8,
            screenWidthPx = 1080,
            screenHeightPx = 2340,
            screenDpi = 390,
            refreshRateHz = 120
        ),
        DeviceSpecs(
            phoneModel = "Realme Narzo 70 Pro / Redmi 12",
            processorName = "MediaTek Dimensity 7050 / Helio G88",
            processorTier = ProcessorTier.ENTRY_LEVEL,
            ramGb = 6,
            screenWidthPx = 1080,
            screenHeightPx = 2400,
            screenDpi = 395,
            refreshRateHz = 90
        ),
        DeviceSpecs(
            phoneModel = "ASUS ROG Phone 8",
            processorName = "Snapdragon 8 Gen 3",
            processorTier = ProcessorTier.HIGH_END,
            ramGb = 16,
            screenWidthPx = 1080,
            screenHeightPx = 2400,
            screenDpi = 395,
            refreshRateHz = 165
        )
    )
}
