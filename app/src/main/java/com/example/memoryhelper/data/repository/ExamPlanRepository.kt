package com.example.memoryhelper.data.repository

import com.example.memoryhelper.data.local.dao.DailyPlanItemDao
import com.example.memoryhelper.data.local.dao.ExamPlanDao
import com.example.memoryhelper.data.local.dao.ExamSubjectDao
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.entity.DailyPlanItem
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.domain.plan.PlanContext
import com.example.memoryhelper.domain.plan.PlanEngine
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class ExamPlanRepository @Inject constructor(
    private val examPlanDao: ExamPlanDao,
    private val examSubjectDao: ExamSubjectDao,
    private val dailyPlanItemDao: DailyPlanItemDao,
    private val memoryItemDao: MemoryItemDao,
    private val planEngine: PlanEngine
) {

    fun getPlansFlow(): Flow<List<ExamPlan>> = examPlanDao.getAllFlow()

    suspend fun createPlan(
        name: String,
        examDateEpochDay: Long,
        dailyBudget: Int,
        overdueCompensationLimit: Int
    ): Long {
        return examPlanDao.insert(
            ExamPlan(
                name = name,
                examDateEpochDay = examDateEpochDay,
                dailyBudget = dailyBudget,
                overdueCompensationLimit = overdueCompensationLimit
            )
        )
    }

    suspend fun addSubject(
        planId: Long,
        name: String,
        weight: Float,
        notebookId: Long?
    ): Long {
        return examSubjectDao.insert(
            ExamSubject(
                examPlanId = planId,
                name = name,
                weight = weight,
                notebookId = notebookId
            )
        )
    }

    suspend fun regenerateDailyPlan(dateEpochDay: Long = LocalDate.now().toEpochDay()): Int {
        val plan = examPlanDao.getActivePlan() ?: return 0
        val zone = ZoneId.systemDefault()
        val dayEnd = LocalDate.ofEpochDay(dateEpochDay).plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        val subjects = examSubjectDao.getByPlan(plan.id)
        val dueItems = memoryItemDao.getDueItemsBefore(dayEnd)

        val computed = planEngine.generate(
            dateEpochDay = dateEpochDay,
            context = PlanContext(
                plan = plan,
                subjects = subjects,
                dueItems = dueItems,
                now = System.currentTimeMillis()
            )
        )

        dailyPlanItemDao.deleteByPlanAndDate(plan.id, dateEpochDay)
        dailyPlanItemDao.insertAll(
            computed.items.map {
                DailyPlanItem(
                    examPlanId = plan.id,
                    planDate = dateEpochDay,
                    memoryItemId = it.memoryItemId,
                    examSubjectId = it.subjectId,
                    priority = it.priority
                )
            }
        )
        return computed.items.size
    }

    fun getDailyPlanFlow(planId: Long, dateEpochDay: Long): Flow<List<DailyPlanItem>> {
        return dailyPlanItemDao.getByPlanAndDateFlow(planId, dateEpochDay)
    }

    suspend fun getActivePlan(): ExamPlan? = examPlanDao.getActivePlan()

    fun getSubjectsFlow(planId: Long): Flow<List<ExamSubject>> = examSubjectDao.getByPlanFlow(planId)
}
