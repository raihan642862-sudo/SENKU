package com.headshotaipro.trainer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.headshotaipro.trainer.domain.model.DeviceSpecs
import com.headshotaipro.trainer.domain.model.ProcessorTier
import com.headshotaipro.trainer.domain.model.SensitivityRecommendation
import com.headshotaipro.trainer.domain.repository.TrainingRepository
import com.headshotaipro.trainer.domain.usecase.AnalyzeSensitivityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SensitivityUiState(
    val presets: List<DeviceSpecs> = emptyList(),
    val phoneModelInput: String = "POCO X6 Pro 5G",
    val processorInput: String = "MediaTek Dimensity 8300-Ultra",
    val selectedTier: ProcessorTier = ProcessorTier.HIGH_END,
    val ramGbInput: Int = 12,
    val screenWidthInput: String = "1220",
    val screenHeightInput: String = "2712",
    val dpiInput: String = "446",
    val refreshRateHz: Int = 120,
    val recentHeadshotPercent: Float = 0f,
    val recentAvgDragVelocity: Float = 0f,
    val recommendation: SensitivityRecommendation? = null
)

class SensitivityViewModel(
    private val repository: TrainingRepository,
    private val analyzeSensitivity: AnalyzeSensitivityUseCase = AnalyzeSensitivityUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SensitivityUiState())
    val uiState: StateFlow<SensitivityUiState> = _uiState.asStateFlow()

    init {
        val presets = repository.getPreloadedDevicePresets()
        val defaultDevice = presets.firstOrNull() ?: DeviceSpecs(
            phoneModel = "Generic Gaming Phone",
            processorName = "Snapdragon 7+ Gen 2",
            processorTier = ProcessorTier.MID_RANGE,
            ramGb = 8,
            screenWidthPx = 1080,
            screenHeightPx = 2400,
            screenDpi = 420,
            refreshRateHz = 120
        )

        _uiState.update {
            it.copy(
                presets = presets,
                phoneModelInput = defaultDevice.phoneModel,
                processorInput = defaultDevice.processorName,
                selectedTier = defaultDevice.processorTier,
                ramGbInput = defaultDevice.ramGb,
                screenWidthInput = defaultDevice.screenWidthPx.toString(),
                screenHeightInput = defaultDevice.screenHeightPx.toString(),
                dpiInput = defaultDevice.screenDpi.toString(),
                refreshRateHz = defaultDevice.refreshRateHz
            )
        }
        recalculateRecommendation()

        viewModelScope.launch {
            repository.observeAllSessions().collectLatest { sessions ->
                if (sessions.isNotEmpty()) {
                    val totalShots = sessions.sumOf { it.totalShots }.coerceAtLeast(1)
                    val totalHs = sessions.sumOf { it.headshots }
                    val hsPct = (totalHs * 100f) / totalShots
                    val avgVel = sessions.map { it.avgDragVelocityPxPerSec }.filter { it > 0f }
                        .let { list -> if (list.isNotEmpty()) list.average().toFloat() else 0f }

                    _uiState.update {
                        it.copy(
                            recentHeadshotPercent = hsPct,
                            recentAvgDragVelocity = avgVel
                        )
                    }
                    recalculateRecommendation()
                }
            }
        }
    }

    fun applyPreset(device: DeviceSpecs) {
        _uiState.update {
            it.copy(
                phoneModelInput = device.phoneModel,
                processorInput = device.processorName,
                selectedTier = device.processorTier,
                ramGbInput = device.ramGb,
                screenWidthInput = device.screenWidthPx.toString(),
                screenHeightInput = device.screenHeightPx.toString(),
                dpiInput = device.screenDpi.toString(),
                refreshRateHz = device.refreshRateHz
            )
        }
        recalculateRecommendation()
    }

    fun updatePhoneModel(value: String) {
        _uiState.update { it.copy(phoneModelInput = value) }
    }

    fun updateProcessor(value: String) {
        _uiState.update { it.copy(processorInput = value) }
    }

    fun updateProcessorTier(tier: ProcessorTier) {
        _uiState.update { it.copy(selectedTier = tier) }
        recalculateRecommendation()
    }

    fun updateRamGb(ramGb: Int) {
        _uiState.update { it.copy(ramGbInput = ramGb) }
        recalculateRecommendation()
    }

    fun updateResolution(width: String, height: String) {
        _uiState.update {
            it.copy(
                screenWidthInput = width.filter { ch -> ch.isDigit() },
                screenHeightInput = height.filter { ch -> ch.isDigit() }
            )
        }
    }

    fun updateDpi(dpi: String) {
        _uiState.update { it.copy(dpiInput = dpi.filter { ch -> ch.isDigit() }) }
    }

    fun updateRefreshRate(hz: Int) {
        _uiState.update { it.copy(refreshRateHz = hz) }
        recalculateRecommendation()
    }

    fun recalculateRecommendation() {
        val state = _uiState.value
        val width = state.screenWidthInput.toIntOrNull()?.coerceIn(720, 3200) ?: 1080
        val height = state.screenHeightInput.toIntOrNull()?.coerceIn(1280, 3840) ?: 2400
        val dpi = state.dpiInput.toIntOrNull()?.coerceIn(240, 640) ?: 420

        val specs = DeviceSpecs(
            phoneModel = state.phoneModelInput.ifBlank { "Custom Android Device" },
            processorName = state.processorInput.ifBlank { "Octa-Core Gaming SoC" },
            processorTier = state.selectedTier,
            ramGb = state.ramGbInput,
            screenWidthPx = width,
            screenHeightPx = height,
            screenDpi = dpi,
            refreshRateHz = state.refreshRateHz
        )

        val result = analyzeSensitivity(
            specs = specs,
            recentHeadshotPercent = state.recentHeadshotPercent,
            recentAvgDragVelocity = state.recentAvgDragVelocity
        )

        _uiState.update { it.copy(recommendation = result) }
    }

    companion object {
        fun provideFactory(repository: TrainingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SensitivityViewModel(repository) as T
                }
            }
    }
}
