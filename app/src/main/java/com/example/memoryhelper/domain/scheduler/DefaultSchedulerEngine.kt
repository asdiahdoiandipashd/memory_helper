package com.example.memoryhelper.domain.scheduler

import com.example.memoryhelper.data.local.entity.MemoryItem
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlin.math.min

@Singleton
class DefaultSchedulerEngine @Inject constructor() : SchedulerEngine {

    override fun grade(
        item: MemoryItem,
        intervalsMinutes: List<Long>,
        grade: ReviewGradeOption,
        now: Long
    ): ScheduleResult {
        val safeIntervals = if (intervalsMinutes.isEmpty()) listOf(5L) else intervalsMinutes
        val lastIndex = safeIntervals.lastIndex

        val targetStage = when (grade) {
            ReviewGradeOption.AGAIN -> 0
            ReviewGradeOption.HARD -> min(item.stageIndex + 1, lastIndex)
            ReviewGradeOption.GOOD -> min(item.stageIndex + 1, lastIndex + 1)
            ReviewGradeOption.EASY -> min(item.stageIndex + 2, lastIndex + 1)
        }

        val completed = targetStage > lastIndex
        if (completed) {
            return ScheduleResult(
                nextReviewTime = Long.MAX_VALUE,
                newStageIndex = lastIndex,
                completed = true,
                stabilitySnapshot = 1f
            )
        }

        val multiplier = when (grade) {
            ReviewGradeOption.AGAIN -> 1.0
            ReviewGradeOption.HARD -> 1.2
            ReviewGradeOption.GOOD -> 1.0
            ReviewGradeOption.EASY -> 1.3
        }

        val intervalMinutes = max(1L, (safeIntervals[targetStage] * multiplier).toLong())
        val nextReviewTime = now + intervalMinutes * 60_000L
        val stability = ((targetStage + 1).toFloat() / safeIntervals.size.toFloat()).coerceIn(0f, 1f)

        return ScheduleResult(
            nextReviewTime = nextReviewTime,
            newStageIndex = targetStage,
            completed = false,
            stabilitySnapshot = stability
        )
    }
}
