package com.example.memoryhelper.domain.scheduler

data class ScheduleResult(
    val nextReviewTime: Long,
    val newStageIndex: Int,
    val completed: Boolean,
    val stabilitySnapshot: Float
)
