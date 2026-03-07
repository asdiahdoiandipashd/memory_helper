package com.example.memoryhelper.domain.plan

interface PlanEngine {
    fun generate(dateEpochDay: Long, context: PlanContext): DailyPlan
}
