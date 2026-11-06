package com.headshotaipro.trainer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession

@Entity(tableName = "training_sessions")
data class TrainingSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val modeId: String,
    val durationSec: Int,
    val totalShots: Int,
    val headshots: Int,
    val bodyShots: Int,
    val missedShots: Int,
    val headshotAccuracyPercent: Float,
    val overallAccuracyPercent: Float,
    val avgReactionTimeMs: Long,
    val bestReactionTimeMs: Long,
    val aimingPrecisionScore: Float,
    val avgDragVelocityPxPerSec: Float,
    val avgDragAngleDeg: Float,
    val score: Int,
    val aiFeedbackSummary: String
) {
    fun toDomain(): TrainingSession = TrainingSession(
        id = id,
        timestampEpochMs = timestampEpochMs,
        mode = TrainingMode.fromId(modeId),
        durationSec = durationSec,
        totalShots = totalShots,
        headshots = headshots,
        bodyShots = bodyShots,
        missedShots = missedShots,
        headshotAccuracyPercent = headshotAccuracyPercent,
        overallAccuracyPercent = overallAccuracyPercent,
        avgReactionTimeMs = avgReactionTimeMs,
        bestReactionTimeMs = bestReactionTimeMs,
        aimingPrecisionScore = aimingPrecisionScore,
        avgDragVelocityPxPerSec = avgDragVelocityPxPerSec,
        avgDragAngleDeg = avgDragAngleDeg,
        score = score,
        aiFeedbackSummary = aiFeedbackSummary
    )

    companion object {
        fun fromDomain(session: TrainingSession): TrainingSessionEntity = TrainingSessionEntity(
            id = session.id,
            timestampEpochMs = session.timestampEpochMs,
            modeId = session.mode.id,
            durationSec = session.durationSec,
            totalShots = session.totalShots,
            headshots = session.headshots,
            bodyShots = session.bodyShots,
            missedShots = session.missedShots,
            headshotAccuracyPercent = session.headshotAccuracyPercent,
            overallAccuracyPercent = session.overallAccuracyPercent,
            avgReactionTimeMs = session.avgReactionTimeMs,
            bestReactionTimeMs = session.bestReactionTimeMs,
            aimingPrecisionScore = session.aimingPrecisionScore,
            avgDragVelocityPxPerSec = session.avgDragVelocityPxPerSec,
            avgDragAngleDeg = session.avgDragAngleDeg,
            score = session.score,
            aiFeedbackSummary = session.aiFeedbackSummary
        )
    }
}
