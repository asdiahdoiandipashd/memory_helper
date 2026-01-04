package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * ReviewLog entity - records each review action for statistics.
 * Used for generating charts and analyzing review patterns.
 */
@Entity(
    tableName = "review_logs",
    indices = [
        Index(value = ["item_id"]),
        Index(value = ["actual_review_time"])
    ]
)
data class ReviewLog(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "item_id")
    val itemId: Long,

    /**
     * The actual time when the user clicked review
     */
    @ColumnInfo(name = "actual_review_time")
    val actualReviewTime: Long,

    /**
     * The originally planned review time
     */
    @ColumnInfo(name = "planned_review_time")
    val plannedReviewTime: Long,

    /**
     * Review action: 1 = Remembered, 2 = Forgot
     */
    @ColumnInfo(name = "review_action")
    val reviewAction: Int
)

/**
 * Review action constants
 */
object ReviewAction {
    const val REMEMBERED = 1
    const val FORGOT = 2
}
