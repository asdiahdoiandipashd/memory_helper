package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.TodoTask
import com.example.memoryhelper.data.local.entity.TodoTaskTagCrossRef
import com.example.memoryhelper.data.local.entity.TodoTaskWithTags
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoTaskDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TodoTask): Long

    @Update
    suspend fun update(task: TodoTask)

    @Delete
    suspend fun delete(task: TodoTask)

    @Transaction
    @Query("SELECT * FROM todo_tasks ORDER BY updated_at DESC")
    fun getAllTasksWithTagsFlow(): Flow<List<TodoTaskWithTags>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<TodoTaskTagCrossRef>)

    @Query("DELETE FROM todo_task_tag_cross_ref WHERE task_id = :taskId")
    suspend fun deleteCrossRefsForTask(taskId: Long)

    @Transaction
    suspend fun replaceTaskTags(taskId: Long, tagIds: List<Long>) {
        deleteCrossRefsForTask(taskId)
        if (tagIds.isNotEmpty()) {
            val crossRefs = tagIds.map { tagId ->
                TodoTaskTagCrossRef(taskId = taskId, tagId = tagId)
            }
            insertCrossRefs(crossRefs)
        }
    }
}
