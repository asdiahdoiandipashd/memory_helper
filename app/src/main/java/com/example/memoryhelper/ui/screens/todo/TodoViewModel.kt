package com.example.memoryhelper.ui.screens.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryhelper.data.local.entity.TodoTag
import com.example.memoryhelper.data.local.entity.TodoTask
import com.example.memoryhelper.data.local.entity.TodoTaskWithTags
import com.example.memoryhelper.data.repository.TodoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TodoDisplayItem(
    val task: TodoTask,
    val tags: List<TodoTag>,
    val isCompleted: Boolean
)

data class TodoUiState(
    val dailyTasks: List<TodoDisplayItem> = emptyList(),
    val activeTasks: List<TodoDisplayItem> = emptyList(),
    val completedTasks: List<TodoDisplayItem> = emptyList(),
    val tags: List<TodoTag> = emptyList(),
    val selectedTagId: Long? = null
) {
    val isEmpty: Boolean
        get() = dailyTasks.isEmpty() && activeTasks.isEmpty() && completedTasks.isEmpty()
}

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repository: TodoRepository
) : ViewModel() {

    private val selectedTagId = MutableStateFlow<Long?>(null)

    private val currentDayFlow = flow {
        while (true) {
            val zone = ZoneId.systemDefault()
            val today = LocalDate.now(zone)
            emit(today.toEpochDay())
            val nextDayStart = today.plusDays(1).atStartOfDay(zone).toInstant()
            val delayMillis = (nextDayStart.toEpochMilli() - Instant.now().toEpochMilli())
                .coerceAtLeast(60_000L)
            delay(delayMillis)
        }
    }

    val uiState: StateFlow<TodoUiState> = combine(
        repository.getAllTasksWithTagsFlow(),
        repository.getAllTagsFlow(),
        selectedTagId,
        currentDayFlow
    ) { tasks, tags, selectedTag, todayEpochDay ->
        buildUiState(tasks, tags, selectedTag, todayEpochDay)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TodoUiState()
    )

    fun selectTag(tagId: Long?) {
        selectedTagId.value = tagId
    }

    fun addTask(title: String, note: String, isDaily: Boolean, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.addTask(title, note, isDaily, tagIds)
        }
    }

    fun updateTask(task: TodoTask, title: String, note: String, isDaily: Boolean, tagIds: List<Long>) {
        viewModelScope.launch {
            repository.updateTask(
                task.copy(
                    title = title,
                    note = note,
                    isDaily = isDaily
                ),
                tagIds
            )
        }
    }

    fun toggleTaskCompletion(item: TodoDisplayItem) {
        viewModelScope.launch {
            val todayEpochDay = LocalDate.now(ZoneId.systemDefault()).toEpochDay()
            repository.setTaskCompleted(
                task = item.task,
                completed = !item.isCompleted,
                todayEpochDay = todayEpochDay
            )
        }
    }

    fun deleteTask(task: TodoTask) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addTag(name: String) {
        viewModelScope.launch {
            repository.addTag(name)
        }
    }

    fun renameTag(tag: TodoTag, newName: String) {
        viewModelScope.launch {
            repository.updateTag(tag.copy(name = newName))
        }
    }

    fun deleteTag(tag: TodoTag) {
        viewModelScope.launch {
            repository.deleteTag(tag)
            if (selectedTagId.value == tag.id) {
                selectedTagId.value = null
            }
        }
    }

    private fun buildUiState(
        tasks: List<TodoTaskWithTags>,
        tags: List<TodoTag>,
        selectedTag: Long?,
        todayEpochDay: Long
    ): TodoUiState {
        val filtered = if (selectedTag == null) {
            tasks
        } else {
            tasks.filter { item ->
                item.tags.any { tag -> tag.id == selectedTag }
            }
        }

        val displayItems = filtered.map { item ->
            TodoDisplayItem(
                task = item.task,
                tags = item.tags,
                isCompleted = isCompletedForToday(item.task, todayEpochDay)
            )
        }

        val daily = displayItems
            .filter { it.task.isDaily }
            .sortedWith(compareBy<TodoDisplayItem> { it.isCompleted }
                .thenByDescending { it.task.updatedAt })

        val regular = displayItems.filter { !it.task.isDaily }

        val active = regular.filter { !it.isCompleted }
            .sortedByDescending { it.task.updatedAt }

        val completed = regular.filter { it.isCompleted }
            .sortedByDescending { it.task.updatedAt }

        return TodoUiState(
            dailyTasks = daily,
            activeTasks = active,
            completedTasks = completed,
            tags = tags,
            selectedTagId = selectedTag
        )
    }

    private fun isCompletedForToday(task: TodoTask, todayEpochDay: Long): Boolean {
        return if (task.isDaily) {
            task.lastCompletedDay == todayEpochDay
        } else {
            task.isCompleted
        }
    }
}
