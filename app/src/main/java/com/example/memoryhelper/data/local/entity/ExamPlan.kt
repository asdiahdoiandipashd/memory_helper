package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_plan")
data class ExamPlan(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "exam_date_epoch_day")
    val examDateEpochDay: Long,
    @ColumnInfo(name = "daily_budget")
    val dailyBudget: Int = 30,
    @ColumnInfo(name = "overdue_compensation_limit")
    val overdueCompensationLimit: Int = 15,
    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
