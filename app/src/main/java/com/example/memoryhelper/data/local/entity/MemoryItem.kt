package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * MemoryItem status constants
 */
object MemoryItemStatus {
    const val NEW = 0
    const val REVIEWING = 1
    const val COMPLETED = 2
    const val PAUSED = 3
}

/**
 * MemoryItem entity - the core content to be memorized.
 * Contains the actual content (text/images) and tracks review progress.
 */
@Entity(
    tableName = "memory_items",
    foreignKeys = [
        ForeignKey(
            entity = ReviewCurve::class,
            parentColumns = ["id"],
            childColumns = ["curve_id"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = Notebook::class,
            parentColumns = ["id"],
            childColumns = ["notebook_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["next_review_time"]),
        Index(value = ["curve_id"]),
        Index(value = ["notebook_id"])
    ]
)
data class MemoryItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "notebook_id")
    val notebookId: Long = 1,

    @ColumnInfo(name = "curve_id")
    val curveId: Long? = null,

    @ColumnInfo(name = "title")
    val title: String,

    /**
     * Content with Markdown support
     */
    @ColumnInfo(name = "content")
    val content: String,

    /**
     * JSON List of local file paths for images
     */
    @ColumnInfo(name = "image_paths")
    val imagePaths: String = "[]",

    /**
     * Status: 0=New, 1=Reviewing, 2=Completed, 3=Paused
     */
    @ColumnInfo(name = "status")
    val status: Int = MemoryItemStatus.NEW,

    /**
     * Current index in the curve interval list
     */
    @ColumnInfo(name = "stage_index")
    val stageIndex: Int = 0,

    /**
     * Timestamp for NEXT alarm (milliseconds)
     */
    @ColumnInfo(name = "next_review_time")
    val nextReviewTime: Long = 0L,

    /**
     * Last review timestamp (milliseconds)
     */
    @ColumnInfo(name = "last_review_time")
    val lastReviewTime: Long = 0L,

    /**
     * Creation timestamp (milliseconds)
     */
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
