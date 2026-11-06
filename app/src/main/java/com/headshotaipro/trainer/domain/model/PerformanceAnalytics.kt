package com.headshotaipro.trainer.domain.model

data class ModePerformanceStat(
    val mode: TrainingMode,
    val sessionsCount: Int,
    val avgHeadshotPercent: Float,
    val avgReactionMs: Long,
    val bestScore: Int
)

data class SessionTrendPoint(
    val sessionIndex: Int,
    val label: String,
    val headshotPercent: Float,
    val precisionScore: Float,
    val reactionMs: Long
)

data class PerformanceAnalyticsSummary(
    val totalSessions: Int,
    val totalShotsFired: Int,
    val totalHeadshots: Int,
    val totalBodyShots: Int,
    val totalMissedShots: Int,
    val overallHeadshotPercent: Float,
    val overallHitAccuracyPercent: Float,
    val avgReactionTimeMs: Long,
    val bestReactionTimeMs: Long,
    val avgAimingPrecision: Float,
    val currentSkillRating: String,
    val trendPoints: List<SessionTrendPoint>,
    val modeBreakdown: List<ModePerformanceStat>,
    val aiImprovementSuggestions: List<String>
)
