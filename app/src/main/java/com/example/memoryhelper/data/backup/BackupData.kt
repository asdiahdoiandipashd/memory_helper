package com.example.memoryhelper.data.backup

import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.data.local.entity.ReviewCurve
import com.example.memoryhelper.data.local.entity.ReviewLog
import kotlinx.serialization.Serializable

/**
 * Backup data container for JSON serialization.
 * Contains all app data needed for complete backup/restore.
 */
@Serializable
data class BackupData(
    val items: List<MemoryItemDto>,
    val notebooks: List<NotebookDto>,
    val logs: List<ReviewLogDto>,
    val curves: List<ReviewCurveDto>,
    val version: Int = 1,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * DTO for MemoryItem entity
 */
@Serializable
data class MemoryItemDto(
    val id: Long,
    val notebookId: Long,
    val curveId: Long?,
    val title: String,
    val content: String,
    val imagePaths: String,
    val status: Int,
    val stageIndex: Int,
    val nextReviewTime: Long,
    val lastReviewTime: Long,
    val createdAt: Long
) {
    fun toEntity(): MemoryItem = MemoryItem(
        id = id,
        notebookId = notebookId,
        curveId = curveId,
        title = title,
        content = content,
        imagePaths = imagePaths,
        status = status,
        stageIndex = stageIndex,
        nextReviewTime = nextReviewTime,
        lastReviewTime = lastReviewTime,
        createdAt = createdAt
    )

    companion object {
        fun fromEntity(entity: MemoryItem): MemoryItemDto = MemoryItemDto(
            id = entity.id,
            notebookId = entity.notebookId,
            curveId = entity.curveId,
            title = entity.title,
            content = entity.content,
            imagePaths = entity.imagePaths,
            status = entity.status,
            stageIndex = entity.stageIndex,
            nextReviewTime = entity.nextReviewTime,
            lastReviewTime = entity.lastReviewTime,
            createdAt = entity.createdAt
        )
    }
}

/**
 * DTO for Notebook entity
 */
@Serializable
data class NotebookDto(
    val id: Long,
    val name: String,
    val color: Int
) {
    fun toEntity(): Notebook = Notebook(
        id = id,
        name = name,
        color = color
    )

    companion object {
        fun fromEntity(entity: Notebook): NotebookDto = NotebookDto(
            id = entity.id,
            name = entity.name,
            color = entity.color
        )
    }
}

/**
 * DTO for ReviewLog entity
 */
@Serializable
data class ReviewLogDto(
    val id: Long,
    val itemId: Long,
    val actualReviewTime: Long,
    val plannedReviewTime: Long,
    val reviewAction: Int
) {
    fun toEntity(): ReviewLog = ReviewLog(
        id = id,
        itemId = itemId,
        actualReviewTime = actualReviewTime,
        plannedReviewTime = plannedReviewTime,
        reviewAction = reviewAction
    )

    companion object {
        fun fromEntity(entity: ReviewLog): ReviewLogDto = ReviewLogDto(
            id = entity.id,
            itemId = entity.itemId,
            actualReviewTime = entity.actualReviewTime,
            plannedReviewTime = entity.plannedReviewTime,
            reviewAction = entity.reviewAction
        )
    }
}

/**
 * DTO for ReviewCurve entity
 */
@Serializable
data class ReviewCurveDto(
    val id: Long,
    val name: String,
    val intervalsJson: String,
    val isDefault: Boolean
) {
    fun toEntity(): ReviewCurve = ReviewCurve(
        id = id,
        name = name,
        intervalsJson = intervalsJson,
        isDefault = isDefault
    )

    companion object {
        fun fromEntity(entity: ReviewCurve): ReviewCurveDto = ReviewCurveDto(
            id = entity.id,
            name = entity.name,
            intervalsJson = entity.intervalsJson,
            isDefault = entity.isDefault
        )
    }
}
