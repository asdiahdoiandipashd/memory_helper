package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memoryhelper.data.local.entity.ReviewLog

/**
 * Data Access Object for ReviewLog entity.
 * Used for tracking review history and generating statistics.
 */
@Dao
interface ReviewLogDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: ReviewLog): Long

    /**
     * Get all review logs since a specific time.
     * Used for generating statistics for a time period.
     */
    @Query("SELECT * FROM review_logs WHERE actual_review_time >= :startTime ORDER BY actual_review_time ASC")
    suspend fun getLogsSince(startTime: Long): List<ReviewLog>

    /**
     * Get review logs for a specific item.
     */
    @Query("SELECT * FROM review_logs WHERE item_id = :itemId ORDER BY actual_review_time DESC")
    suspend fun getLogsForItem(itemId: Long): List<ReviewLog>

    /**
     * Count reviews within a time range.
     */
    @Query("SELECT COUNT(*) FROM review_logs WHERE actual_review_time >= :startTime AND actual_review_time < :endTime")
    suspend fun countReviewsBetween(startTime: Long, endTime: Long): Int

    /**
     * Get total review count since a specific time.
     */
    @Query("SELECT COUNT(*) FROM review_logs WHERE actual_review_time >= :startTime")
    suspend fun countReviewsSince(startTime: Long): Int

    /**
     * Delete all logs for a specific item.
     */
    @Query("DELETE FROM review_logs WHERE item_id = :itemId")
    suspend fun deleteLogsForItem(itemId: Long)

    /**
     * Count reviews completed today (from startOfDay timestamp).
     * Used for daily progress tracking.
     */
    @Query("SELECT COUNT(*) FROM review_logs WHERE actual_review_time >= :startOfDay")
    suspend fun countReviewsToday(startOfDay: Long): Int

    /**
     * Get reviews completed today as a Flow for real-time updates.
     */
    @Query("SELECT COUNT(*) FROM review_logs WHERE actual_review_time >= :startOfDay")
    fun countReviewsTodayFlow(startOfDay: Long): kotlinx.coroutines.flow.Flow<Int>

    /**
     * Get all review logs.
     */
    @Query("SELECT * FROM review_logs ORDER BY actual_review_time DESC")
    suspend fun getAllLogs(): List<ReviewLog>

    /**
     * Get review log by ID.
     */
    @Query("SELECT * FROM review_logs WHERE id = :id")
    suspend fun getById(id: Long): ReviewLog?
}
