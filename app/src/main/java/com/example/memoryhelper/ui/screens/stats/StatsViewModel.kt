package com.example.memoryhelper.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryhelper.data.repository.ActivePlanProgress
import com.example.memoryhelper.data.repository.ExamPlanRepository
import com.example.memoryhelper.data.repository.MemoryRepository
import com.example.memoryhelper.domain.scheduler.ReviewGradeOption
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

data class GradeBreakdownUi(
    val grade: ReviewGradeOption,
    val count: Int
)

/**
 * UI State for the Stats screen.
 */
data class StatsUiState(
    val isLoading: Boolean = true,
    val chartData: List<ChartDataPoint> = emptyList(),
    val totalReviews: Int = 0,
    val overdueReviews: Int = 0,
    val overdueRate: Float = 0f,
    val averageResponseMs: Long = 0L,
    val gradeBreakdown: List<GradeBreakdownUi> = emptyList(),
    val activePlanProgress: ActivePlanProgress? = null
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val examPlanRepository: ExamPlanRepository
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
            val summary = repository.getReviewStatsSummaryForLast7Days()
            val activePlanProgress = examPlanRepository.getActivePlanProgress()

            // Convert to ordered list of ChartDataPoint
            val chartData = reviewCounts.entries
                .map { ChartDataPoint(date = it.key, count = it.value) }

            val gradeBreakdown = ReviewGradeOption.entries.map { grade ->
                GradeBreakdownUi(
                    grade = grade,
                    count = summary.gradeCounts[grade] ?: 0
                )
            }

            _uiState.value = StatsUiState(
                isLoading = false,
                chartData = chartData,
                totalReviews = summary.totalReviews,
                overdueReviews = summary.overdueReviews,
                overdueRate = summary.overdueRate,
                averageResponseMs = summary.averageResponseMs,
                gradeBreakdown = gradeBreakdown,
                activePlanProgress = activePlanProgress
            )
        }
    }
}
