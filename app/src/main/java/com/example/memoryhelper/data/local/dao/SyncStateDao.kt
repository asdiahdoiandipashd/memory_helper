package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.SyncState

@Dao
interface SyncStateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: SyncState)

    @Update
    suspend fun update(state: SyncState)

    @Query("SELECT * FROM sync_state WHERE id = 1 LIMIT 1")
    suspend fun getState(): SyncState?
}
