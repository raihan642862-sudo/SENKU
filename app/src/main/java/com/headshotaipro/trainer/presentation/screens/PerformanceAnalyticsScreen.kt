package com.headshotaipro.trainer.presentation.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headshotaipro.trainer.domain.model.ModePerformanceStat
import com.headshotaipro.trainer.domain.model.SessionTrendPoint
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
import com.headshotaipro.trainer.presentation.viewmodel.AnalyticsViewModel
import kotlin.math.roundToInt

@Composable
fun PerformanceAnalyticsScreen(
    viewModel: AnalyticsViewModel
) {
    val state by viewModel.uiState.collectAsState()
    val summary = state.summary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header & Skill Rank Banner
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "AI PERFORMANCE ANALYTICS",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary
                )
                Text(
                    text = "RANK TIER: ${summary.currentSkillRating}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
            }
            OutlinedButton(
                onClick = {
                    if (state.sessions.isEmpty()) viewModel.restoreSampleData()
                    else viewModel.resetHistory()
                }
            ) {
                Text(
                    text = if (state.sessions.isEmpty()) "LOAD DEMO DATA" else "RESET LOGS",
                    fontSize = 11.sp,
                    color = CrimsonHeadshot
                )
            }
        }

        // KPI Metric Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KpiMetricCard(
                title = "HEADSHOT RATE",
                value = "${summary.overallHeadshotPercent.roundToInt()}%",
                subtitle = "${summary.totalHeadshots} Red Kills",
                accent = CrimsonHeadshot,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "AVG REACTION",
                value = "${summary.avgReactionTimeMs}ms",
                subtitle = "Best: ${summary.bestReactionTimeMs}ms",
                accent = CyberCyan,
                modifier = Modifier.weight(1f)
            )
            KpiMetricCard(
                title = "AIM PRECISION",
                value = "${summary.avgAimingPrecision.roundToInt()}/100",
                subtitle = "${summary.totalShotsFired} Total Shots",
                accent = MatrixGreen,
                modifier = Modifier.weight(1f)
            )
        }

        // Chart 1: Headshot % & Precision Progression Line Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CharcoalElevated, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "HEADSHOT ACCURACY & PRECISION TREND",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Crimson = Headshot %   •   Cyan = Aiming Precision Score",
                    fontSize = 11.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(12.dp))
                HeadshotTrendLineChart(
                    points = summary.trendPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                )
            }
        }

        // Chart 2: Shot Distribution Breakdown (Headshots vs Body vs Missed)
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CharcoalElevated, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "SHOT IMPACT DISTRIBUTION (SUCCESSFUL VS MISSED)",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                ShotDistributionChart(
                    headshots = summary.totalHeadshots,
                    bodyShots = summary.totalBodyShots,
                    missedShots = summary.totalMissedShots
                )
            }
        }

        // Chart 3: Mode-by-Mode Accuracy Bar Chart
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CharcoalElevated, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "TRAINING MODE HEADSHOT BREAKDOWN",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                ModeAccuracyBarChart(stats = summary.modeBreakdown)
            }
        }

        // Personalized AI Coaching Suggestions
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalCard),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CrimsonHeadshot.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "AI COACH PERSONALIZED IMPROVEMENT PLAN",
                    style = MaterialTheme.typography.labelMedium,
                    color = CrimsonHeadshot
                )
                summary.aiImprovementSuggestions.forEach { suggestion ->
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            text = "★ ",
                            color = TacticalGold,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CharcoalCard),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, CharcoalElevated, RoundedCornerShape(12.dp))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Text(text = title, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 20.sp, fontWeight = FontWeight.Black, color = accent)
            Text(text = subtitle, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

@Composable
private fun HeadshotTrendLineChart(
    points: List<SessionTrendPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            Text("No sessions logged yet.", color = TextSecondary)
        }
        return
    }

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padLeft = 24f
        val padRight = 16f
        val padTop = 16f
        val padBottom = 20f
        val plotW = (w - padLeft - padRight).coerceAtLeast(1f)
        val plotH = (h - padTop - padBottom).coerceAtLeast(1f)

        // Horizontal reference lines (25%, 50%, 75%, 100%)
        listOf(0.25f, 0.50f, 0.75f, 1.0f).forEach { frac ->
            val y = padTop + plotH * (1f - frac)
            drawLine(
                color = GridLineColor,
                start = Offset(padLeft, y),
                end = Offset(w - padRight, y),
                strokeWidth = 1f
            )
        }

        val stepX = if (points.size > 1) plotW / (points.size - 1) else 0f

        val hsPath = Path()
        val precPath = Path()

        points.forEachIndexed { idx, pt ->
            val x = padLeft + idx * stepX
            val hsY = padTop + plotH * (1f - (pt.headshotPercent / 100f).coerceIn(0f, 1f))
            val prY = padTop + plotH * (1f - (pt.precisionScore / 100f).coerceIn(0f, 1f))

            if (idx == 0) {
                hsPath.moveTo(x, hsY)
                precPath.moveTo(x, prY)
            } else {
                hsPath.lineTo(x, hsY)
                precPath.lineTo(x, prY)
            }
        }

        if (points.size > 1) {
            drawPath(
                path = precPath,
                color = CyberCyan,
                style = Stroke(width = 3.2f, cap = StrokeCap.Round)
            )
            drawPath(
                path = hsPath,
                color = CrimsonHeadshot,
                style = Stroke(width = 4f, cap = StrokeCap.Round)
            )
        }

        points.forEachIndexed { idx, pt ->
            val x = padLeft + idx * stepX
            val hsY = padTop + plotH * (1f - (pt.headshotPercent / 100f).coerceIn(0f, 1f))
            val prY = padTop + plotH * (1f - (pt.precisionScore / 100f).coerceIn(0f, 1f))

            drawCircle(color = CyberCyan, radius = 5f, center = Offset(x, prY))
            drawCircle(color = CrimsonHeadshot, radius = 6.5f, center = Offset(x, hsY))
        }
    }
}

