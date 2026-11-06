package com.headshotaipro.trainer.domain

import com.headshotaipro.trainer.domain.model.DeviceSpecs
import com.headshotaipro.trainer.domain.model.HitZone
import com.headshotaipro.trainer.domain.model.ProcessorTier
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession
import com.headshotaipro.trainer.domain.usecase.AnalyzeSensitivityUseCase
import com.headshotaipro.trainer.domain.usecase.EvaluateShotPhysicsUseCase
import com.headshotaipro.trainer.domain.usecase.GetPerformanceAnalyticsUseCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HeadshotAiDomainTest {

    private val analyzeSensitivity = AnalyzeSensitivityUseCase()
    private val evaluateShotPhysics = EvaluateShotPhysicsUseCase()
    private val getPerformanceAnalytics = GetPerformanceAnalyticsUseCase()

    @Test
    fun `sensitivity analyzer stays within Free Fire MAX 0 to 200 scale`() {
        val specs = DeviceSpecs(
            phoneModel = "POCO X6 Pro 5G",
            processorName = "Dimensity 8300-Ultra",
            processorTier = ProcessorTier.HIGH_END,
            ramGb = 12,
            screenWidthPx = 1220,
            screenHeightPx = 2712,
            screenDpi = 446,
            refreshRateHz = 120
        )

        val rec = analyzeSensitivity(specs, recentHeadshotPercent = 54f, recentAvgDragVelocity = 720f)
        assertTrue(rec.generalSensitivity in 100..200)
        assertTrue(rec.redDotSensitivity in 100..200)
        assertTrue(rec.scope2xSensitivity in 80..200)
        assertTrue(rec.scope4xSensitivity in 80..200)
        assertTrue(rec.sniperScopeSensitivity in 40..160)
        assertTrue(rec.recommendedFireButtonSizePercent in 40..56)
        assertTrue(rec.dragRecommendations.isNotEmpty())
    }

    @Test
    fun `upward drag into head hitbox registers HEADSHOT in Drag Headshot Mode`() {
        val target = EvaluateShotPhysicsUseCase.TargetGeometry(
            id = 1,
            centerX = 500f,
            headCenterY = 300f,
            headRadiusPx = 42f,
            bodyTopY = 330f,
            bodyBottomY = 470f,
            bodyHalfWidthPx = 60f,
            depthScale = 1.0f,
            spawnTimestampMs = 1000L
        )

        val result = evaluateShotPhysics.evaluateShot(
            mode = TrainingMode.DRAG_HEADSHOT,
            shotTimestampMs = 1280L,
            startX = 502f,
            startY = 410f, // Started at torso
            releaseX = 500f,
            releaseY = 302f, // Released inside head hitbox
            dragDurationMs = 120L,
            targets = listOf(target)
        )

        assertEquals(HitZone.HEADSHOT, result.hitZone)
        assertEquals(1, result.hitTargetId)
        assertEquals(280L, result.telemetry.reactionTimeMs)
        assertTrue(result.telemetry.precisionScore >= 75f)
    }

    @Test
    fun `static tap without upward drag in Drag Headshot Mode downgrades to BODYSHOT`() {
        val target = EvaluateShotPhysicsUseCase.TargetGeometry(
            id = 2,
            centerX = 500f,
            headCenterY = 300f,
            headRadiusPx = 42f,
            bodyTopY = 330f,
            bodyBottomY = 470f,
            bodyHalfWidthPx = 60f,
            depthScale = 1.0f,
            spawnTimestampMs = 1000L
        )

        val result = evaluateShotPhysics.evaluateShot(
            mode = TrainingMode.DRAG_HEADSHOT,
            shotTimestampMs = 1350L,
            startX = 500f,
            startY = 300f, // No upward drag
            releaseX = 500f,
            releaseY = 300f,
            dragDurationMs = 90L,
            targets = listOf(target)
        )

        assertEquals(HitZone.BODYSHOT, result.hitZone)
    }

    @Test
    fun `performance analytics calculates accurate headshot percentage and coaching tips`() {
        val sessions = listOf(
            TrainingSession(
                id = 1L,
                timestampEpochMs = 1000L,
                mode = TrainingMode.DRAG_HEADSHOT,
                durationSec = 60,
                totalShots = 20,
                headshots = 12,
                bodyShots = 5,
                missedShots = 3,
                headshotAccuracyPercent = 60f,
                overallAccuracyPercent = 85f,
                avgReactionTimeMs = 320L,
                bestReactionTimeMs = 220L,
                aimingPrecisionScore = 82f,
                avgDragVelocityPxPerSec = 780f,
                avgDragAngleDeg = 10f,
                score = 3100,
                aiFeedbackSummary = "Good session"
            )
        )

        val summary = getPerformanceAnalytics(sessions)
        assertEquals(1, summary.totalSessions)
        assertEquals(60f, summary.overallHeadshotPercent, 0.01f)
        assertEquals(320L, summary.avgReactionTimeMs)
        assertTrue(summary.aiImprovementSuggestions.isNotEmpty())
    }
}
