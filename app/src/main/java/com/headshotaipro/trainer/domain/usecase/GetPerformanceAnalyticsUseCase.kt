package com.headshotaipro.trainer.domain.usecase

import com.headshotaipro.trainer.domain.model.ModePerformanceStat
import com.headshotaipro.trainer.domain.model.PerformanceAnalyticsSummary
import com.headshotaipro.trainer.domain.model.SessionTrendPoint
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Computes comprehensive AI Performance Analytics, chart series, skill rating tiers,
 * and personalized improvement suggestions across all stored Room training sessions.
 */
class GetPerformanceAnalyticsUseCase {

    operator fun invoke(sessions: List<TrainingSession>): PerformanceAnalyticsSummary {
        if (sessions.isEmpty()) {
            return PerformanceAnalyticsSummary(
                totalSessions = 0,
                totalShotsFired = 0,
                totalHeadshots = 0,
                totalBodyShots = 0,
                totalMissedShots = 0,
                overallHeadshotPercent = 0f,
                overallHitAccuracyPercent = 0f,
                avgReactionTimeMs = 0L,
                bestReactionTimeMs = 0L,
                avgAimingPrecision = 0f,
                currentSkillRating = "UNRANKED RECRUIT",
                trendPoints = emptyList(),
                modeBreakdown = TrainingMode.entries.map { mode ->
                    ModePerformanceStat(mode, 0, 0f, 0L, 0)
                },
                aiImprovementSuggestions = listOf(
                    "Complete your first session in Drag Headshot Mode to calibrate your baseline AI profile.",
                    "Open Sensitivity AI to generate a custom 0–200 sensitivity loadout for your phone hardware."
                )
            )
        }

        val totalSessions = sessions.size
        val totalShots = sessions.sumOf { it.totalShots }.coerceAtLeast(1)
        val totalHeadshots = sessions.sumOf { it.headshots }
        val totalBodyShots = sessions.sumOf { it.bodyShots }
        val totalMisses = sessions.sumOf { it.missedShots }

        val overallHsPercent = (totalHeadshots * 100f) / totalShots
        val overallHitPercent = ((totalHeadshots + totalBodyShots) * 100f) / totalShots

        val validReactions = sessions.map { it.avgReactionTimeMs }.filter { it > 0L }
        val avgReaction = if (validReactions.isNotEmpty()) validReactions.average().toLong() else 0L
        val bestReaction = sessions.map { it.bestReactionTimeMs }.filter { it > 0L }.minOrNull() ?: avgReaction
        val avgPrecision = sessions.map { it.aimingPrecisionScore }.average().toFloat()

        val rating = when {
            overallHsPercent >= 72f && avgReaction in 1L..330L -> "GRANDMASTER ONE-TAP AI"
            overallHsPercent >= 58f && avgReaction in 1L..410L -> "HEROIC DRAG SPECIALIST"
            overallHsPercent >= 44f -> "DIAMOND SHARPSHOOTER"
            overallHsPercent >= 30f -> "PLATINUM STRIKER"
            else -> "GOLD RISING CONTENDER"
        }

        val chronological = sessions.sortedBy { it.timestampEpochMs }.takeLast(12)
        val formatter = SimpleDateFormat("MM/dd HH:mm", Locale.US)
        val trendPoints = chronological.mapIndexed { idx, session ->
            SessionTrendPoint(
                sessionIndex = idx + 1,
                label = "S${idx + 1} (${formatter.format(Date(session.timestampEpochMs))})",
                headshotPercent = session.headshotAccuracyPercent,
                precisionScore = session.aimingPrecisionScore,
                reactionMs = session.avgReactionTimeMs
            )
        }

        val modeStats = TrainingMode.entries.map { mode ->
            val modeSessions = sessions.filter { it.mode == mode }
            if (modeSessions.isEmpty()) {
                ModePerformanceStat(mode, 0, 0f, 0L, 0)
            } else {
                val mShots = modeSessions.sumOf { it.totalShots }.coerceAtLeast(1)
                val mHs = modeSessions.sumOf { it.headshots }
                val mReactions = modeSessions.map { it.avgReactionTimeMs }.filter { it > 0L }
                ModePerformanceStat(
                    mode = mode,
                    sessionsCount = modeSessions.size,
                    avgHeadshotPercent = (mHs * 100f) / mShots,
                    avgReactionMs = if (mReactions.isNotEmpty()) mReactions.average().toLong() else 0L,
                    bestScore = modeSessions.maxOf { it.score }
                )
            }
        }

        val suggestions = buildCoachingSuggestions(
            sessions = sessions,
            overallHsPercent = overallHsPercent,
            overallHitPercent = overallHitPercent,
            avgReaction = avgReaction,
            avgPrecision = avgPrecision,
            modeStats = modeStats
        )

        return PerformanceAnalyticsSummary(
            totalSessions = totalSessions,
            totalShotsFired = totalShots,
            totalHeadshots = totalHeadshots,
            totalBodyShots = totalBodyShots,
            totalMissedShots = totalMisses,
            overallHeadshotPercent = overallHsPercent,
            overallHitAccuracyPercent = overallHitPercent,
            avgReactionTimeMs = avgReaction,
            bestReactionTimeMs = bestReaction,
            avgAimingPrecision = avgPrecision,
            currentSkillRating = rating,
            trendPoints = trendPoints,
            modeBreakdown = modeStats,
            aiImprovementSuggestions = suggestions
        )
    }

