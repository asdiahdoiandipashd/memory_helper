package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.MemoryTag
import kotlinx.coroutines.flow.Flow

@Dao
interface MemoryTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: MemoryTag): Long

    @Update
    suspend fun update(tag: MemoryTag)

    @Delete
    suspend fun delete(tag: MemoryTag)

    @Query("SELECT * FROM memory_tag ORDER BY name ASC")
    fun getAllFlow(): Flow<List<MemoryTag>>

    @Query("SELECT * FROM memory_tag WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): MemoryTag?
}
