package com.example.memoryhelper.domain.scheduler

import com.example.memoryhelper.data.local.entity.MemoryItem

interface SchedulerEngine {
    fun grade(
        item: MemoryItem,
        intervalsMinutes: List<Long>,
        grade: ReviewGradeOption,
        now: Long
    ): ScheduleResult
}
