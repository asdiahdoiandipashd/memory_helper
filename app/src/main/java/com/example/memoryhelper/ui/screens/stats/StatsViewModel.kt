package com.example.memoryhelper.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryhelper.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class representing a single data point for the chart.
 */
data class ChartDataPoint(
    val date: String,
    val count: Int
)

/**
 * UI State for the Stats screen.
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val chartData: List<ChartDataPoint> = emptyList(),
    val totalReviews: Int = 0
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MemoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val reviewCounts = repository.getReviewCountsForLast7Days()

            // Convert to ordered list of ChartDataPoint
            val chartData = reviewCounts.entries
                .map { ChartDataPoint(date = it.key, count = it.value) }

            val totalReviews = chartData.sumOf { it.count }

            _uiState.value = StatsUiState(
                isLoading = false,
                chartData = chartData,
                totalReviews = totalReviews
            )
        }
    }
}
