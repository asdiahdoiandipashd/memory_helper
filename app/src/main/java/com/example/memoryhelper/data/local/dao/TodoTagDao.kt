package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.TodoTag
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TodoTag): Long

    @Update
    suspend fun update(tag: TodoTag)

    @Delete
    suspend fun delete(tag: TodoTag)

    @Query("SELECT * FROM todo_tags ORDER BY name ASC")
    fun getAllTagsFlow(): Flow<List<TodoTag>>

    @Query("SELECT * FROM todo_tags ORDER BY name ASC")
    suspend fun getAllTags(): List<TodoTag>

    @Query("SELECT * FROM todo_tags WHERE id = :id")
    suspend fun getById(id: Long): TodoTag?
}
