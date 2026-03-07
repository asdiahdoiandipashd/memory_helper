package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.SyncEvent

@Dao
interface SyncEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: SyncEvent): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<SyncEvent>)

    @Update
    suspend fun update(event: SyncEvent)

    @Query("SELECT * FROM sync_event WHERE synced_at IS NULL ORDER BY id ASC LIMIT :limit")
    suspend fun getPendingEvents(limit: Int = 200): List<SyncEvent>
}
