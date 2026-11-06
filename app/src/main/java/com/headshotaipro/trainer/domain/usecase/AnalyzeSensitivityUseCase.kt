package com.headshotaipro.trainer.domain.usecase

import com.headshotaipro.trainer.domain.model.DeviceSpecs
import com.headshotaipro.trainer.domain.model.ProcessorTier
import com.headshotaipro.trainer.domain.model.SensitivityRecommendation
import kotlin.math.roundToInt

/**
 * AI Sensitivity Analyzer Engine.
 * Computes optimal Free Fire MAX sensitivity parameters (0..200 scale), fire button size,
 * and personalized drag-headshot mechanics from device screen geometry, DPI, RAM, and SoC tier.
 */
class AnalyzeSensitivityUseCase {

    operator fun invoke(
        specs: DeviceSpecs,
        recentHeadshotPercent: Float = 0f,
        recentAvgDragVelocity: Float = 0f
    ): SensitivityRecommendation {
        val totalPixels = (specs.screenWidthPx * specs.screenHeightPx).coerceAtLeast(720 * 1280)
        val referencePixels = 1080f * 2400f
        val resolutionFactor = (totalPixels / referencePixels).coerceIn(0.65f, 1.45f)

        // Higher DPI means faster cursor traversal per physical millimeter of thumb movement
        val dpiNormalized = (420f / specs.screenDpi.coerceIn(240, 640)).coerceIn(0.72f, 1.35f)

        // RAM & Refresh rate stability factor
        val ramFactor = when {
            specs.ramGb <= 4 -> 1.10f
            specs.ramGb <= 6 -> 1.04f
            specs.ramGb <= 8 -> 1.00f
            else -> 0.95f
        }
        val refreshAdjustment = when {
            specs.refreshRateHz >= 120 -> -6
            specs.refreshRateHz >= 90 -> -2
            else -> +5
        }

        val compositeMultiplier =
            (resolutionFactor * 0.35f + dpiNormalized * 0.40f + specs.processorTier.touchSamplingFactor * 0.25f) * ramFactor

        // Adaptive AI calibration based on user's recent arena performance
        val performanceDelta = when {
            recentHeadshotPercent in 1f..32f -> +6 // Boost sensitivity slightly to assist upward drag
            recentHeadshotPercent > 68f -> -3     // Fine-tune for micro-precision control
            else -> 0
        }

        // Free Fire MAX uses a 0..200 sensitivity scale
        val general = (168 * compositeMultiplier + refreshAdjustment + performanceDelta)
            .roundToInt()
            .coerceIn(115, 200)
        val redDot = (156 * compositeMultiplier + refreshAdjustment + performanceDelta)
            .roundToInt()
            .coerceIn(105, 198)
        val scope2x = (144 * compositeMultiplier + refreshAdjustment)
            .roundToInt()
            .coerceIn(95, 192)
        val scope4x = (132 * compositeMultiplier + refreshAdjustment)
            .roundToInt()
            .coerceIn(85, 185)
        val sniper = (88 * compositeMultiplier)
            .roundToInt()
            .coerceIn(50, 145)
        val freeLook = (125 * compositeMultiplier)
            .roundToInt()
            .coerceIn(75, 175)

        // Fire button size recommendation (in %): larger screens / high DPI benefit from 42%..48%
        val diagonalInchesProxy = kotlin.math.sqrt(
            (specs.screenWidthPx * specs.screenWidthPx + specs.screenHeightPx * specs.screenHeightPx).toDouble()
        ) / specs.screenDpi.coerceAtLeast(240)

        val fireButtonSize = when {
            diagonalInchesProxy >= 6.7 -> 44
            diagonalInchesProxy >= 6.4 -> 48
            else -> 52
        }

        val optimalDragSpeedDpPerSec = (820f / compositeMultiplier).roundToInt().coerceIn(580, 1150)
        val safeDpi = (specs.screenDpi + 60).coerceAtMost(540)

        val dragTitle = when (specs.processorTier) {
            ProcessorTier.ENTRY_LEVEL -> "J-Curve Progressive Drag (High-Torque)"
            ProcessorTier.MID_RANGE -> "Crisp Vertical Flick-Drag (Balanced)"
            ProcessorTier.HIGH_END -> "Micro-Snap Short Drag (High Sampling)"
        }

        val recommendations = buildList {
            add(
                "Position your Fire Button at ${fireButtonSize}% size, placed 18% above the bottom bezel to maximize vertical thumb travel on ${specs.phoneModel}."
            )
            add(
                "Target an upward drag velocity of ~$optimalDragSpeedDpPerSec dp/s: start crosshair at upper-chest height for close range (<15m) and just below the chin for mid range (15m–35m)."
            )
            if (specs.screenDpi < 400) {
                add(
                    "Your current DPI (${specs.screenDpi}) requires longer thumb sweeps. Use General Sensitivity $general and execute a firm 35°–10° upward J-curve when enemies strafe."
                )
            } else {
                add(
                    "With ${specs.screenDpi} DPI, avoid over-dragging past the helmet. Keep vertical swipe length between 2.2 cm and 2.8 cm to prevent recoil overshoot."
                )
            }
            if (specs.ramGb <= 6 || specs.processorTier == ProcessorTier.ENTRY_LEVEL) {
                add(
                    "To eliminate touch latency spikes on ${specs.processorName} (${specs.ramGb}GB RAM), lock graphics to Smooth + High FPS during competitive matches."
                )
            } else {
                add(
                    "Your ${specs.processorName} supports low-latency touch polling. Practice 180ms instant release timing in Drag Headshot Mode so bullets register on the 2nd–3rd frame."
                )
            }
            if (recentAvgDragVelocity > 1350f) {
                add(
                    "AI Telemetry Notice: Your recent training drag speed (${recentAvgDragVelocity.roundToInt()} px/s) is slightly aggressive—reduce Red Dot by 4 points if shots fly above the head."
                )
            }
        }

        val confidence = when {
            specs.phoneModel.isNotBlank() && specs.processorName.isNotBlank() -> 96
            else -> 88
        }

        return SensitivityRecommendation(
            deviceSpecs = specs,
            generalSensitivity = general,
            redDotSensitivity = redDot,
            scope2xSensitivity = scope2x,
            scope4xSensitivity = scope4x,
            sniperScopeSensitivity = sniper,
            freeLookSensitivity = freeLook,
            recommendedFireButtonSizePercent = fireButtonSize,
            optimalDragVelocityDpPerSec = optimalDragSpeedDpPerSec,
            recommendedDpiSafeLimit = safeDpi,
            dragTechniqueTitle = dragTitle,
            dragRecommendations = recommendations,
            confidenceScorePercent = confidence
        )
    }
}
