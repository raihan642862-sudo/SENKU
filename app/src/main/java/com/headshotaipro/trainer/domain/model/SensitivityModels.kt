package com.headshotaipro.trainer.domain.model

enum class ProcessorTier(val label: String, val touchSamplingFactor: Float) {
    ENTRY_LEVEL("Entry-Level (Helio G35/G85, Snapdragon 680)", 1.12f),
    MID_RANGE("Mid-Range (Dimensity 7050/8050, Snapdragon 7s Gen 2 / 778G)", 1.0f),
    HIGH_END("Flagship / Gaming (Snapdragon 8 Gen 2/3, Dimensity 9300)", 0.90f)
}

/**
 * Hardware profile input for the AI Sensitivity Analyzer.
 */
data class DeviceSpecs(
    val phoneModel: String,
    val processorName: String,
    val processorTier: ProcessorTier,
    val ramGb: Int,
    val screenWidthPx: Int,
    val screenHeightPx: Int,
    val screenDpi: Int,
    val refreshRateHz: Int = 90
)

/**
 * Recommended Free Fire MAX sensitivity configuration (0 - 200 scale)
 * plus drag-headshot ergonomics.
 */
data class SensitivityRecommendation(
    val deviceSpecs: DeviceSpecs,
    val generalSensitivity: Int,
    val redDotSensitivity: Int,
    val scope2xSensitivity: Int,
    val scope4xSensitivity: Int,
    val sniperScopeSensitivity: Int,
    val freeLookSensitivity: Int,
    val recommendedFireButtonSizePercent: Int,
    val optimalDragVelocityDpPerSec: Int,
    val recommendedDpiSafeLimit: Int,
    val dragTechniqueTitle: String,
    val dragRecommendations: List<String>,
    val confidenceScorePercent: Int
)