    private fun buildCoachingSuggestions(
        sessions: List<TrainingSession>,
        overallHsPercent: Float,
        overallHitPercent: Float,
        avgReaction: Long,
        avgPrecision: Float,
        modeStats: List<ModePerformanceStat>
    ): List<String> {
        val tips = mutableListOf<String>()
        val avgDragVel = sessions.map { it.avgDragVelocityPxPerSec }.filter { it > 0f }.average().toFloat()
        val avgDragAngle = sessions.map { it.avgDragAngleDeg }.filter { it > 0f }.average().toFloat()

        if (overallHsPercent < 42f && overallHitPercent >= 65f) {
            tips.add(
                "High Body-Shot Ratio Detected: ${(overallHitPercent - overallHsPercent).roundToInt()}% of your shots land on the chest. Increase upward fire-button drag speed by ~18% or raise General sensitivity by +6."
            )
        } else if (overallHsPercent >= 55f) {
            tips.add(
                "Strong Headshot Lock (${overallHsPercent.roundToInt()}%): Challenge yourself in Advanced Mode and Moving Target Mode to maintain red-number consistency against strafing targets."
            )
        }

        if (avgReaction > 430L) {
            tips.add(
                "Reaction Latency (${avgReaction}ms): Run two 45-second drills in Reaction Speed Mode daily, keeping your thumb pre-positioned over the fire zone to shave 80–110ms off first-shot time."
            )
        } else if (avgReaction in 1L..340L) {
            tips.add(
                "Elite First-Shot Reflex (${avgReaction}ms average): Your target acquisition speed is tournament-grade—focus on first-bullet stop-and-pop rhythm to avoid movement bloom."
            )
        }

        if (!avgDragAngle.isNaN() && avgDragAngle > 22f) {
            tips.add(
                "Lateral Drag Drift (${avgDragAngle.roundToInt()}° off-vertical): Your thumb arcs too far sideways during upward drags. Anchor your right wrist and trace a tighter 85°–90° vertical path."
            )
        }

        if (!avgDragVel.isNaN() && avgDragVel in 1f..460f) {
            tips.add(
                "Under-Accelerated Drag (${avgDragVel.roundToInt()} px/s): Snap your thumb briskly over the first 120ms of firing so aim lifts above chest recoil lock."
            )
        }

        val movingStat = modeStats.find { it.mode == TrainingMode.MOVING_TARGET }
        if (movingStat != null && movingStat.sessionsCount > 0 && movingStat.avgHeadshotPercent < 38f) {
            tips.add(
                "Strafe Tracking Deficit (${movingStat.avgHeadshotPercent.roundToInt()}% in Moving Target Mode): Use a subtle J-shape drag curving in the direction of enemy movement."
            )
        }

        if (avgPrecision < 68f) {
            tips.add(
                "Crosshair Pre-Aim Placement (${avgPrecision.roundToInt()}/100): Keep your default camera horizon at shoulder height rather than ground level before initiating fire."
            )
        }

        if (tips.isEmpty()) {
            tips.add(
                "Balanced Telemetry Profile: Maintain your current sensitivity configuration and alternate between Drag Headshot Mode and Moving Target Mode."
            )
        }
        return tips
    }
}
