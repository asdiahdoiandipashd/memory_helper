package com.example.memoryhelper.ui.screens.home

import android.app.Application
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memoryhelper.R
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import com.example.memoryhelper.data.repository.MemoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * Represents the daily progress for reviews.
 */
data class DailyProgress(
    val completedToday: Int = 0,
    val totalDueToday: Int = 0
) {
    val progressPercentage: Float
        get() = if (totalDueToday > 0) completedToday.toFloat() / totalDueToday else 0f

    val isAllDone: Boolean
        get() = totalDueToday > 0 && completedToday >= totalDueToday
}

/**
 * UI state for the Home screen with To-Do List structure.
 * Categorizes items into:
 * - Overdue (Inherited): Items from past days that were missed
 * - Due Today: Items due today (within today's time range)
 * - Upcoming: Items due tomorrow or later
 * - Completed: Items that finished all review stages
 */
data class HomeUiState(
    val overdueItems: List<MemoryItem> = emptyList(),
    val todayItems: List<MemoryItem> = emptyList(),
    val upcomingItems: List<MemoryItem> = emptyList(),
    val completedItems: List<MemoryItem> = emptyList(),
    val dailyProgress: DailyProgress = DailyProgress()
) {
    val allItems: List<MemoryItem>
        get() = overdueItems + todayItems + upcomingItems + completedItems

    val totalDueNow: Int
        get() = overdueItems.size + todayItems.filter { it.nextReviewTime <= System.currentTimeMillis() }.size

    val hasAnyDueItems: Boolean
        get() = overdueItems.isNotEmpty() || todayItems.any { it.nextReviewTime <= System.currentTimeMillis() }

    val isEmpty: Boolean
        get() = allItems.isEmpty()
}

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MemoryRepository,
    private val application: Application
) : ViewModel() {

    // Snackbar host state for showing undo messages
    val snackbarHostState = SnackbarHostState()

    // Recently deleted item for undo functionality
    private val _recentlyDeletedItem = MutableStateFlow<MemoryItem?>(null)
    val recentlyDeletedItem: StateFlow<MemoryItem?> = _recentlyDeletedItem.asStateFlow()

    // Search state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val isSearching: Boolean
        get() = _searchQuery.value.isNotBlank()

    // Notebook state
    val notebooks = repository.getAllNotebooksFlow().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _selectedNotebookId = MutableStateFlow<Long?>(null)
    val selectedNotebookId: StateFlow<Long?> = _selectedNotebookId.asStateFlow()

    // A flow that emits the current time every second for accurate "due" calculation
    private val currentTimeFlow = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(1000L)
        }
    }

    /**
     * UI state that combines all items with current time and today's review count
     * to create a comprehensive To-Do List view.
     */
    val uiState: StateFlow<HomeUiState> = combine(_searchQuery, _selectedNotebookId) { query, notebookId ->
        Pair(query, notebookId)
    }.flatMapLatest { (query, notebookId) ->
        val itemsFlow = if (query.isBlank()) {
            repository.getAllItemsFlow()
        } else {
            repository.searchItems(query)
        }
        combine(itemsFlow, repository.getTodayReviewCountFlow(), currentTimeFlow) { items, completedTodayCount, currentTime ->
            val filteredItems = if (notebookId != null) {
                items.filter { it.notebookId == notebookId }
            } else {
                items
            }
            categorizeItems(filteredItems, completedTodayCount, currentTime)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    init {
        viewModelScope.launch {
            repository.initDefaultCurves()
        }
    }

    /**
     * Categorizes items into overdue, today, upcoming, and completed sections.
     */
    private fun categorizeItems(
        items: List<MemoryItem>,
        completedTodayCount: Int,
        currentTime: Long
    ): HomeUiState {
        val startOfToday = getStartOfToday()
        val endOfToday = getEndOfToday()

        // Separate completed items first
        val (completed, active) = items.partition { it.status == MemoryItemStatus.COMPLETED }

        // Categorize active items
        val overdue = mutableListOf<MemoryItem>()
        val today = mutableListOf<MemoryItem>()
        val upcoming = mutableListOf<MemoryItem>()

        for (item in active) {
            when {
                // Overdue: Items due BEFORE today started (inherited from past days)
                item.nextReviewTime < startOfToday -> {
                    overdue.add(item)
                }
                // Today: Items due within today's time range
                item.nextReviewTime in startOfToday..endOfToday -> {
                    today.add(item)
                }
                // Upcoming: Items due after today
                else -> {
                    upcoming.add(item)
                }
            }
        }

        // Calculate total due today (overdue + today's items)
        val totalDueToday = overdue.size + today.size

        return HomeUiState(
            overdueItems = overdue.sortedBy { it.nextReviewTime },
            todayItems = today.sortedBy { it.nextReviewTime },
            upcomingItems = upcoming.sortedBy { it.nextReviewTime },
            completedItems = completed.sortedByDescending { it.lastReviewTime },
            dailyProgress = DailyProgress(
                completedToday = completedTodayCount,
                totalDueToday = totalDueToday + completedTodayCount // Include already completed
            )
        )
    }

    /**
     * Get the start of today (midnight) as a timestamp.
     */
    private fun getStartOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get the end of today (23:59:59) as a timestamp.
     */
    private fun getEndOfToday(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    fun addItem(title: String, content: String, notebookId: Long = _selectedNotebookId.value ?: 1) {
        viewModelScope.launch {
            repository.addNewItem(title, content, notebookId)
        }
    }

    fun selectNotebook(notebookId: Long?) {
        _selectedNotebookId.value = notebookId
    }

    fun createNotebook(name: String) {
        viewModelScope.launch {
            repository.addNotebook(name)
        }
    }

    fun renameNotebook(notebook: com.example.memoryhelper.data.local.entity.Notebook, newName: String) {
        viewModelScope.launch {
            repository.updateNotebook(notebook.copy(name = newName))
        }
    }

    fun deleteNotebook(notebook: com.example.memoryhelper.data.local.entity.Notebook) {
        viewModelScope.launch {
            repository.deleteNotebook(notebook)
            if (_selectedNotebookId.value == notebook.id) {
                _selectedNotebookId.value = null
            }
            // Show confirmation snackbar
            snackbarHostState.showSnackbar(
                message = application.getString(R.string.notebook_deleted),
                duration = SnackbarDuration.Short
            )
        }
    }

    /**
     * Marks an item as remembered and advances to the next review stage.
     */
    fun markAsRemembered(item: MemoryItem) {
        viewModelScope.launch {
            repository.markAsRemembered(item)
        }
    }

    /**
     * Marks an item as forgot and resets to the first review stage.
     */
    fun markAsForgot(item: MemoryItem) {
        viewModelScope.launch {
            repository.markAsForgot(item)
        }
    }

    /**
     * Deletes an item and shows a snackbar with undo option.
     */
    fun deleteItem(item: MemoryItem) {
        viewModelScope.launch {
            // Store the item for potential undo
            _recentlyDeletedItem.value = item

            // Delete from repository
            repository.deleteItem(item)

            // Show snackbar with undo action
            val message = application.getString(R.string.item_deleted_format, item.title)
            val actionLabel = application.getString(R.string.undo)

            val result = snackbarHostState.showSnackbar(
                message = message,
                actionLabel = actionLabel,
                duration = SnackbarDuration.Short
            )

            when (result) {
                SnackbarResult.ActionPerformed -> {
                    // User clicked Undo
                    undoDelete()
                }
                SnackbarResult.Dismissed -> {
                    // Clear the recently deleted item
                    _recentlyDeletedItem.value = null
                }
            }
        }
    }

    /**
     * Restores the recently deleted item.
     */
    private fun undoDelete() {
        viewModelScope.launch {
            _recentlyDeletedItem.value?.let { item ->
                repository.restoreItem(item)
                _recentlyDeletedItem.value = null
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun clearSearch() {
        _searchQuery.value = ""
    }
}
