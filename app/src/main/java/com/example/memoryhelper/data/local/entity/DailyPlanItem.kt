package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

object DailyPlanPriority {
    const val MUST_DO = 0
    const val RECOMMENDED = 1
    const val OPTIONAL = 2
}

object DailyPlanStatus {
    const val PENDING = 0
    const val DONE = 1
    const val SKIPPED = 2
}

@Entity(
    tableName = "daily_plan_item",
    foreignKeys = [
        ForeignKey(
            entity = ExamPlan::class,
            parentColumns = ["id"],
            childColumns = ["exam_plan_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemoryItem::class,
            parentColumns = ["id"],
            childColumns = ["memory_item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ExamSubject::class,
            parentColumns = ["id"],
            childColumns = ["exam_subject_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["plan_date"]),
        Index(value = ["priority"]),
        Index(value = ["status"]),
        Index(value = ["exam_plan_id"]),
        Index(value = ["memory_item_id"]),
        Index(value = ["exam_subject_id"])
    ]
)
data class DailyPlanItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "exam_plan_id")
    val examPlanId: Long,
    @ColumnInfo(name = "plan_date")
    val planDate: Long,
    @ColumnInfo(name = "memory_item_id")
    val memoryItemId: Long,
    @ColumnInfo(name = "exam_subject_id")
    val examSubjectId: Long? = null,
    @ColumnInfo(name = "priority")
    val priority: Int = DailyPlanPriority.RECOMMENDED,
    @ColumnInfo(name = "status")
    val status: Int = DailyPlanStatus.PENDING,
    @ColumnInfo(name = "scheduled_at")
    val scheduledAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
