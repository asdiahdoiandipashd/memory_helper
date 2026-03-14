package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.DailyPlanItem
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyPlanItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: DailyPlanItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<DailyPlanItem>)

    @Update
    suspend fun update(item: DailyPlanItem)

    @Query("DELETE FROM daily_plan_item WHERE exam_plan_id = :planId AND plan_date = :planDate")
    suspend fun deleteByPlanAndDate(planId: Long, planDate: Long)

    @Query("SELECT * FROM daily_plan_item WHERE exam_plan_id = :planId AND plan_date = :planDate ORDER BY priority ASC, scheduled_at ASC")
    fun getByPlanAndDateFlow(planId: Long, planDate: Long): Flow<List<DailyPlanItem>>

    @Query("SELECT COUNT(*) FROM daily_plan_item WHERE exam_plan_id = :planId AND plan_date = :planDate")
    suspend fun countByPlanAndDate(planId: Long, planDate: Long): Int

    @Query(
        """
        SELECT
            dpi.id AS id,
            dpi.memory_item_id AS memoryItemId,
            mi.title AS title,
            dpi.priority AS priority,
            dpi.status AS status,
            es.name AS subjectName
        FROM daily_plan_item dpi
        INNER JOIN memory_items mi ON mi.id = dpi.memory_item_id
        LEFT JOIN exam_subject es ON es.id = dpi.exam_subject_id
        WHERE dpi.exam_plan_id = :planId AND dpi.plan_date = :planDate
        ORDER BY dpi.priority ASC, dpi.scheduled_at ASC
        """
    )
    fun getPlanRowsFlow(planId: Long, planDate: Long): Flow<List<DailyPlanRow>>
}

data class DailyPlanRow(
    val id: Long,
    val memoryItemId: Long,
    val title: String,
    val priority: Int,
    val status: Int,
    val subjectName: String?
)
