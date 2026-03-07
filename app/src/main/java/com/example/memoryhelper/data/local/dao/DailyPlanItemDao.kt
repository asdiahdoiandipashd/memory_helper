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
}
