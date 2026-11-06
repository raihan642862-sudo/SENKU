package com.headshotaipro.trainer.domain.model

enum class HitZone {
    HEADSHOT,
    BODYSHOT,
    MISS
}

/**
 * Telemetry captured for every individual shot or drag-fire gesture in the arena.
 */
data class ShotTelemetry(
    val timestampMs: Long,
    val hitZone: HitZone,
    val reactionTimeMs: Long,
    val aimingOffsetPx: Float,
    val precisionScore: Float,       // 0..100
    val dragVelocityPxPerSec: Float, // Upward swipe velocity during fire
    val dragAngleDegrees: Float,     // Angle relative to vertical (0 deg = straight up)
    val targetDistanceDepth: Float   // 0.5 (close) to 1.5 (far) in 2.5D space
)

/**
 * Aggregated record of a completed or saved training session.
 */
data class TrainingSession(
    val id: Long = 0L,
    val timestampEpochMs: Long,
    val mode: TrainingMode,
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
)
