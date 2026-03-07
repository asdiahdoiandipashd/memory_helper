package com.example.memoryhelper.domain.plan

import com.example.memoryhelper.data.local.entity.DailyPlanPriority
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.MemoryItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min
import kotlin.math.roundToInt

@Singleton
class DefaultPlanEngine @Inject constructor() : PlanEngine {

    override fun generate(dateEpochDay: Long, context: PlanContext): DailyPlan {
        val zone = ZoneId.systemDefault()
        val dayStart = LocalDate.ofEpochDay(dateEpochDay).atStartOfDay(zone).toInstant().toEpochMilli()
        val dayEnd = LocalDate.ofEpochDay(dateEpochDay).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1

        val overdueItems = context.dueItems
            .filter { it.nextReviewTime < dayStart }
            .sortedBy { it.nextReviewTime }

        val todayItems = context.dueItems
            .filter { it.nextReviewTime in dayStart..dayEnd }
            .sortedBy { it.nextReviewTime }

        val capacity = context.plan.dailyBudget + min(overdueItems.size, context.plan.overdueCompensationLimit)
        val mustDo = overdueItems.take(capacity).map {
            PlannedItem(
                memoryItemId = it.id,
                subjectId = findSubjectId(context.subjects, it),
                priority = DailyPlanPriority.MUST_DO
            )
        }

        val remaining = (capacity - mustDo.size).coerceAtLeast(0)
        val recommended = allocateTodayItems(
            todayItems = todayItems,
            subjects = context.subjects,
            capacity = remaining
        )

        val selectedIds = (mustDo + recommended).map { it.memoryItemId }.toSet()
        val optional = todayItems
            .asSequence()
            .filter { !selectedIds.contains(it.id) }
            .take(30)
            .map {
                PlannedItem(
                    memoryItemId = it.id,
                    subjectId = findSubjectId(context.subjects, it),
                    priority = DailyPlanPriority.OPTIONAL
                )
            }
            .toList()

        return DailyPlan(
            dateEpochDay = dateEpochDay,
            capacity = capacity,
            items = mustDo + recommended + optional
        )
    }

    private fun allocateTodayItems(
        todayItems: List<MemoryItem>,
        subjects: List<ExamSubject>,
        capacity: Int
    ): List<PlannedItem> {
        if (capacity <= 0 || todayItems.isEmpty()) return emptyList()

        val subjectByNotebook = subjects
            .filter { it.notebookId != null }
            .associateBy { it.notebookId }

        val grouped = todayItems.groupBy { item ->
            subjectByNotebook[item.notebookId]?.id
        }.toMutableMap()

        val result = mutableListOf<PlannedItem>()
        val weightedSubjects = subjects.filter { it.weight > 0f }
        val totalWeight = weightedSubjects.sumOf { it.weight.toDouble() }.toFloat().takeIf { it > 0f } ?: 1f

        for (subject in weightedSubjects) {
            if (result.size >= capacity) break
            val subjectItems = grouped[subject.id].orEmpty()
            if (subjectItems.isEmpty()) continue

            val quota = subject.dailyBudgetOverride
                ?: ((capacity * (subject.weight / totalWeight)).roundToInt().coerceAtLeast(1))

            subjectItems.take(quota).forEach {
                if (result.size < capacity) {
                    result.add(
                        PlannedItem(
                            memoryItemId = it.id,
                            subjectId = subject.id,
                            priority = DailyPlanPriority.RECOMMENDED
                        )
                    )
                }
            }
            grouped[subject.id] = subjectItems.drop(quota)
        }

        if (result.size < capacity) {
            grouped.values
                .flatten()
                .sortedBy { it.nextReviewTime }
                .forEach { item ->
                    if (result.size >= capacity) return@forEach
                    result.add(
                        PlannedItem(
                            memoryItemId = item.id,
                            subjectId = findSubjectId(subjects, item),
                            priority = DailyPlanPriority.RECOMMENDED
                        )
                    )
                }
        }

        return result
    }

    private fun findSubjectId(subjects: List<ExamSubject>, item: MemoryItem): Long? {
        return subjects.firstOrNull { it.notebookId == item.notebookId }?.id
    }
}
