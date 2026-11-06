package com.headshotaipro.trainer.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headshotaipro.trainer.domain.model.HitZone
import com.headshotaipro.trainer.domain.model.TrainingMode
import com.headshotaipro.trainer.presentation.theme.CharcoalCard
import com.headshotaipro.trainer.presentation.theme.CharcoalElevated
import com.headshotaipro.trainer.presentation.theme.CharcoalSurface
import com.headshotaipro.trainer.presentation.theme.CrimsonHeadshot
import com.headshotaipro.trainer.presentation.theme.CyberCyan
import com.headshotaipro.trainer.presentation.theme.GridLineColor
import com.headshotaipro.trainer.presentation.theme.MatrixGreen
import com.headshotaipro.trainer.presentation.theme.ObsidianBlack
import com.headshotaipro.trainer.presentation.theme.TacticalGold
import com.headshotaipro.trainer.presentation.theme.TextPrimary
import com.headshotaipro.trainer.presentation.theme.TextSecondary
import com.headshotaipro.trainer.presentation.viewmodel.TrainingViewModel
import kotlin.math.roundToInt

@Composable
fun HeadshotArenaScreen(
    viewModel: TrainingViewModel
) {
    val state by viewModel.uiState.collectAsState()

    var arenaWidthPx by remember { mutableFloatStateOf(1080f) }
    var arenaHeightPx by remember { mutableFloatStateOf(1400f) }

    // Crosshair & drag gesture tracking
    var crosshairX by remember { mutableFloatStateOf(540f) }
    var crosshairY by remember { mutableFloatStateOf(520f) }
    var dragStartX by remember { mutableFloatStateOf(540f) }
    var dragStartY by remember { mutableFloatStateOf(520f) }
    var dragStartTimeMs by remember { mutableLongStateOf(0L) }
    var isDraggingFire by remember { mutableStateOf(false) }
    val dragTrail = remember { mutableStateListOf<Offset>() }

    val pulseTransition = rememberInfiniteTransition(label = "reticle_pulse")
    val pulseRadius by pulseTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        // Top Mode Selector Bar
        ModeSelectorRow(
            selectedMode = state.selectedMode,
            onSelectMode = { mode ->
                viewModel.selectMode(mode)
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Live Telemetry HUD Strip
        TelemetryHudBar(
            remainingSec = state.remainingSeconds,
            headshotPct = state.headshotPercentage,
            reactionMs = state.avgReactionMs,
            precision = state.avgPrecisionScore,
            score = state.score,
            streak = state.currentStreak
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Live AI Coaching Banner
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Bolt,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = state.liveAiCoachingBanner,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    maxLines = 2
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Main 2.5D Shooting Arena Canvas
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0E131F),
                            Color(0xFF141B2D),
                            Color(0xFF0A0C12)
                        )
                    )
                )
                .border(1.5.dp, CharcoalElevated, RoundedCornerShape(16.dp))
                .onSizeChanged { size ->
                    arenaWidthPx = size.width.toFloat().coerceAtLeast(1f)
                    arenaHeightPx = size.height.toFloat().coerceAtLeast(1f)
                    if (!isDraggingFire && crosshairX == 540f) {
                        crosshairX = arenaWidthPx * 0.5f
                        crosshairY = arenaHeightPx * 0.45f
                    }
                }
                .pointerInput(state.isRunning, state.selectedMode) {
                    detectTapGestures { tapOffset ->
                        crosshairX = tapOffset.x
                        crosshairY = tapOffset.y
                        if (state.isRunning) {
                            viewModel.onFireGestureCompleted(
                                arenaWidthPx = arenaWidthPx,
                                arenaHeightPx = arenaHeightPx,
                                startX = tapOffset.x,
                                startY = tapOffset.y + 10f,
                                releaseX = tapOffset.x,
                                releaseY = tapOffset.y,
                                dragDurationMs = 95L
                            )
                        }
                    }
                }
                .pointerInput(state.isRunning, state.selectedMode) {
                    detectDragGestures(
                        onDragStart = { startOffset ->
                            isDraggingFire = true
                            dragStartTimeMs = System.currentTimeMillis()
                            dragStartX = startOffset.x
                            dragStartY = startOffset.y
                            crosshairX = startOffset.x
                            crosshairY = startOffset.y
                            dragTrail.clear()
                            dragTrail.add(startOffset)
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            crosshairX = change.position.x.coerceIn(0f, arenaWidthPx)
                            crosshairY = change.position.y.coerceIn(0f, arenaHeightPx)
                            dragTrail.add(Offset(crosshairX, crosshairY))
                            if (dragTrail.size > 20) {
                                dragTrail.removeAt(0)
                            }
                        },
                        onDragEnd = {
                            isDraggingFire = false
                            val duration = (System.currentTimeMillis() - dragStartTimeMs).coerceAtLeast(16L)
                            if (state.isRunning) {
                                viewModel.onFireGestureCompleted(
                                    arenaWidthPx = arenaWidthPx,
                                    arenaHeightPx = arenaHeightPx,
                                    startX = dragStartX,
                                    startY = dragStartY,
                                    releaseX = crosshairX,
                                    releaseY = crosshairY,
                                    dragDurationMs = duration
                                )
                            }
                        },
                        onDragCancel = {
                            isDraggingFire = false
                            dragTrail.clear()
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val horizonY = h * 0.36f

                // 2.5D Perspective Floor Grid & Horizon Line
                drawLine(
                    color = CyberCyan.copy(alpha = 0.25f),
                    start = Offset(0f, horizonY),
                    end = Offset(w, horizonY),
                    strokeWidth = 1.5f
                )

                for (i in 1..6) {
                    val ratio = i / 6f
                    val y = horizonY + (h - horizonY) * (ratio * ratio)
                    drawLine(
                        color = GridLineColor,
                        start = Offset(0f, y),
                        end = Offset(w, y),
                        strokeWidth = 1f
                    )
                }

                for (col in -4..4) {
                    val topX = w * 0.5f + col * (w * 0.06f)
                    val bottomX = w * 0.5f + col * (w * 0.22f)
                    drawLine(
                        color = GridLineColor,
                        start = Offset(topX, horizonY),
                        end = Offset(bottomX, h),
                        strokeWidth = 1f
                    )
                }

                // Render Active Training Targets (2.5D Enemy Silhouettes with Head & Torso Hitboxes)
                for (target in state.activeTargets) {
                    val cx = target.normalizedX * w
                    val cy = target.normalizedY * h
                    val baseHeadRadius = (w * 0.040f) * target.depthScale * state.selectedMode.hitboxScale
                    val headCenterY = cy - baseHeadRadius * 1.65f
                    val bodyTopY = cy - baseHeadRadius * 0.65f
                    val bodyBottomY = cy + baseHeadRadius * 2.55f
                    val bodyHalfW = baseHeadRadius * 1.45f

                    // Target Shadow on 2.5D Floor
                    drawOval(
                        color = Color.Black.copy(alpha = 0.55f),
                        topLeft = Offset(cx - bodyHalfW * 1.1f, bodyBottomY + 6f),
                        size = Size(bodyHalfW * 2.2f, baseHeadRadius * 0.65f)
                    )

                    // Tactical Body Silhouette (Armored Torso)
                    drawRoundRect(
                        color = Color(0xFF1F293D),
                        topLeft = Offset(cx - bodyHalfW, bodyTopY),
                        size = Size(bodyHalfW * 2f, bodyBottomY - bodyTopY),
                        cornerRadius = CornerRadius(14f, 14f)
                    )
                    drawRoundRect(
                        color = CyberCyan.copy(alpha = 0.70f),
                        topLeft = Offset(cx - bodyHalfW, bodyTopY),
                        size = Size(bodyHalfW * 2f, bodyBottomY - bodyTopY),
                        cornerRadius = CornerRadius(14f, 14f),
                        style = Stroke(width = 2.2f)
                    )

                    // Chest Center Aim Guide (Start Point for Upward Drag)
                    val chestY = (bodyTopY + bodyBottomY) * 0.45f
                    drawCircle(
                        color = TacticalGold.copy(alpha = 0.45f),
                        radius = baseHeadRadius * 0.35f,
                        center = Offset(cx, chestY)
                    )

                    // Beginner / Drag Mode Upward Vector Guide Arrow
                    if (state.selectedMode == TrainingMode.BEGINNER || state.selectedMode == TrainingMode.DRAG_HEADSHOT) {
                        drawLine(
                            color = CrimsonHeadshot.copy(alpha = 0.60f),
                            start = Offset(cx, chestY),
                            end = Offset(cx, headCenterY),
                            strokeWidth = 3f,
                            cap = StrokeCap.Round
                        )
                    }

                    // Head Hitbox (Glowing Crimson Target Core)
                    drawCircle(
                        color = CrimsonHeadshot.copy(alpha = 0.28f),
                        radius = baseHeadRadius * pulseRadius * 1.25f,
                        center = Offset(cx, headCenterY)
                    )
                    drawCircle(
                        color = CrimsonHeadshot,
                        radius = baseHeadRadius,
                        center = Offset(cx, headCenterY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = baseHeadRadius * 0.35f,
                        center = Offset(cx, headCenterY)
                    )
                }

                // Render Upward Drag Trail Vector
                if (dragTrail.size >= 2) {
                    val path = Path().apply {
                        moveTo(dragTrail.first().x, dragTrail.first().y)
                        for (i in 1 until dragTrail.size) {
                            lineTo(dragTrail[i].x, dragTrail[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        brush = Brush.verticalGradient(
                            colors = listOf( CrimsonHeadshot, CyberCyan )
                        ),
                        style = Stroke(width = 5f, cap = StrokeCap.Round)
                    )
                }

                // Render Recent Hit Markers (Red Headshot Sparks & Yellow Body Sparks)
                for (marker in state.hitMarkers) {
                    val markerColor = when (marker.hitZone) {
                        HitZone.HEADSHOT -> CrimsonHeadshot
                        HitZone.BODYSHOT -> TacticalGold
                        HitZone.MISS -> TextSecondary
                    }
                    val r = if (marker.hitZone == HitZone.HEADSHOT) 26f else 16f
                    drawLine(
                        color = markerColor,
                        start = Offset(marker.xPx - r, marker.yPx - r),
                        end = Offset(marker.xPx + r, marker.yPx + r),
                        strokeWidth = 3.5f
                    )
                    drawLine(
                        color = markerColor,
                        start = Offset(marker.xPx + r, marker.yPx - r),
                        end = Offset(marker.xPx - r, marker.yPx + r),
                        strokeWidth = 3.5f
                    )
                }

                // Render Tactical Crosshair Reticle
                val reticleColor = if (isDraggingFire) CrimsonHeadshot else CyberCyan
                drawCircle(
                    color = reticleColor.copy(alpha = 0.85f),
                    radius = 22f,
                    center = Offset(crosshairX, crosshairY),
                    style = Stroke(width = 2.2f)
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(crosshairX - 32f, crosshairY),
                    end = Offset(crosshairX - 10f, crosshairY),
                    strokeWidth = 2.2f
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(crosshairX + 10f, crosshairY),
                    end = Offset(crosshairX + 32f, crosshairY),
                    strokeWidth = 2.2f
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(crosshairX, crosshairY - 32f),
                    end = Offset(crosshairX, crosshairY - 10f),
                    strokeWidth = 2.2f
                )
                drawLine(
                    color = reticleColor,
                    start = Offset(crosshairX, crosshairY + 10f),
                    end = Offset(crosshairX, crosshairY + 32f),
                    strokeWidth = 2.2f
                )
                drawCircle(
                    color = CrimsonHeadshot,
                    radius = 4f,
                    center = Offset(crosshairX, crosshairY)
                )
            }

            // Overlay: Drag Telemetry Pill (Top-Left of Arena)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .background(ObsidianBlack.copy(alpha = 0.78f), RoundedCornerShape(8.dp))
                    .border(1.dp, CharcoalElevated, RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "DRAG VEL: ${state.lastDragVelocityPxPerSec.roundToInt()} px/s  |  ANGLE: ${state.lastDragAngleDeg.roundToInt()}°",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = CyberCyan
                )
            }

            // Overlay: Interactive Free Fire Style Drag Fire Button (Bottom Center-Right)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp)
                    .size(78.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                CrimsonHeadshot.copy(alpha = 0.85f),
                                ObsidianBlack.copy(alpha = 0.90f)
                            )
                        )
                    )
                    .border(2.5.dp, if (isDraggingFire) CyberCyan else CrimsonHeadshot, CircleShape)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.GpsFixed,
                        contentDescription = "Drag Fire Button",
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                    Text(
                        text = "DRAG UP",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Session Summary Card when drill finishes
        AnimatedVisibility(visible = !state.isRunning && state.lastCompletedSession != null) {
            state.lastCompletedSession?.let { session ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                        .border(1.dp, CrimsonHeadshot.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "DRILL REPORT • ${session.mode.title.uppercase()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = CrimsonHeadshot
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Headshots: ${session.headshots}/${session.totalShots} (${session.headshotAccuracyPercent.roundToInt()}%) • Best Reflex: ${session.bestReactionTimeMs}ms • Score: ${session.score}",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = session.aiFeedbackSummary,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // Start / Stop Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    if (state.isRunning) {
                        viewModel.stopSession(saveToHistory = true)
                    } else {
                        viewModel.startSession()
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (state.isRunning) CharcoalElevated else CrimsonHeadshot
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (state.isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = TextPrimary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (state.isRunning) "END & SAVE SESSION (${state.remainingSeconds}s)" else "START AI HEADSHOT DRILL",
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.8.sp,
                    color = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ModeSelectorRow(
    selectedMode: TrainingMode,
    onSelectMode: (TrainingMode) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TrainingMode.entries.forEach { mode ->
            val isSelected = mode == selectedMode
            Surface(
                color = if (isSelected) CrimsonHeadshot else CharcoalSurface,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectMode(mode) }
                    .border(
                        width = 1.dp,
                        color = if (isSelected) CyberCyan else CharcoalElevated,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = mode.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = if (mode.requiresDragGesture) "Drag-Lock Required" else "Reflex / Tap-Drag",
                        fontSize = 10.sp,
                        color = if (isSelected) TextPrimary.copy(alpha = 0.85f) else CyberCyan
                    )
                }
            }
        }
    }
}

@Composable
private fun TelemetryHudBar(
    remainingSec: Int,
    headshotPct: Float,
    reactionMs: Long,
    precision: Float,
    score: Int,
    streak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CharcoalSurface, RoundedCornerShape(12.dp))
            .border(1.dp, CharcoalElevated, RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 6.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        HudStatItem("TIME", "${remainingSec}s", CyberCyan)
        HudStatItem("HEADSHOT%", "${headshotPct.roundToInt()}%", CrimsonHeadshot)
        HudStatItem("REACTION", if (reactionMs > 0) "${reactionMs}ms" else "--", TacticalGold)
        HudStatItem("PRECISION", "${precision.roundToInt()}%", MatrixGreen)
        HudStatItem("STREAK", "x$streak", CyberCyan)
        HudStatItem("SCORE", "$score", TextPrimary)
    }
}

@Composable
private fun HudStatItem(label: String, value: String, valueColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontFamily = FontFamily.Monospace,
            color = TextSecondary
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.ExtraBold,
            color = valueColor
        )
    }
}
