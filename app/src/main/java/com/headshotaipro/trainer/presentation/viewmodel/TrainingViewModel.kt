package com.headshotaipro.trainer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.headshotaipro.trainer.domain.model.HitZone
import com.headshotaipro.trainer.domain.model.ShotTelemetry
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.domain.model.TrainingSession
import com.headshotaipro.trainer.domain.repository.TrainingRepository
import com.headshotaipro.trainer.domain.usecase.EvaluateShotPhysicsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

data class ActiveArenaTarget(
    val id: Int,
    val normalizedX: Float,       // 0.15f .. 0.85f
    val normalizedY: Float,       // 0.22f .. 0.62f
    val depthScale: Float,        // 0.70f (far) .. 1.25f (close)
    val velocityX: Float,         // Normalized units per second
    val velocityY: Float,
    val strafePhase: Float,
    val spawnTimestampMs: Long,
    val maxLifetimeMs: Long
)

data class HitMarkerEffect(
    val id: Long,
    val xPx: Float,
    val yPx: Float,
    val hitZone: HitZone,
    val damageNumber: Int,
    val timestampMs: Long
)

data class ArenaUiState(
    val selectedMode: TrainingMode = TrainingMode.DRAG_HEADSHOT,
    val isRunning: Boolean = false,
    val remainingSeconds: Int = TrainingMode.DRAG_HEADSHOT.defaultDurationSec,
    val activeTargets: List<ActiveArenaTarget> = emptyList(),
    val hitMarkers: List<HitMarkerEffect> = emptyList(),
    val totalShots: Int = 0,
    val headshots: Int = 0,
    val bodyShots: Int = 0,
    val missedShots: Int = 0,
    val currentStreak: Int = 0,
    val bestStreak: Int = 0,
    val score: Int = 0,
    val avgReactionMs: Long = 0L,
    val bestReactionMs: Long = 0L,
    val avgPrecisionScore: Float = 0f,
    val lastDragVelocityPxPerSec: Float = 0f,
    val lastDragAngleDeg: Float = 0f,
    val liveAiCoachingBanner: String = "Select a training mode & press START DRILL. Drag UP on the Fire Button to lock red-number headshots.",
    val lastCompletedSession: TrainingSession? = null
) {
    val headshotPercentage: Float
        get() = if (totalShots > 0) (headshots * 100f) / totalShots else 0f

    val hitAccuracyPercentage: Float
        get() = if (totalShots > 0) ((headshots + bodyShots) * 100f) / totalShots else 0f
}

