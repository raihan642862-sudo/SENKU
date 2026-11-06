package com.headshotaipro.trainer.domain.usecase

import com.headshotaipro.trainer.domain.model.HitZone
import com.headshotaipro.trainer.domain.model.ShotTelemetry
import com.headshotaipro.trainer.domain.model.TrainingMode
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.roundToInt

/**
 * Computer Vision & Geometry Hitbox Evaluator for the 2D/2.5D Shooting Arena.
 * Evaluates touch/crosshair coordinates, upward drag vector trajectory, velocity,
 * and target silhouette geometry (Head Circle + Upper Torso Capsule).
 */
class EvaluateShotPhysicsUseCase {

    data class TargetGeometry(
        val id: Int,
        val centerX: Float,
        val headCenterY: Float,
        val headRadiusPx: Float,
        val bodyTopY: Float,
        val bodyBottomY: Float,
        val bodyHalfWidthPx: Float,
        val depthScale: Float, // 0.65f (far) .. 1.30f (close)
        val spawnTimestampMs: Long
    )

    data class ShotEvaluationResult(
        val hitTargetId: Int?,
        val hitZone: HitZone,
        val scoreDelta: Int,
        val telemetry: ShotTelemetry,
        val instantCoachingCue: String
    )

    fun evaluateShot(
        mode: TrainingMode,
        shotTimestampMs: Long,
        startX: Float,
        startY: Float,
        releaseX: Float,
        releaseY: Float,
        dragDurationMs: Long,
        targets: List<TargetGeometry>
    ): ShotEvaluationResult {
        val dx = releaseX - startX
        val dy = releaseY - startY // Negative dy means upward drag
        val dragDistancePx = hypot(dx.toDouble(), dy.toDouble()).toFloat()
        val safeDurationSec = (dragDurationMs.coerceAtLeast(16L)) / 1000f
        val upwardVelocityPxPerSec = if (dy < 0f) abs(dy) / safeDurationSec else 0f

        // Compute angle from straight-up vertical (0 deg = straight up, 90 deg = horizontal)
        val dragAngleFromVerticalDeg = if (dragDistancePx > 8f && dy < 0f) {
            Math.toDegrees(atan2(abs(dx).toDouble(), abs(dy).toDouble())).toFloat()
        } else {
            0f
        }

        // In Free Fire drag-headshot mechanics, starting near the chest/body and dragging upward
        // with optimal velocity (420..1450 px/s) snaps aim onto the head hitbox.
        val isOptimalDragWindow = upwardVelocityPxPerSec in 380f..1650f && dragAngleFromVerticalDeg <= 32f

        // Find closest target to the release point (or drag trajectory)
        var bestTarget: TargetGeometry? = null
        var minHeadDist = Float.MAX_VALUE

        for (target in targets) {
            val dHead = hypot(
                (releaseX - target.centerX).toDouble(),
                (releaseY - target.headCenterY).toDouble()
            ).toFloat()
            if (dHead < minHeadDist) {
                minHeadDist = dHead
                bestTarget = target
            }
        }

        if (bestTarget == null) {
            val missTelemetry = ShotTelemetry(
                timestampMs = shotTimestampMs,
                hitZone = HitZone.MISS,
                reactionTimeMs = 0L,
                aimingOffsetPx = 999f,
                precisionScore = 0f,
                dragVelocityPxPerSec = upwardVelocityPxPerSec,
                dragAngleDegrees = dragAngleFromVerticalDeg,
                targetDistanceDepth = 1.0f
            )
            return ShotEvaluationResult(
                hitTargetId = null,
                hitZone = HitZone.MISS,
                scoreDelta = -15,
                telemetry = missTelemetry,
                instantCoachingCue = "No active target in sightline—wait for spawn lock."
            )
        }

        val reactionMs = (shotTimestampMs - bestTarget.spawnTimestampMs).coerceIn(85L, 4500L)
        val effectiveHeadRadius = bestTarget.headRadiusPx * mode.hitboxScale *
            if (isOptimalDragWindow) 1.18f else 1.0f

        // Check if drag started near torso/head and ended in head zone
        val headDistance = hypot(
            (releaseX - bestTarget.centerX).toDouble(),
            (releaseY - bestTarget.headCenterY).toDouble()
        ).toFloat()

        val inBodyHorizontal = abs(releaseX - bestTarget.centerX) <= bestTarget.bodyHalfWidthPx * mode.hitboxScale
        val inBodyVertical = releaseY in bestTarget.bodyTopY..bestTarget.bodyBottomY

        // If mode requires drag gesture, pure tap without upward drag on head is downgraded to bodyshot
        // unless upward velocity >= 240 px/s
        val dragRequirementMet = !mode.requiresDragGesture || (dy < -14f && upwardVelocityPxPerSec >= 240f)

        val hitZone = when {
            headDistance <= effectiveHeadRadius && dragRequirementMet -> HitZone.HEADSHOT
            headDistance <= effectiveHeadRadius && !dragRequirementMet -> HitZone.BODYSHOT
            inBodyHorizontal && inBodyVertical -> HitZone.BODYSHOT
            else -> HitZone.MISS
        }

        val normalizedPrecision = when (hitZone) {
            HitZone.HEADSHOT -> {
                val centerAccuracy = (1f - (headDistance / (effectiveHeadRadius * 1.15f))).coerceIn(0.15f, 1f)
                (75f + centerAccuracy * 25f).coerceIn(75f, 100f)
            }
            HitZone.BODYSHOT -> {
                val bodyCenterY = (bestTarget.bodyTopY + bestTarget.bodyBottomY) * 0.5f
                val bodyDist = hypot(
                    (releaseX - bestTarget.centerX).toDouble(),
                    (releaseY - bodyCenterY).toDouble()
                ).toFloat()
                val maxBodySpan = (bestTarget.bodyBottomY - bestTarget.bodyTopY).coerceAtLeast(40f)
                (45f + (1f - (bodyDist / maxBodySpan).coerceIn(0f, 1f)) * 28f).coerceIn(35f, 73f)
            }
            HitZone.MISS -> {
                val missRatio = (1f - (headDistance / 360f)).coerceIn(0f, 1f)
                (missRatio * 30f).coerceIn(0f, 30f)
            }
        }

        val speedBonus = ((1200L - reactionMs).coerceAtLeast(0L) / 25L).toInt()
        val scoreDelta = when (hitZone) {
            HitZone.HEADSHOT -> 120 + speedBonus + (normalizedPrecision * 0.3f).roundToInt()
            HitZone.BODYSHOT -> 45 + (speedBonus / 2)
            HitZone.MISS -> -15
        }

        val cue = when (hitZone) {
            HitZone.HEADSHOT -> {
                when {
                    reactionMs < 320L && isOptimalDragWindow ->
                        "PERFECT RED-NUMBER DRAG! (${reactionMs}ms • ${upwardVelocityPxPerSec.roundToInt()} px/s)"
                    dragAngleFromVerticalDeg <= 10f ->
                        "Clean vertical lock! Precision ${normalizedPrecision.roundToInt()}%"
                    else ->
                        "Diagonal J-Drag Headshot locked in ${reactionMs}ms!"
                }
            }
            HitZone.BODYSHOT -> {
                when {
                    mode.requiresDragGesture && dy >= -14f ->
                        "Yellow Number (Body): Swipe UP on the fire button to lift aim to the head!"
                    upwardVelocityPxPerSec < 360f ->
                        "Body Hit: Drag speed (${upwardVelocityPxPerSec.roundToInt()} px/s) too slow—flick faster!"
                    else ->
                        "Chest Hit: Start crosshair slightly higher before dragging upward."
                }
            }
            HitZone.MISS -> {
                when {
                    releaseY < bestTarget.headCenterY - effectiveHeadRadius ->
                        "Overshot helmet! Lower drag force or reduce Red Dot sensitivity by 3–5."
                    dragAngleFromVerticalDeg > 35f ->
                        "Wide lateral drift (${dragAngleFromVerticalDeg.roundToInt()}°)—keep upward drag straighter."
                    else ->
                        "Missed target (${headDistance.roundToInt()}px off-center)—track movement before firing."
                }
            }
        }

        val telemetry = ShotTelemetry(
            timestampMs = shotTimestampMs,
            hitZone = hitZone,
            reactionTimeMs = reactionMs,
            aimingOffsetPx = headDistance,
            precisionScore = normalizedPrecision,
            dragVelocityPxPerSec = upwardVelocityPxPerSec,
            dragAngleDegrees = dragAngleFromVerticalDeg,
            targetDistanceDepth = bestTarget.depthScale
        )

        return ShotEvaluationResult(
            hitTargetId = if (hitZone != HitZone.MISS) bestTarget.id else null,
            hitZone = hitZone,
            scoreDelta = scoreDelta,
            telemetry = telemetry,
            instantCoachingCue = cue
        )
    }
}
