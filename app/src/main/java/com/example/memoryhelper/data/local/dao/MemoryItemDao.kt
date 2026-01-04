package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.MemoryItem
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for MemoryItem entity.
 */
@Dao
interface MemoryItemDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MemoryItem): Long

    @Update
    suspend fun update(item: MemoryItem)

    @Delete
    suspend fun delete(item: MemoryItem)

    @Query("SELECT * FROM memory_items WHERE id = :id")
    suspend fun getById(id: Long): MemoryItem?

    @Query("SELECT * FROM memory_items ORDER BY created_at DESC")
    suspend fun getAllItems(): List<MemoryItem>

    @Query("SELECT * FROM memory_items ORDER BY next_review_time ASC")
    fun getAllItemsFlow(): Flow<List<MemoryItem>>

    @Query("SELECT * FROM memory_items WHERE status = :status ORDER BY next_review_time ASC")
    fun getItemsByStatus(status: Int): Flow<List<MemoryItem>>

    /**
     * Get items that are due for review (next_review_time <= current time)
     * Only includes items with REVIEWING status
     */
    @Query("""
        SELECT * FROM memory_items
        WHERE status = 1 AND next_review_time <= :currentTime
        ORDER BY next_review_time ASC
    """)
    fun getDueItems(currentTime: Long): Flow<List<MemoryItem>>

    /**
     * Get the minimum next_review_time for scheduling the next alarm.
     * Only considers items that are in REVIEWING status.
     */
    @Query("""
        SELECT MIN(next_review_time) FROM memory_items
        WHERE status = 1 AND next_review_time > :currentTime
    """)
    suspend fun getNextAlarmTime(currentTime: Long): Long?

    /**
     * Count items by status
     */
    @Query("SELECT COUNT(*) FROM memory_items WHERE status = :status")
    suspend fun countByStatus(status: Int): Int

    /**
     * Search items by title or content
     */
    @Query("""
        SELECT * FROM memory_items
        WHERE title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%'
        ORDER BY created_at DESC
    """)
    fun searchItems(query: String): Flow<List<MemoryItem>>

    /**
     * Count items that are due for review (for alarm notifications).
     */
    @Query("""
        SELECT COUNT(*) FROM memory_items
        WHERE status = 1 AND next_review_time <= :currentTime
    """)
    suspend fun countDueItems(currentTime: Long): Int
}