class TrainingViewModel(
    private val repository: TrainingRepository,
    private val evaluateShotPhysics: EvaluateShotPhysicsUseCase = EvaluateShotPhysicsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ArenaUiState())
    val uiState: StateFlow<ArenaUiState> = _uiState.asStateFlow()

    private val recordedShots = mutableListOf<ShotTelemetry>()
    private var gameLoopJob: Job? = null
    private var timerJob: Job? = null
    private var nextTargetId = 1

    init {
        viewModelScope.launch {
            repository.seedDemoSessionsIfEmpty()
        }
    }

    fun selectMode(mode: TrainingMode) {
        if (_uiState.value.isRunning) {
            stopSession(saveToHistory = false)
        }
        _uiState.update {
            it.copy(
                selectedMode = mode,
                remainingSeconds = mode.defaultDurationSec,
                lastCompletedSession = null,
                liveAiCoachingBanner = "${mode.title}: ${mode.subtitle}"
            )
        }
    }

    fun startSession() {
        stopJobs()
        recordedShots.clear()
        val mode = _uiState.value.selectedMode
        val now = System.currentTimeMillis()
        val initialTargets = listOf(spawnTarget(mode, now))

        _uiState.update {
            ArenaUiState(
                selectedMode = mode,
                isRunning = true,
                remainingSeconds = mode.defaultDurationSec,
                activeTargets = initialTargets,
                liveAiCoachingBanner = if (mode.requiresDragGesture) {
                    "DRAG UP on the crimson Fire Pad to snap from chest to head!"
                } else {
                    "Acquire target head hitbox quickly—speed & precision active!"
                }
            )
        }

        // 60 FPS Arena Physics & Target Lifecycle Loop
        gameLoopJob = viewModelScope.launch {
            var lastTickMs = System.currentTimeMillis()
            while (_uiState.value.isRunning) {
                delay(16L)
                val currentMs = System.currentTimeMillis()
                val dtSec = ((currentMs - lastTickMs).coerceAtLeast(1L)) / 1000f
                lastTickMs = currentMs
                updateArenaPhysics(currentMs, dtSec)
            }
        }

        // 1-Second Drill Countdown Loop
        timerJob = viewModelScope.launch {
            while (_uiState.value.isRunning && _uiState.value.remainingSeconds > 0) {
                delay(1000L)
                val nextRemaining = _uiState.value.remainingSeconds - 1
                if (nextRemaining <= 0) {
                    _uiState.update { it.copy(remainingSeconds = 0) }
                    stopSession(saveToHistory = true)
                } else {
                    _uiState.update { it.copy(remainingSeconds = nextRemaining) }
                }
            }
        }
    }

    private fun updateArenaPhysics(nowMs: Long, dtSec: Float) {
        val state = _uiState.value
        val mode = state.selectedMode
        val speedMul = mode.targetSpeedMultiplier

        val updatedTargets = mutableListOf<ActiveArenaTarget>()
        var expiredMisses = 0

        for (target in state.activeTargets) {
            val ageMs = nowMs - target.spawnTimestampMs
            if (ageMs >= target.maxLifetimeMs) {
                expiredMisses++
                continue
            }

            val phase = target.strafePhase + dtSec * 3.2f
            val strafeOscillation = if (mode == TrainingMode.MOVING_TARGET || mode == TrainingMode.ADVANCED) {
                sin(phase.toDouble()).toFloat() * 0.14f * speedMul
            } else {
                0f
            }

            var nx = target.normalizedX + (target.velocityX * speedMul + strafeOscillation) * dtSec
            var ny = target.normalizedY + (target.velocityY * speedMul * cos(phase.toDouble()).toFloat() * 0.35f) * dtSec
            var vx = target.velocityX

            if (nx < 0.16f) {
                nx = 0.16f
                vx = -vx
            } else if (nx > 0.84f) {
                nx = 0.84f
                vx = -vx
            }
            ny = ny.coerceIn(0.22f, 0.58f)

            updatedTargets.add(
                target.copy(
                    normalizedX = nx,
                    normalizedY = ny,
                    velocityX = vx,
                    strafePhase = phase
                )
            )
        }

        val desiredTargetCount = when (mode) {
            TrainingMode.MOVING_TARGET, TrainingMode.ADVANCED -> 2
            else -> 1
        }
        while (updatedTargets.size < desiredTargetCount) {
            updatedTargets.add(spawnTarget(mode, nowMs))
        }

        val activeMarkers = state.hitMarkers.filter { nowMs - it.timestampMs < 650L }

        if (expiredMisses > 0) {
            _uiState.update {
                it.copy(
                    activeTargets = updatedTargets,
                    hitMarkers = activeMarkers,
                    totalShots = it.totalShots + expiredMisses,
                    missedShots = it.missedShots + expiredMisses,
                    currentStreak = 0,
                    liveAiCoachingBanner = "Target despawned! Increase first-shot reaction speed."
                )
            }
        } else {
            _uiState.update {
                it.copy(
                    activeTargets = updatedTargets,
                    hitMarkers = activeMarkers
                )
            }
        }
    }

    /**
     * Processes a completed shot or upward drag-fire gesture from the Canvas Arena.
     */
    fun onFireGestureCompleted(
        arenaWidthPx: Float,
        arenaHeightPx: Float,
        startX: Float,
        startY: Float,
        releaseX: Float,
        releaseY: Float,
        dragDurationMs: Long
    ) {
        val state = _uiState.value
        if (!state.isRunning || arenaWidthPx <= 0f || arenaHeightPx <= 0f) return

        val nowMs = System.currentTimeMillis()
        val geometries = state.activeTargets.map { t ->
            val cx = t.normalizedX * arenaWidthPx
            val cy = t.normalizedY * arenaHeightPx
            val baseHeadRadius = (arenaWidthPx * 0.040f) * t.depthScale
            val headCenterY = cy - baseHeadRadius * 1.65f
            val bodyTopY = cy - baseHeadRadius * 0.65f
            val bodyBottomY = cy + baseHeadRadius * 2.55f
            val bodyHalfWidth = baseHeadRadius * 1.45f

            EvaluateShotPhysicsUseCase.TargetGeometry(
                id = t.id,
                centerX = cx,
                headCenterY = headCenterY,
                headRadiusPx = baseHeadRadius,
                bodyTopY = bodyTopY,
                bodyBottomY = bodyBottomY,
                bodyHalfWidthPx = bodyHalfWidth,
                depthScale = t.depthScale,
                spawnTimestampMs = t.spawnTimestampMs
            )
        }

        val eval = evaluateShotPhysics.evaluateShot(
            mode = state.selectedMode,
            shotTimestampMs = nowMs,
            startX = startX,
            startY = startY,
            releaseX = releaseX,
            releaseY = releaseY,
            dragDurationMs = dragDurationMs,
            targets = geometries
        )

        recordedShots.add(eval.telemetry)

        val newTotalShots = state.totalShots + 1
        val newHeadshots = state.headshots + if (eval.hitZone == HitZone.HEADSHOT) 1 else 0
        val newBodyShots = state.bodyShots + if (eval.hitZone == HitZone.BODYSHOT) 1 else 0
        val newMisses = state.missedShots + if (eval.hitZone == HitZone.MISS) 1 else 0

        val newStreak = if (eval.hitZone == HitZone.HEADSHOT) state.currentStreak + 1 else 0
        val newBestStreak = maxOf(state.bestStreak, newStreak)
        val newScore = (state.score + eval.scoreDelta).coerceAtLeast(0)

        val validReactions = recordedShots.map { it.reactionTimeMs }.filter { it > 0L }
        val avgReaction = if (validReactions.isNotEmpty()) validReactions.average().toLong() else 0L
        val bestReaction = validReactions.minOrNull() ?: 0L
        val avgPrecision = recordedShots.map { it.precisionScore }.average().toFloat()

        // Replace hit target with a freshly spawned target
        val nextTargets = state.activeTargets.map { t ->
            if (t.id == eval.hitTargetId) spawnTarget(state.selectedMode, nowMs) else t
        }

        val damageNum = when (eval.hitZone) {
            HitZone.HEADSHOT -> Random.nextInt(165, 248) // Iconic Free Fire Red Headshot damage
            HitZone.BODYSHOT -> Random.nextInt(24, 42)   // Yellow body damage
            HitZone.MISS -> 0
        }

        val newMarker = HitMarkerEffect(
            id = nowMs + Random.nextLong(1, 9999),
            xPx = releaseX,
            yPx = releaseY,
            hitZone = eval.hitZone,
            damageNumber = damageNum,
            timestampMs = nowMs
        )

        _uiState.update {
            it.copy(
                activeTargets = nextTargets,
                hitMarkers = (it.hitMarkers + newMarker).takeLast(8),
                totalShots = newTotalShots,
                headshots = newHeadshots,
                bodyShots = newBodyShots,
                missedShots = newMisses,
                currentStreak = newStreak,
                bestStreak = newBestStreak,
                score = newScore,
                avgReactionMs = avgReaction,
                bestReactionMs = bestReaction,
                avgPrecisionScore = avgPrecision,
                lastDragVelocityPxPerSec = eval.telemetry.dragVelocityPxPerSec,
                lastDragAngleDeg = eval.telemetry.dragAngleDegrees,
                liveAiCoachingBanner = eval.instantCoachingCue
            )
        }
    }

    fun stopSession(saveToHistory: Boolean = true) {
        val state = _uiState.value
        stopJobs()

        if (!saveToHistory || state.totalShots == 0) {
            _uiState.update {
                it.copy(
                    isRunning = false,
                    remainingSeconds = it.selectedMode.defaultDurationSec,
                    activeTargets = emptyList()
                )
            }
            return
        }

        val elapsedSec = (state.selectedMode.defaultDurationSec - state.remainingSeconds)
            .coerceIn(5, state.selectedMode.defaultDurationSec)

        val avgDragVel = recordedShots.map { it.dragVelocityPxPerSec }.filter { it > 0f }
            .let { if (it.isNotEmpty()) it.average().toFloat() else 0f }
        val avgDragAngle = recordedShots.map { it.dragAngleDegrees }.filter { it > 0f }
            .let { if (it.isNotEmpty()) it.average().toFloat() else 0f }

        val hsPct = state.headshotPercentage
        val summaryFeedback = when {
            hsPct >= 65f ->
                "Elite ${hsPct.roundToInt()}% headshot rate! Your upward drag velocity (${avgDragVel.roundToInt()} px/s) and ${state.avgReactionMs}ms reaction timing are tournament ready."
            hsPct >= 45f ->
                "Solid ${hsPct.roundToInt()}% headshot rate. Tighten your vertical drag angle (${avgDragAngle.roundToInt()}°) to convert remaining chest hits into red numbers."
            else ->
                "Recorded ${hsPct.roundToInt()}% headshots across ${state.totalShots} shots. Start crosshair at upper-chest height and flick upward faster."
        }

        val completedSession = TrainingSession(
            timestampEpochMs = System.currentTimeMillis(),
            mode = state.selectedMode,
            durationSec = elapsedSec,
            totalShots = state.totalShots,
            headshots = state.headshots,
            bodyShots = state.bodyShots,
            missedShots = state.missedShots,
            headshotAccuracyPercent = state.headshotPercentage,
            overallAccuracyPercent = state.hitAccuracyPercentage,
            avgReactionTimeMs = state.avgReactionMs,
            bestReactionTimeMs = state.bestReactionMs,
            aimingPrecisionScore = state.avgPrecisionScore,
            avgDragVelocityPxPerSec = avgDragVel,
            avgDragAngleDeg = avgDragAngle,
            score = state.score,
            aiFeedbackSummary = summaryFeedback
        )

        _uiState.update {
            it.copy(
                isRunning = false,
                activeTargets = emptyList(),
                lastCompletedSession = completedSession,
                liveAiCoachingBanner = summaryFeedback
            )
        }

        viewModelScope.launch {
            repository.insertSession(completedSession)
        }
    }

    private fun spawnTarget(mode: TrainingMode, nowMs: Long): ActiveArenaTarget {
        val id = nextTargetId++
        val nx = Random.nextFloat() * 0.62f + 0.19f
        val ny = Random.nextFloat() * 0.30f + 0.25f
        val depth = when (mode) {
            TrainingMode.BEGINNER -> 1.15f
            TrainingMode.REACTION_SPEED -> Random.nextFloat() * 0.30f + 0.90f
            else -> Random.nextFloat() * 0.45f + 0.75f
        }
        val baseVx = (if (Random.nextBoolean()) 1f else -1f) * (Random.nextFloat() * 0.14f + 0.10f)
        val baseVy = (if (Random.nextBoolean()) 1f else -1f) * (Random.nextFloat() * 0.06f + 0.03f)

        return ActiveArenaTarget(
            id = id,
            normalizedX = nx,
            normalizedY = ny,
            depthScale = depth,
            velocityX = baseVx,
            velocityY = baseVy,
            strafePhase = Random.nextFloat() * 6.28f,
            spawnTimestampMs = nowMs,
            maxLifetimeMs = mode.targetLifetimeMs
        )
    }

    private fun stopJobs() {
        gameLoopJob?.cancel()
        timerJob?.cancel()
        gameLoopJob = null
        timerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        stopJobs()
    }

    companion object {
        fun provideFactory(repository: TrainingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return TrainingViewModel(repository) as T
                }
            }
    }
}
