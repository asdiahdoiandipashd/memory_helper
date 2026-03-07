package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.memoryhelper.data.local.entity.MemoryItemTagCrossRef
import com.example.memoryhelper.data.local.entity.MemoryItemWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryItemTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<MemoryItemTagCrossRef>)

    @Query("DELETE FROM memory_item_tag_cross_ref WHERE memory_item_id = :itemId")
    suspend fun deleteCrossRefsForItem(itemId: Long)

    @Transaction
    @Query("SELECT * FROM memory_items WHERE id = :itemId")
    suspend fun getItemWithTags(itemId: Long): MemoryItemWithTags?

    @Transaction
    @Query("SELECT * FROM memory_items WHERE deleted_at IS NULL ORDER BY next_review_time ASC")
    fun getAllItemsWithTagsFlow(): Flow<List<MemoryItemWithTags>>

    @Transaction
    suspend fun replaceItemTags(itemId: Long, tagIds: List<Long>) {
        deleteCrossRefsForItem(itemId)
        if (tagIds.isNotEmpty()) {
            insertCrossRefs(tagIds.map { MemoryItemTagCrossRef(memoryItemId = itemId, tagId = it) })
        }
    }
}
