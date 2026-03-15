package com.example.memoryhelper.domain.plan

import com.example.memoryhelper.data.local.entity.DailyPlanPriority
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import java.time.LocalDate
import java.time.ZoneId
import org.junit.Assert.assertEquals
import org.junit.Test

class DefaultPlanEngineTest {

    private val engine = DefaultPlanEngine()
    private val zone = ZoneId.systemDefault()

    @Test
    fun overdue_items_are_prioritized_before_today_items() {
        val date = LocalDate.of(2026, 3, 15)
        val plan = ExamPlan(
            id = 1L,
            name = "Exam",
            examDateEpochDay = date.plusDays(60).toEpochDay(),
            dailyBudget = 2,
            overdueCompensationLimit = 1
        )
        val subject = ExamSubject(
            id = 10L,
            examPlanId = plan.id,
            name = "Law",
            weight = 1f,
            notebookId = 100L
        )

        val result = engine.generate(
            dateEpochDay = date.toEpochDay(),
            context = PlanContext(
                plan = plan,
                subjects = listOf(subject),
                dueItems = listOf(
                    dueItem(id = 1L, notebookId = 100L, dueTime = dayStart(date.minusDays(1), 9)),
                    dueItem(id = 2L, notebookId = 100L, dueTime = dayStart(date.minusDays(1), 12)),
                    dueItem(id = 3L, notebookId = 100L, dueTime = dayStart(date, 10)),
                    dueItem(id = 4L, notebookId = 100L, dueTime = dayStart(date, 14))
                ),
                now = dayStart(date, 8)
            )
        )

        assertEquals(3, result.capacity)
        assertEquals(listOf(1L, 2L, 3L), result.items.take(3).map { it.memoryItemId })
        assertEquals(
            listOf(
                DailyPlanPriority.MUST_DO,
                DailyPlanPriority.MUST_DO,
                DailyPlanPriority.RECOMMENDED
            ),
            result.items.take(3).map { it.priority }
        )
    }

    @Test
    fun weighted_subjects_shape_recommended_allocation() {
        val date = LocalDate.of(2026, 3, 15)
        val plan = ExamPlan(
            id = 1L,
            name = "Exam",
            examDateEpochDay = date.plusDays(30).toEpochDay(),
            dailyBudget = 4,
            overdueCompensationLimit = 0
        )
        val subjectA = ExamSubject(
            id = 10L,
            examPlanId = plan.id,
            name = "Subject A",
            weight = 3f,
            notebookId = 100L
        )
        val subjectB = ExamSubject(
            id = 20L,
            examPlanId = plan.id,
            name = "Subject B",
            weight = 1f,
            notebookId = 200L
        )

        val result = engine.generate(
            dateEpochDay = date.toEpochDay(),
            context = PlanContext(
                plan = plan,
                subjects = listOf(subjectA, subjectB),
                dueItems = listOf(
                    dueItem(id = 1L, notebookId = 100L, dueTime = dayStart(date, 8)),
                    dueItem(id = 2L, notebookId = 100L, dueTime = dayStart(date, 9)),
                    dueItem(id = 3L, notebookId = 100L, dueTime = dayStart(date, 10)),
                    dueItem(id = 4L, notebookId = 100L, dueTime = dayStart(date, 11)),
                    dueItem(id = 5L, notebookId = 200L, dueTime = dayStart(date, 8)),
                    dueItem(id = 6L, notebookId = 200L, dueTime = dayStart(date, 9))
                ),
                now = dayStart(date, 7)
            )
        )

        val recommended = result.items.filter { it.priority == DailyPlanPriority.RECOMMENDED }

        assertEquals(4, recommended.size)
        assertEquals(3, recommended.count { it.subjectId == subjectA.id })
        assertEquals(1, recommended.count { it.subjectId == subjectB.id })
    }

    private fun dueItem(id: Long, notebookId: Long, dueTime: Long): MemoryItem {
        return MemoryItem(
            id = id,
            notebookId = notebookId,
            title = "Item $id",
            content = "Content $id",
            status = MemoryItemStatus.REVIEWING,
            stageIndex = 0,
            nextReviewTime = dueTime,
            lastReviewTime = 0L,
            createdAt = 0L,
            updatedAt = 0L
        )
    }

    private fun dayStart(date: LocalDate, hour: Int): Long {
        return date.atTime(hour, 0).atZone(zone).toInstant().toEpochMilli()
    }
}