@Composable
private fun ShotDistributionChart(
    headshots: Int,
    bodyShots: Int,
    missedShots: Int
) {
    val total = (headshots + bodyShots + missedShots).coerceAtLeast(1)
    val hsSweep = (headshots.toFloat() / total) * 360f
    val bodySweep = (bodyShots.toFloat() / total) * 360f
    val missSweep = 360f - hsSweep - bodySweep

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val stroke = 26f
            var startAngle = -90f
            drawArc(
                color = CrimsonHeadshot,
                startAngle = startAngle,
                sweepAngle = hsSweep,
                useCenter = false,
                style = Stroke(width = stroke)
            )
            startAngle += hsSweep
            drawArc(
                color = TacticalGold,
                startAngle = startAngle,
                sweepAngle = bodySweep,
                useCenter = false,
                style = Stroke(width = stroke)
            )
            startAngle += bodySweep
            drawArc(
                color = CharcoalElevated,
                startAngle = startAngle,
                sweepAngle = missSweep,
                useCenter = false,
                style = Stroke(width = stroke)
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            LegendItem("Red Headshots", "$headshots (${(headshots * 100f / total).roundToInt()}%)", CrimsonHeadshot)
            LegendItem("Yellow Body Hits", "$bodyShots (${(bodyShots * 100f / total).roundToInt()}%)", TacticalGold)
            LegendItem("Missed / Overshot", "$missedShots (${(missedShots * 100f / total).roundToInt()}%)", TextSecondary)
        }
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = label, fontSize = 12.sp, color = TextSecondary)
            Text(text = value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        }
    }
}

@Composable
private fun ModeAccuracyBarChart(stats: List<ModePerformanceStat>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        stats.forEach { stat ->
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${stat.mode.title} (${stat.sessionsCount} runs)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${stat.avgHeadshotPercent.roundToInt()}% HS • ${stat.avgReactionMs}ms",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = CyberCyan
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                ) {
                    drawRoundRect(
                        color = ObsidianBlack,
                        size = size,
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                    val barW = size.width * (stat.avgHeadshotPercent / 100f).coerceIn(0.03f, 1f)
                    drawRoundRect(
                        color = if (stat.avgHeadshotPercent >= 55f) CrimsonHeadshot else CyberCyan,
                        size = Size(barW, size.height),
                        cornerRadius = CornerRadius(6f, 6f)
                    )
                }
            }
        }
    }
}
