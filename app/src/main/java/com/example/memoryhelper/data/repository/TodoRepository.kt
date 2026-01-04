package com.example.memoryhelper.data.repository

import com.example.memoryhelper.data.local.dao.TodoTagDao
import com.example.memoryhelper.data.local.dao.TodoTaskDao
import com.example.memoryhelper.data.local.entity.TodoTag
import com.example.memoryhelper.data.local.entity.TodoTask
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoRepository @Inject constructor(
    private val todoTaskDao: TodoTaskDao,
    private val todoTagDao: TodoTagDao
) {
    fun getAllTasksWithTagsFlow() = todoTaskDao.getAllTasksWithTagsFlow()

    fun getAllTagsFlow(): Flow<List<TodoTag>> = todoTagDao.getAllTagsFlow()

    suspend fun addTask(
        title: String,
        note: String,
        isDaily: Boolean,
        tagIds: List<Long>
    ): Long {
        val now = System.currentTimeMillis()
        val taskId = todoTaskDao.insert(
            TodoTask(
                title = title,
                note = note,
                isDaily = isDaily,
                createdAt = now,
                updatedAt = now
            )
        )
        todoTaskDao.replaceTaskTags(taskId, tagIds)
        return taskId
    }

    suspend fun updateTask(
        task: TodoTask,
        tagIds: List<Long>
    ) {
        val updatedTask = task.copy(updatedAt = System.currentTimeMillis())
        todoTaskDao.update(updatedTask)
        todoTaskDao.replaceTaskTags(updatedTask.id, tagIds)
    }

    suspend fun deleteTask(task: TodoTask) {
        todoTaskDao.delete(task)
    }

    suspend fun setTaskCompleted(
        task: TodoTask,
        completed: Boolean,
        todayEpochDay: Long
    ) {
        val now = System.currentTimeMillis()
        val updatedTask = task.copy(
            isCompleted = completed,
            lastCompletedDay = if (completed) todayEpochDay else null,
            lastCompletedAt = if (completed) now else null,
            updatedAt = now
        )
        todoTaskDao.update(updatedTask)
    }

    suspend fun addTag(name: String): Long {
        return todoTagDao.insert(TodoTag(name = name))
    }

    suspend fun updateTag(tag: TodoTag) {
        todoTagDao.update(tag)
    }

    suspend fun deleteTag(tag: TodoTag) {
        todoTagDao.delete(tag)
    }
}
