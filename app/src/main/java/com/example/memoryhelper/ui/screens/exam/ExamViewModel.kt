package com.example.memoryhelper.ui.screens.exam

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryhelper.data.local.dao.DailyPlanRow
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.data.repository.ExamPlanRepository
import com.example.memoryhelper.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ExamUiState(
    val plans: List<ExamPlan> = emptyList(),
    val activePlan: ExamPlan? = null,
    val subjects: List<ExamSubject> = emptyList(),
    val notebooks: List<Notebook> = emptyList(),
    val todayPlan: List<DailyPlanRow> = emptyList(),
    val lastGeneratedCount: Int? = null,
    val message: String? = null
)

@HiltViewModel
@OptIn(ExperimentalCoroutinesApi::class)
class ExamViewModel @Inject constructor(
    private val examPlanRepository: ExamPlanRepository,
    private val memoryRepository: MemoryRepository
) : ViewModel() {

    private val todayEpochDay = LocalDate.now().toEpochDay()
    private val generationCount = MutableStateFlow<Int?>(null)
    private val message = MutableStateFlow<String?>(null)

    val uiState: StateFlow<ExamUiState> = examPlanRepository.getPlansFlow()
        .flatMapLatest { plans ->
            val activePlan = plans.firstOrNull { it.isActive }
            val notebooksFlow = memoryRepository.getAllNotebooksFlow()

            if (activePlan == null) {
                combine(
                    notebooksFlow,
                    generationCount,
                    message
                ) { notebooks: List<Notebook>, count: Int?, latestMessage: String? ->
                    ExamUiState(
                        plans = plans,
                        activePlan = null,
                        subjects = emptyList(),
                        notebooks = notebooks,
                        todayPlan = emptyList(),
                        lastGeneratedCount = count,
                        message = latestMessage
                    )
                }
            } else {
                combine(
                    examPlanRepository.getSubjectsFlow(activePlan.id),
                    notebooksFlow,
                    examPlanRepository.getDailyPlanRowsFlow(activePlan.id, todayEpochDay),
                    generationCount,
                    message
                ) { subjects: List<ExamSubject>,
                    notebooks: List<Notebook>,
                    todayPlan: List<DailyPlanRow>,
                    count: Int?,
                    latestMessage: String? ->
                    ExamUiState(
                        plans = plans,
                        activePlan = activePlan,
                        subjects = subjects,
                        notebooks = notebooks,
                        todayPlan = todayPlan,
                        lastGeneratedCount = count,
                        message = latestMessage
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExamUiState()
        )

    fun createPlan(
        name: String,
        examDate: LocalDate,
        dailyBudget: Int,
        overdueCompensationLimit: Int
    ) {
        viewModelScope.launch {
            examPlanRepository.createPlan(
                name = name.trim(),
                examDateEpochDay = examDate.toEpochDay(),
                dailyBudget = dailyBudget,
                overdueCompensationLimit = overdueCompensationLimit
            )
            message.value = "Study plan created"
        }
    }

    fun addSubject(
        planId: Long,
        name: String,
        weight: Float,
        notebookId: Long?
    ) {
        viewModelScope.launch {
            examPlanRepository.addSubject(
                planId = planId,
                name = name.trim(),
                weight = weight,
                notebookId = notebookId
            )
            message.value = "Subject added"
        }
    }

    fun regenerateTodayPlan() {
        viewModelScope.launch {
            val count = examPlanRepository.regenerateDailyPlan(todayEpochDay)
            generationCount.value = count
            message.value = "Generated $count items for today"
        }
    }

    fun consumeMessage() {
        message.value = null
    }
}
