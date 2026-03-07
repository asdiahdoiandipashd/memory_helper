package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.ExamPlan
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: ExamPlan): Long

    @Update
    suspend fun update(plan: ExamPlan)

    @Delete
    suspend fun delete(plan: ExamPlan)

    @Query("SELECT * FROM exam_plan ORDER BY updated_at DESC")
    fun getAllFlow(): Flow<List<ExamPlan>>

    @Query("SELECT * FROM exam_plan WHERE is_active = 1 ORDER BY updated_at DESC LIMIT 1")
    suspend fun getActivePlan(): ExamPlan?

    @Query("SELECT * FROM exam_plan WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ExamPlan?
}
