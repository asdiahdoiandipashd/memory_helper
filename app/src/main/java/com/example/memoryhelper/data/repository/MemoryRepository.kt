package com.example.memoryhelper.data.repository

import com.example.memoryhelper.alarm.AlarmScheduler
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import com.example.memoryhelper.data.local.entity.ReviewAction
import com.example.memoryhelper.data.local.entity.ReviewCurve
import com.example.memoryhelper.data.local.entity.ReviewLog
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MemoryRepository @Inject constructor(
    private val reviewCurveDao: ReviewCurveDao,
    private val memoryItemDao: MemoryItemDao,
    private val reviewLogDao: ReviewLogDao,
    private val notebookDao: NotebookDao,
    private val alarmScheduler: AlarmScheduler
) {
    private val json = Json { ignoreUnknownKeys = true }

    companion object {
        private val STANDARD_INTERVALS = listOf(5L, 30L, 720L, 1440L, 2880L, 5760L, 10080L, 21600L)
    }

    fun getAllItemsFlow(): Flow<List<MemoryItem>> {
        return memoryItemDao.getAllItemsFlow()
    }

    fun searchItems(query: String): Flow<List<MemoryItem>> {
        return memoryItemDao.searchItems(query)
    }

    /**
     * Get today's review count as a Flow for real-time progress tracking.
     */
    fun getTodayReviewCountFlow(): Flow<Int> {
        val startOfToday = getStartOfToday()
        return reviewLogDao.countReviewsTodayFlow(startOfToday)
    }

    /**
     * Get the start of today (midnight) as a timestamp.
     */
    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get the end of today (23:59:59) as a timestamp.
     */
    private fun getEndOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    suspend fun initDefaultCurves() {
        val existingCurves = reviewCurveDao.getAllCurves()
        if (existingCurves.isEmpty()) {
            val defaultCurve = ReviewCurve(
                name = "艾宾浩斯标准曲线",
                intervalsJson = json.encodeToString(STANDARD_INTERVALS),
                isDefault = true
            )
            reviewCurveDao.insert(defaultCurve)
        }

        val existingNotebooks = notebookDao.getAllNotebooks()
        if (existingNotebooks.isEmpty()) {
            val defaultNotebook = com.example.memoryhelper.data.local.entity.Notebook(
                name = "默认生词本"
            )
            notebookDao.insert(defaultNotebook)
        }
    }

    fun getAllNotebooksFlow() = notebookDao.getAllNotebooksFlow()

    suspend fun addNotebook(name: String): Long {
        val notebook = com.example.memoryhelper.data.local.entity.Notebook(name = name)
        return notebookDao.insert(notebook)
    }

    suspend fun updateNotebook(notebook: com.example.memoryhelper.data.local.entity.Notebook) {
        notebookDao.update(notebook)
    }

    suspend fun deleteNotebook(notebook: com.example.memoryhelper.data.local.entity.Notebook) {
        notebookDao.delete(notebook)
    }

    suspend fun addNewItem(title: String, content: String, notebookId: Long = 1): Long {
        val defaultCurve = reviewCurveDao.getDefaultCurve()
            ?: throw IllegalStateException("No default curve found. Call initDefaultCurves() first.")

        val intervals = parseIntervals(defaultCurve.intervalsJson)
        val firstIntervalMinutes = intervals.firstOrNull() ?: 5L
        val now = System.currentTimeMillis()
        val nextReviewTime = now + (firstIntervalMinutes * 60 * 1000)

        val newItem = MemoryItem(
            notebookId = notebookId,
            curveId = defaultCurve.id,
            title = title,
            content = content,
            status = MemoryItemStatus.REVIEWING,
            stageIndex = 0,
            nextReviewTime = nextReviewTime,
            lastReviewTime = now,
            createdAt = now
        )

        val itemId = memoryItemDao.insert(newItem)

        // Schedule the next alarm after adding a new item
        alarmScheduler.scheduleNextAlarm()

        return itemId
    }

    /**
     * Marks an item as remembered and advances to the next stage.
     *
     * Logic:
     * - If at the last stage -> Mark status as COMPLETED
     * - Else -> Increment stage_index and calculate next review time
     */
    suspend fun markAsRemembered(item: MemoryItem) {
        val curve = item.curveId?.let { reviewCurveDao.getById(it) }
        val intervals = curve?.let { parseIntervals(it.intervalsJson) } ?: STANDARD_INTERVALS
        val now = System.currentTimeMillis()

        // Log the review action
        val log = ReviewLog(
            itemId = item.id,
            actualReviewTime = now,
            plannedReviewTime = item.nextReviewTime,
            reviewAction = ReviewAction.REMEMBERED
        )
        reviewLogDao.insert(log)

        val nextStageIndex = item.stageIndex + 1

        val updatedItem = if (nextStageIndex >= intervals.size) {
            // Completed all stages - mark as completed
            item.copy(
                status = MemoryItemStatus.COMPLETED,
                lastReviewTime = now,
                nextReviewTime = Long.MAX_VALUE
            )
        } else {
            // Advance to next stage
            val nextIntervalMinutes = intervals[nextStageIndex]
            val nextReviewTime = now + (nextIntervalMinutes * 60 * 1000)

            item.copy(
                stageIndex = nextStageIndex,
                nextReviewTime = nextReviewTime,
                lastReviewTime = now
            )
        }

        memoryItemDao.update(updatedItem)

        // Schedule the next alarm after updating
        alarmScheduler.scheduleNextAlarm()
    }

    /**
     * Marks an item as forgot and resets to the first stage.
     *
     * Logic:
     * - Reset stage_index to 0
     * - Calculate next review time from the first interval
     */
    suspend fun markAsForgot(item: MemoryItem) {
        val curve = item.curveId?.let { reviewCurveDao.getById(it) }
        val intervals = curve?.let { parseIntervals(it.intervalsJson) } ?: STANDARD_INTERVALS
        val now = System.currentTimeMillis()

        // Log the review action
        val log = ReviewLog(
            itemId = item.id,
            actualReviewTime = now,
            plannedReviewTime = item.nextReviewTime,
            reviewAction = ReviewAction.FORGOT
        )
        reviewLogDao.insert(log)

        val firstIntervalMinutes = intervals.firstOrNull() ?: 5L
        val nextReviewTime = now + (firstIntervalMinutes * 60 * 1000)

        val resetItem = item.copy(
            stageIndex = 0,
            nextReviewTime = nextReviewTime,
            lastReviewTime = now,
            status = MemoryItemStatus.REVIEWING // Ensure status is REVIEWING
        )

        memoryItemDao.update(resetItem)

        // Schedule the next alarm after updating
        alarmScheduler.scheduleNextAlarm()
    }

    /**
     * Get review counts for the last 7 days.
     * Returns a map of date string (MM-dd) to review count.
     */
    suspend fun getReviewCountsForLast7Days(): Map<String, Int> {
        val dateFormat = SimpleDateFormat("MM-dd", Locale.getDefault())
        val result = mutableMapOf<String, Int>()

        val calendar = Calendar.getInstance()

        // Initialize with zeros for the last 7 days
        for (i in 6 downTo 0) {
            calendar.timeInMillis = System.currentTimeMillis()
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val dateKey = dateFormat.format(calendar.time)
            result[dateKey] = 0
        }

        // Get start of 7 days ago (midnight)
        calendar.timeInMillis = System.currentTimeMillis()
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis

        // Query logs from the last 7 days
        val logs = reviewLogDao.getLogsSince(startTime)

        // Group by date
        for (log in logs) {
            val dateKey = dateFormat.format(log.actualReviewTime)
            result[dateKey] = (result[dateKey] ?: 0) + 1
        }

        return result
    }

    /**
     * Deletes a memory item from the database.
     */
    suspend fun deleteItem(item: MemoryItem) {
        memoryItemDao.delete(item)
        // Reschedule alarm in case the deleted item was the next one
        alarmScheduler.scheduleNextAlarm()
    }

    /**
     * Restores a previously deleted item (for undo functionality).
     * Re-inserts the item with its original data.
     */
    suspend fun restoreItem(item: MemoryItem): Long {
        val restoredId = memoryItemDao.insert(item)
        // Reschedule alarm in case the restored item needs a notification
        alarmScheduler.scheduleNextAlarm()
        return restoredId
    }

    private fun parseIntervals(intervalsJson: String): List<Long> {
        return try {
            json.decodeFromString(intervalsJson)
        } catch (e: Exception) {
            STANDARD_INTERVALS
        }
    }
}
