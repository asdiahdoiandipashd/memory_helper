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
    val reviewAction: Int,

    /**
     * Review grade: 1 = Again, 2 = Hard, 3 = Good, 4 = Easy.
     */
    @ColumnInfo(name = "grade")
    val grade: Int = ReviewGrade.GOOD,

    /**
     * User response time in milliseconds.
     */
    @ColumnInfo(name = "response_ms")
    val responseMs: Long = 0L,

    /**
     * actual_review_time - planned_review_time in milliseconds.
     */
    @ColumnInfo(name = "due_delta_ms")
    val dueDeltaMs: Long = 0L,

    /**
     * Scheduler algorithm version.
     */
    @ColumnInfo(name = "scheduler_version")
    val schedulerVersion: String = "v2"
)

/**
 * Review action constants
 */
object ReviewAction {
    const val REMEMBERED = 1
    const val FORGOT = 2
}

object ReviewGrade {
    const val AGAIN = 1
    const val HARD = 2
    const val GOOD = 3
    const val EASY = 4
}
