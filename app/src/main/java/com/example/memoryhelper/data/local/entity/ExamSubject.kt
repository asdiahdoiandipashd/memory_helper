package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "exam_subject",
    foreignKeys = [
        ForeignKey(
            entity = ExamPlan::class,
            parentColumns = ["id"],
            childColumns = ["exam_plan_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebook_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["exam_plan_id"]),
        Index(value = ["notebook_id"])
    ]
)
data class ExamSubject(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exam_plan_id")
    val examPlanId: Long,
    @ColumnInfo(name = "name")
    val name: String,
    @ColumnInfo(name = "weight")
    val weight: Float = 1f,
    @ColumnInfo(name = "notebook_id")
    val notebookId: Long? = null,
    @ColumnInfo(name = "daily_budget_override")
    val dailyBudgetOverride: Int? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
