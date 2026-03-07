package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.ExamSubject
import kotlinx.coroutines.flow.Flow

@Dao
interface ExamSubjectDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subject: ExamSubject): Long

    @Update
    suspend fun update(subject: ExamSubject)

    @Delete
    suspend fun delete(subject: ExamSubject)

    @Query("SELECT * FROM exam_subject WHERE exam_plan_id = :planId ORDER BY weight DESC")
    fun getByPlanFlow(planId: Long): Flow<List<ExamSubject>>

    @Query("SELECT * FROM exam_subject WHERE exam_plan_id = :planId ORDER BY weight DESC")
    suspend fun getByPlan(planId: Long): List<ExamSubject>
}
