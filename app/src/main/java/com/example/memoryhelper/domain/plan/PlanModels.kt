package com.example.memoryhelper.domain.plan

import com.example.memoryhelper.data.local.entity.DailyPlanPriority
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.MemoryItem

data class PlanContext(
    val plan: ExamPlan,
    val subjects: List<ExamSubject>,
    val dueItems: List<MemoryItem>,
    val now: Long
)

data class PlannedItem(
    val memoryItemId: Long,
    val subjectId: Long?,
    val priority: Int = DailyPlanPriority.RECOMMENDED
)

data class DailyPlan(
    val dateEpochDay: Long,
    val capacity: Int,
    val items: List<PlannedItem>
)
