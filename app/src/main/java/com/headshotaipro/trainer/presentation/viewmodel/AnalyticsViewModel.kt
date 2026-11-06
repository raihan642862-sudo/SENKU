package com.headshotaipro.trainer.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.headshotaipro.trainer.domain.model.PerformanceAnalyticsSummary
import com.headshotaipro.trainer.domain.model.TrainingSession
import com.headshotaipro.trainer.domain.repository.TrainingRepository
import com.headshotaipro.trainer.domain.usecase.GetPerformanceAnalyticsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class AnalyticsUiState(
    val sessions: List<TrainingSession> = emptyList(),
    val summary: PerformanceAnalyticsSummary = GetPerformanceAnalyticsUseCase()(emptyList())
)

class AnalyticsViewModel(
    private val repository: TrainingRepository,
    private val getAnalytics: GetPerformanceAnalyticsUseCase = GetPerformanceAnalyticsUseCase()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AnalyticsUiState())
    val uiState: StateFlow<AnalyticsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeAllSessions().collectLatest { sessions ->
                _uiState.value = AnalyticsUiState(
                    sessions = sessions,
                    summary = getAnalytics(sessions)
                )
            }
        }
    }

    fun resetHistory() {
        viewModelScope.launch {
            repository.clearAllSessions()
        }
    }

    fun restoreSampleData() {
        viewModelScope.launch {
            repository.seedDemoSessionsIfEmpty()
        }
    }

    companion object {
        fun provideFactory(repository: TrainingRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return AnalyticsViewModel(repository) as T
                }
            }
    }
}
