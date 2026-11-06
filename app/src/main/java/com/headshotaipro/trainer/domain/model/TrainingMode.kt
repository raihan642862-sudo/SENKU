package com.headshotaipro.trainer.domain.model

/**
 * Defines the 5 specialized training modes in HEADSHOT AI PRO.
 */
enum class TrainingMode(
    val id: String,
    val title: String,
    val subtitle: String,
    val targetSpeedMultiplier: Float,
    val hitboxScale: Float,
    val targetLifetimeMs: Long,
    val requiresDragGesture: Boolean,
    val defaultDurationSec: Int
) {
    BEGINNER(
        id = "BEGINNER",
        title = "Beginner Mode",
        subtitle = "Large head hitboxes, steady pacing & assisted drag trajectory guides",
        targetSpeedMultiplier = 0.45f,
        hitboxScale = 1.35f,
        targetLifetimeMs = 2800L,
        requiresDragGesture = false,
        defaultDurationSec = 45
    ),
    ADVANCED(
        id = "ADVANCED",
        title = "Advanced Mode",
        subtitle = "Smaller hitboxes, strafe-jumping silhouettes & tight recoil penalties",
        targetSpeedMultiplier = 1.25f,
        hitboxScale = 0.85f,
        targetLifetimeMs = 1550L,
        requiresDragGesture = true,
        defaultDurationSec = 60
    ),
    DRAG_HEADSHOT(
        id = "DRAG_HEADSHOT",
        title = "Drag Headshot Mode",
        subtitle = "Master upward fire-button swipe velocity, angle & chest-to-head lock timing",
        targetSpeedMultiplier = 0.75f,
        hitboxScale = 1.0f,
        targetLifetimeMs = 2100L,
        requiresDragGesture = true,
        defaultDurationSec = 60
    ),
    MOVING_TARGET(
        id = "MOVING_TARGET",
        title = "Moving Target Mode",
        subtitle = "Track unpredictable lateral strafes, crouch-peaks & 2.5D depth scaling",
        targetSpeedMultiplier = 1.65f,
        hitboxScale = 0.95f,
        targetLifetimeMs = 1900L,
        requiresDragGesture = true,
        defaultDurationSec = 60
    ),
    REACTION_SPEED(
        id = "REACTION_SPEED",
        title = "Reaction Speed Mode",
        subtitle = "Ultra-fast flick spawns measuring millisecond first-shot reflex time",
        targetSpeedMultiplier = 0.30f,
        hitboxScale = 1.05f,
        targetLifetimeMs = 950L,
        requiresDragGesture = false,
        defaultDurationSec = 45
    );

    companion object {
        fun fromId(id: String): TrainingMode =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: DRAG_HEADSHOT
    }
}
