package com.headshotaipro.trainer.presentation.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.headshotaipro.trainer.domain.model.ProcessorTier
import com.headshotaipro.trainer.presentation.theme.CharcoalCard
import com.headshotaipro.trainer.presentation.theme.CharcoalElevated
import com.headshotaipro.trainer.presentation.theme.CharcoalSurface
import com.headshotaipro.trainer.presentation.theme.CrimsonHeadshot
import com.headshotaipro.trainer.presentation.theme.CyberCyan
import com.headshotaipro.trainer.presentation.theme.MatrixGreen
import com.headshotaipro.trainer.presentation.theme.ObsidianBlack
import com.headshotaipro.trainer.presentation.theme.TacticalGold
import com.headshotaipro.trainer.presentation.theme.TextPrimary
import com.headshotaipro.trainer.presentation.theme.TextSecondary
import com.headshotaipro.trainer.presentation.viewmodel.SensitivityViewModel

@Composable
fun SensitivityAnalyzerScreen(
    viewModel: SensitivityViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBlack)
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "AI SENSITIVITY & DPI ANALYZER",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary
        )
        Text(
            text = "Select a popular Free Fire MAX phone preset or enter your exact hardware specs to compute 0–200 sensitivity sliders and drag-headshot ergonomics.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        // Preloaded Phone Presets
        Text(
            text = "QUICK DEVICE PRESETS",
            style = MaterialTheme.typography.labelMedium,
            color = CyberCyan
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.presets.forEach { preset ->
                val isSelected = preset.phoneModel == state.phoneModelInput
                Surface(
                    color = if (isSelected) CrimsonHeadshot else CharcoalCard,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { viewModel.applyPreset(preset) }
                        .border(
                            1.dp,
                            if (isSelected) CyberCyan else CharcoalElevated,
                            RoundedCornerShape(10.dp)
                        )
                ) {
                    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                        Text(
                            text = preset.phoneModel,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${preset.ramGb}GB • ${preset.screenDpi} DPI • ${preset.refreshRateHz}Hz",
                            fontSize = 11.sp,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                    }
                }
            }
        }

        // Hardware Input Card
        Card(
            colors = CardDefaults.cardColors(containerColor = CharcoalSurface),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CharcoalElevated, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "DEVICE HARDWARE TELEMETRY INPUT",
                    style = MaterialTheme.typography.labelMedium,
                    color = CyberCyan
                )

                OutlinedTextField(
                    value = state.phoneModelInput,
                    onValueChange = viewModel::updatePhoneModel,
                    label = { Text("Phone Model (e.g. POCO X6 Pro, iQOO Neo 9)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CharcoalElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = state.processorInput,
                    onValueChange = viewModel::updateProcessor,
                    label = { Text("Processor / SoC (e.g. Snapdragon 8 Gen 2, Dimensity 8300)") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyberCyan,
                        unfocusedBorderColor = CharcoalElevated,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Processor Tier Selector
                Text(
                    text = "Processor Touch-Response Tier",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ProcessorTier.entries.forEach { tier ->
                        val active = state.selectedTier == tier
                        Surface(
                            color = if (active) CrimsonHeadshot else CharcoalCard,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.updateProcessorTier(tier) }
                                .border(1.dp, if (active) CyberCyan else CharcoalElevated, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = tier.name.replace("_", " "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                // RAM & Refresh Rate Selectors
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("RAM Capacity (GB)", fontSize = 12.sp, color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(4, 6, 8, 12, 16).forEach { ram ->
                                val active = state.ramGbInput == ram
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (active) CyberCyan else CharcoalCard)
                                        .clickable { viewModel.updateRamGb(ram) }
                                        .padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "${ram}G",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (active) ObsidianBlack else TextPrimary
                                    )
                                }
                            }
                        }
                    }
                }

                // Resolution & DPI Inputs
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = state.screenWidthInput,
                        onValueChange = { viewModel.updateResolution(it, state.screenHeightInput) },
                        label = { Text("Width (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.screenHeightInput,
                        onValueChange = { viewModel.updateResolution(state.screenWidthInput, it) },
                        label = { Text("Height (px)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = state.dpiInput,
                        onValueChange = viewModel::updateDpi,
                        label = { Text("Screen DPI") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Button(
                    onClick = { viewModel.recalculateRecommendation() },
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonHeadshot),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "GENERATE AI SENSITIVITY LOADOUT",
                        fontWeight = FontWeight.ExtraBold,
                        color = TextPrimary
                    )
                }
            }
        }

        // Output Recommendation Card
        state.recommendation?.let { rec ->
            Card(
                colors = CardDefaults.cardColors(containerColor = CharcoalCard),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, CyberCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "FREE FIRE MAX AI SENSITIVITY (0–200 SCALE)",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan
                            )
                            Text(
                                text = rec.deviceSpecs.phoneModel,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimary
                            )
                        }
                        Surface(
                            color = MatrixGreen.copy(alpha = 0.18f),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, MatrixGreen, RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "${rec.confidenceScorePercent}% AI MATCH",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MatrixGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    SensitivitySliderBar("General Sensitivity", rec.generalSensitivity, CrimsonHeadshot)
                    SensitivitySliderBar("Red Dot Sensitivity", rec.redDotSensitivity, CyberCyan)
                    SensitivitySliderBar("2x Scope Sensitivity", rec.scope2xSensitivity, TacticalGold)
                    SensitivitySliderBar("4x Scope Sensitivity", rec.scope4xSensitivity, MatrixGreen)
                    SensitivitySliderBar("Sniper Scope", rec.sniperScopeSensitivity, TextSecondary)
                    SensitivitySliderBar("Free Look", rec.freeLookSensitivity, CyberCyan)

                    Spacer(modifier = Modifier.height(4.dp))

                    // Ergonomics Metrics Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ObsidianBlack, RoundedCornerShape(10.dp))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ErgonomicBadge("FIRE BUTTON", "${rec.recommendedFireButtonSizePercent}%", CrimsonHeadshot)
                        ErgonomicBadge("DRAG SPEED", "${rec.optimalDragVelocityDpPerSec} dp/s", CyberCyan)
                        ErgonomicBadge("MAX SAFE DPI", "${rec.recommendedDpiSafeLimit} DPI", TacticalGold)
                    }

                    Text(
                        text = "TECHNIQUE: ${rec.dragTechniqueTitle.uppercase()}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CrimsonHeadshot
                    )

                    rec.dragRecommendations.forEach { tip ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "▸ ",
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = tip,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SensitivitySliderBar(label: String, value: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
            Text(
                text = "$value / 200",
                fontSize = 13.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (value / 200f).coerceIn(0f, 1f) },
            color = color,
            trackColor = ObsidianBlack,
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        )
    }
}

@Composable
private fun ErgonomicBadge(title: String, value: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = title, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
        Text(text = value, fontSize = 15.sp, fontWeight = FontWeight.ExtraBold, color = accent)
    }
}
