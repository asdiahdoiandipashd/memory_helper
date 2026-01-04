package com.example.memoryhelper.ui.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.R
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppListItem
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.AppTextField
import com.example.memoryhelper.ui.designsystem.AppTopBar
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.designsystem.SecondaryButton
import com.example.memoryhelper.ui.theme.PrimaryBlue
import com.example.memoryhelper.ui.theme.SecondaryTeal
import com.example.memoryhelper.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToFlashcard: (List<MemoryItem>) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val notebooks by viewModel.notebooks.collectAsState()
    val selectedNotebookId by viewModel.selectedNotebookId.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var showAddNotebookDialog by remember { mutableStateOf(false) }
    var selectedItem by remember { mutableStateOf<MemoryItem?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    var notebookToManage by remember { mutableStateOf<com.example.memoryhelper.data.local.entity.Notebook?>(null) }
    var showOptionsDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            if (isSearchExpanded) {
                AppTopBar(
                    title = {
                        AppTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.updateSearchQuery(it) },
                            label = stringResource(R.string.search),
                            placeholder = stringResource(R.string.search_hint),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            viewModel.clearSearch()
                            isSearchExpanded = false
                        }) {
                            Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close))
                        }
                    }
                )
            }
        },
        snackbarHost = {
            SnackbarHost(hostState = viewModel.snackbarHostState)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 8.dp,
                    pressedElevation = 12.dp
                ),
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(R.string.add_item_desc),
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Custom Header (only visible when not searching)
            if (!isSearchExpanded) {
                ModernHeader(
                    pendingCount = uiState.overdueItems.size + uiState.todayItems.size,
                    progress = uiState.dailyProgress,
                    onSearchClick = { isSearchExpanded = true }
                )
            }

            // Notebook Filter Bar
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                item {
                    FilterChip(
                        selected = selectedNotebookId == null,
                        onClick = { viewModel.selectNotebook(null) },
                        label = { Text(stringResource(R.string.all_notebooks)) },
                        shape = CircleShape,
                        border = if (selectedNotebookId == null) null else FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false
                        )
                    )
                }
                items(notebooks) { notebook ->
                    FilterChip(
                        selected = selectedNotebookId == notebook.id,
                        onClick = { viewModel.selectNotebook(notebook.id) },
                        label = { Text(notebook.name) },
                        shape = CircleShape,
                        border = if (selectedNotebookId == notebook.id) null else FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = false
                        ),
                        modifier = Modifier.combinedClickable(
                            onClick = { viewModel.selectNotebook(notebook.id) },
                            onLongClick = {
                                notebookToManage = notebook
                                showOptionsDialog = true
                            }
                        )
                    )
                }
                item {
                    IconButton(onClick = { showAddNotebookDialog = true }) {
                        Icon(Icons.Rounded.Add, contentDescription = stringResource(R.string.add_notebook))
                    }
                }
            }

            if (uiState.isEmpty) {
                if (viewModel.isSearching) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = stringResource(R.string.no_search_results),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    EmptyStateView(modifier = Modifier.fillMaxSize())
                }
            } else if (viewModel.isSearching) {
                // Search mode: flat list
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                items(
                    items = uiState.allItems,
                    key = { "search_${it.id}" }
                ) { item ->
                    SwipeableItemCard(
                        item = item,
                        cardType = when (item.status) {
                            MemoryItemStatus.COMPLETED -> CardType.COMPLETED
                            else -> CardType.TODAY
                        },
                        onClick = { selectedItem = item },
                        onQuickRemember = { viewModel.markAsRemembered(item) },
                        onQuickForgot = { viewModel.markAsForgot(item) },
                        onDelete = { viewModel.deleteItem(item) }
                    )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(AppSpacing.md),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                // Start Review Button - Modern gradient style
                if (uiState.hasAnyDueItems) {
                    item {
                        val dueCount = uiState.overdueItems.size + uiState.todayItems.filter {
                            it.nextReviewTime <= System.currentTimeMillis()
                        }.size

                        AppCard(
                            modifier = Modifier.fillMaxWidth(),
                            tone = AppCardTone.Accent,
                            onClick = {
                                val dueItems = uiState.overdueItems + uiState.todayItems.filter {
                                    it.nextReviewTime <= System.currentTimeMillis()
                                }
                                onNavigateToFlashcard(dueItems)
                            },
                            padding = PaddingValues(AppSpacing.lg)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = stringResource(R.string.start_review),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                    Text(
                                        text = "$dueCount 项待复习",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.2f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier
                                            .padding(AppSpacing.sm)
                                            .size(24.dp)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                // Section 1: Overdue (Inherited) - Red Header - HIGHEST PRIORITY
                if (uiState.overdueItems.isNotEmpty()) {
                    item(key = "header_overdue") {
                        StickyHeader(
                            title = stringResource(R.string.section_overdue),
                            count = uiState.overdueItems.size,
                            icon = Icons.Rounded.Warning,
                            backgroundColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                    items(
                        items = uiState.overdueItems,
                        key = { "overdue_${it.id}" }
                    ) { item ->
                        SwipeableItemCard(
                            item = item,
                            cardType = CardType.OVERDUE,
                            onClick = { selectedItem = item },
                            onQuickRemember = { viewModel.markAsRemembered(item) },
                            onQuickForgot = { viewModel.markAsForgot(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Section 2: Due Today - Blue Header
                if (uiState.todayItems.isNotEmpty()) {
                    item(key = "header_today") {
                        StickyHeader(
                            title = stringResource(R.string.section_due_today),
                            count = uiState.todayItems.size,
                            icon = Icons.Rounded.DateRange,
                            backgroundColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    items(
                        items = uiState.todayItems,
                        key = { "today_${it.id}" }
                    ) { item ->
                        SwipeableItemCard(
                            item = item,
                            cardType = CardType.TODAY,
                            onClick = { selectedItem = item },
                            onQuickRemember = { viewModel.markAsRemembered(item) },
                            onQuickForgot = { viewModel.markAsForgot(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Section 3: Upcoming - Gray Header
                if (uiState.upcomingItems.isNotEmpty()) {
                    item(key = "header_upcoming") {
                        StickyHeader(
                            title = stringResource(R.string.section_upcoming),
                            count = uiState.upcomingItems.size,
                            icon = Icons.Rounded.DateRange,
                            backgroundColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(
                        items = uiState.upcomingItems,
                        key = { "upcoming_${it.id}" }
                    ) { item ->
                        SwipeableItemCard(
                            item = item,
                            cardType = CardType.UPCOMING,
                            onClick = { selectedItem = item },
                            onQuickRemember = { viewModel.markAsRemembered(item) },
                            onQuickForgot = { viewModel.markAsForgot(item) },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }

                // Section 4: Completed - Green Header
                if (uiState.completedItems.isNotEmpty()) {
                    item(key = "header_completed") {
                        StickyHeader(
                            title = stringResource(R.string.section_completed),
                            count = uiState.completedItems.size,
                            icon = Icons.Rounded.CheckCircle,
                            backgroundColor = Color(0xFFE8F5E9),
                            contentColor = Color(0xFF2E7D32)
                        )
                    }
                    items(
                        items = uiState.completedItems,
                        key = { "completed_${it.id}" }
                    ) { item ->
                        SwipeableItemCard(
                            item = item,
                            cardType = CardType.COMPLETED,
                            onClick = { selectedItem = item },
                            onQuickRemember = { },
                            onQuickForgot = { },
                            onDelete = { viewModel.deleteItem(item) }
                        )
                    }
                }
            }
        }
        }

        if (showAddDialog) {
            AddItemDialog(
                notebooks = notebooks,
                selectedNotebookId = selectedNotebookId,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, content, notebookId ->
                    viewModel.addItem(title, content, notebookId)
                    showAddDialog = false
                }
            )
        }

        if (showAddNotebookDialog) {
            AddNotebookDialog(
                onDismiss = { showAddNotebookDialog = false },
                onConfirm = { name ->
                    viewModel.createNotebook(name)
                    showAddNotebookDialog = false
                }
            )
        }

        notebookToManage?.let { notebook ->
            if (showOptionsDialog) {
                NotebookOptionsDialog(
                    notebook = notebook,
                    onDismiss = {
                        showOptionsDialog = false
                        notebookToManage = null
                    },
                    onRename = {
                        showOptionsDialog = false
                        showRenameDialog = true
                    },
                    onDelete = {
                        showOptionsDialog = false
                        showDeleteDialog = true
                    }
                )
            }

            if (showRenameDialog) {
                RenameNotebookDialog(
                    notebook = notebook,
                    onDismiss = {
                        showRenameDialog = false
                        notebookToManage = null
                    },
                    onConfirm = { newName ->
                        viewModel.renameNotebook(notebook, newName)
                        showRenameDialog = false
                        notebookToManage = null
                    }
                )
            }

            if (showDeleteDialog) {
                DeleteNotebookDialog(
                    notebook = notebook,
                    onDismiss = {
                        showDeleteDialog = false
                        notebookToManage = null
                    },
                    onConfirm = {
                        viewModel.deleteNotebook(notebook)
                        showDeleteDialog = false
                        notebookToManage = null
                    }
                )
            }
        }

        // Review Dialog
        selectedItem?.let { item ->
            ReviewDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onRemember = {
                    viewModel.markAsRemembered(item)
                    selectedItem = null
                },
                onForgot = {
                    viewModel.markAsForgot(item)
                    selectedItem = null
                },
                onDelete = {
                    viewModel.deleteItem(item)
                    selectedItem = null
                }
            )
        }
    }
}

/**
 * Daily Progress Card showing review completion status.
 */
@Composable
private fun DailyProgressCard(
    progress: DailyProgress,
    totalPending: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.progressPercentage,
        label = "progress"
    )

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = if (progress.isAllDone && totalPending == 0) AppCardTone.Accent else AppCardTone.Surface,
        padding = PaddingValues(AppSpacing.md)
    ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.todays_progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = if (totalPending > 0)
                            stringResource(R.string.items_to_review, totalPending)
                        else if (progress.isAllDone)
                            stringResource(R.string.all_done_today)
                        else
                            stringResource(R.string.no_items_scheduled),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Progress Circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            if (progress.isAllDone && totalPending == 0)
                                MaterialTheme.colorScheme.tertiary
                            else
                                MaterialTheme.colorScheme.primary
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${progress.completedToday}/${progress.totalDueToday}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppSpacing.sm))

            // Progress Bar
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (progress.isAllDone && totalPending == 0)
                    MaterialTheme.colorScheme.tertiary
                else
                    MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                strokeCap = StrokeCap.Round
            )
    }
}

/**
 * Sticky section header with icon and count - Modern Design.
 */
@Composable
private fun StickyHeader(
    title: String,
    count: Int,
    icon: ImageVector,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = backgroundColor.copy(alpha = 0.9f),
        shape = MaterialTheme.shapes.large,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            }
            Surface(
                shape = MaterialTheme.shapes.large,
                color = contentColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = stringResource(R.string.items_count, count),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = contentColor,
                    modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs)
                )
            }
        }
    }
}

/**
 * Swipeable item card wrapper with delete functionality.
 * Wraps ToDoItemCard with SwipeToDismissBox for swipe-to-delete.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SwipeableItemCard(
    item: MemoryItem,
    cardType: CardType,
    onClick: () -> Unit,
    onQuickRemember: () -> Unit,
    onQuickForgot: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                true
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            // Red background with delete icon when swiping
            val scale by animateFloatAsState(
                targetValue = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) 0.8f else 1.2f,
                label = "deleteIconScale"
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = MaterialTheme.colorScheme.error,
                        shape = MaterialTheme.shapes.large
                    )
                    .padding(horizontal = 20.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = stringResource(R.string.delete),
                    modifier = Modifier.scale(scale),
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        ToDoItemCard(
            item = item,
            cardType = cardType,
            onClick = onClick,
            onQuickRemember = onQuickRemember,
            onQuickForgot = onQuickForgot
        )
    }
}

/**
 * Card type enum for different item states.
 */
private enum class CardType {
    OVERDUE, TODAY, UPCOMING, COMPLETED
}

/**
 * To-Do style item card with quick action buttons - Modern Design.
 */
@Composable
private fun ToDoItemCard(
    item: MemoryItem,
    cardType: CardType,
    onClick: () -> Unit,
    onQuickRemember: () -> Unit,
    onQuickForgot: () -> Unit
) {
    val isCompleted = cardType == CardType.COMPLETED

    // 移除实时倒计时，改为静态显示 - 性能优化
    val currentTime = remember { System.currentTimeMillis() }
    val isDueNow = item.nextReviewTime <= currentTime && !isCompleted

    // Determine strip color based on status
    val stripColor = when (cardType) {
        CardType.OVERDUE -> com.example.memoryhelper.ui.theme.StripOverdue
        CardType.TODAY -> if (isDueNow) SecondaryTeal else com.example.memoryhelper.ui.theme.StripToday
        CardType.UPCOMING -> com.example.memoryhelper.ui.theme.StripUpcoming
        CardType.COMPLETED -> com.example.memoryhelper.ui.theme.StripCompleted
    }

    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = AppCardTone.Elevated,
        padding = PaddingValues(0.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Color Strip (Left indicator) - Vertical bar
            Box(
                modifier = Modifier
                    .width(5.dp)
                    .height(88.dp)
                    .background(
                        color = stripColor,
                        shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                    )
            )

            // Content Section
            Row(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Title and notebook tag
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    // Title - Bold
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(AppSpacing.xxs))

                    // Notebook tag + Stage badge row
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        // Stage badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = stripColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = stringResource(R.string.stage_format, item.stageIndex + 1),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = stripColor,
                                modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(AppSpacing.xxs))

                    // Time indicator - Right side display
                    ReviewTimeText(
                        targetTime = item.nextReviewTime,
                        isCompleted = isCompleted,
                        cardType = cardType
                    )
                }

                Spacer(modifier = Modifier.width(AppSpacing.sm))

                // Quick Action Buttons (only for non-completed items)
                if (!isCompleted) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                    ) {
                        // Forgot button (soft red)
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.errorContainer,
                            onClick = onQuickForgot
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Refresh,
                                    contentDescription = stringResource(R.string.forgot_reset),
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        // Remember button (soft green)
                        Surface(
                            modifier = Modifier.size(44.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.tertiaryContainer,
                            onClick = onQuickRemember
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = stringResource(R.string.remember_done),
                                    modifier = Modifier.size(22.dp),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
                    }
                } else {
                    // Completed checkmark
                    Surface(
                        modifier = Modifier.size(44.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.tertiaryContainer
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = stringResource(R.string.completed),
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Live countdown text component - OPTIMIZED VERSION.
 */
@Composable
private fun ReviewTimeText(
    targetTime: Long,
    isCompleted: Boolean,
    cardType: CardType,
    modifier: Modifier = Modifier
) {
    // 使用 remember 缓存当前时间，减少重组
    var currentTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // 只在需要时更新（非完成状态）
    if (!isCompleted) {
        LaunchedEffect(targetTime) {
            while (true) {
                delay(30000L) // 改为30秒更新一次，减少重组频率
                currentTime = System.currentTimeMillis()
            }
        }
    }

    val isDue = targetTime <= currentTime && !isCompleted
    val diff = targetTime - currentTime

    // Chinese time strings
    val timeCompleted = stringResource(R.string.time_completed)
    val timeOverdueMinutes = stringResource(R.string.time_overdue_minutes)
    val timeOverdueHours = stringResource(R.string.time_overdue_hours)
    val timeOverdueDays = stringResource(R.string.time_overdue_days)
    val timeReviewNow = stringResource(R.string.time_review_now)
    val timeDueSeconds = stringResource(R.string.time_due_seconds)
    val timeDueMinutes = stringResource(R.string.time_due_minutes)
    val timeDueHours = stringResource(R.string.time_due_hours)
    val timeDueDate = stringResource(R.string.time_due_date)

    val displayText = when {
        isCompleted -> timeCompleted
        cardType == CardType.OVERDUE -> {
            val overdueTime = currentTime - targetTime
            when {
                overdueTime < 60 * 60 * 1000 -> String.format(timeOverdueMinutes, overdueTime / (60 * 1000))
                overdueTime < 24 * 60 * 60 * 1000 -> String.format(timeOverdueHours, overdueTime / (60 * 60 * 1000))
                else -> {
                    val days = overdueTime / (24 * 60 * 60 * 1000)
                    String.format(timeOverdueDays, days)
                }
            }
        }
        isDue -> timeReviewNow
        diff < 60 * 1000 -> String.format(timeDueSeconds, diff / 1000)
        diff < 60 * 60 * 1000 -> String.format(timeDueMinutes, diff / (60 * 1000))
        diff < 24 * 60 * 60 * 1000 -> String.format(timeDueHours, diff / (60 * 60 * 1000))
        else -> {
            val dateFormat = SimpleDateFormat("M月d日 HH:mm", Locale.CHINESE)
            String.format(timeDueDate, dateFormat.format(Date(targetTime)))
        }
    }

    val textColor = when {
        isCompleted -> MaterialTheme.colorScheme.tertiary
        cardType == CardType.OVERDUE -> MaterialTheme.colorScheme.error
        isDue -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Text(
        text = displayText,
        style = MaterialTheme.typography.bodySmall,
        fontWeight = if (isDue || cardType == CardType.OVERDUE) FontWeight.Bold else FontWeight.Normal,
        color = textColor,
        modifier = modifier
    )
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Rounded.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(AppSpacing.lg))
            Text(
                text = stringResource(R.string.no_items_yet),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(AppSpacing.xs))
            Text(
                text = stringResource(R.string.tap_to_add_first_item),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ReviewDialog(
    item: MemoryItem,
    onDismiss: () -> Unit,
    onRemember: () -> Unit,
    onForgot: () -> Unit,
    onDelete: () -> Unit
) {
    val isCompleted = item.status == MemoryItemStatus.COMPLETED

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                // Delete icon in the top-right corner
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(R.string.delete_item),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Stage info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.stage_format, item.stageIndex + 1),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (!isCompleted) {
                        ReviewTimeText(
                            targetTime = item.nextReviewTime,
                            isCompleted = false,
                            cardType = CardType.TODAY
                        )
                    }
                }
                Spacer(modifier = Modifier.height(AppSpacing.sm))

                // Content
                val noContentText = stringResource(R.string.no_content)
                Text(
                    text = item.content.ifBlank { noContentText },
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(AppSpacing.lg))

                // Status info
                if (isCompleted) {
                    AppCard(
                        tone = AppCardTone.Accent,
                        padding = PaddingValues(AppSpacing.sm)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(modifier = Modifier.width(AppSpacing.xs))
                            Text(
                                text = stringResource(R.string.item_completed_message),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (!isCompleted) {
                Row {
                    // Forgot button (Red)
                    Button(
                        onClick = onForgot,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.forgot))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Remember button (Green)
                    Button(
                        onClick = onRemember,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.remember))
                    }
                }
            } else {
                SecondaryButton(
                    text = stringResource(R.string.close),
                    onClick = onDismiss
                )
            }
        },
        dismissButton = {
            if (!isCompleted) {
                SecondaryButton(
                    text = stringResource(R.string.cancel),
                    onClick = onDismiss
                )
            }
        }
    )
}

@Composable
private fun AddItemDialog(
    notebooks: List<com.example.memoryhelper.data.local.entity.Notebook>,
    selectedNotebookId: Long?,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String, notebookId: Long) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedId by remember { mutableStateOf(selectedNotebookId ?: notebooks.firstOrNull()?.id ?: 1L) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_new_item)) },
        text = {
            Column {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = stringResource(R.string.title),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                AppTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = stringResource(R.string.content),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(AppSpacing.sm))
                Text(
                    text = stringResource(R.string.select_notebook),
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
                ) {
                    items(notebooks) { notebook ->
                        FilterChip(
                            selected = selectedId == notebook.id,
                            onClick = { selectedId = notebook.id },
                            label = { Text(notebook.name) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.add),
                onClick = { onConfirm(title, content, selectedId) },
                enabled = title.isNotBlank()
            )
        },
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun AddNotebookDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String) -> Unit
) {
    var name by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.add_notebook)) },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.notebook_name),
                placeholder = stringResource(R.string.notebook_name_hint),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.add),
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun NotebookOptionsDialog(
    notebook: com.example.memoryhelper.data.local.entity.Notebook,
    onDismiss: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.manage_notebook),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                Text(
                    text = notebook.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = AppSpacing.xs)
                )

                // Rename option
                AppListItem(
                    title = stringResource(R.string.rename),
                    leadingIcon = Icons.Rounded.Edit,
                    onClick = onRename
                )

                // Delete option
                AppListItem(
                    title = stringResource(R.string.delete_action),
                    leadingIcon = Icons.Rounded.Delete,
                    onClick = onDelete
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun RenameNotebookDialog(
    notebook: com.example.memoryhelper.data.local.entity.Notebook,
    onDismiss: () -> Unit,
    onConfirm: (newName: String) -> Unit
) {
    var name by remember { mutableStateOf(notebook.name) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.rename_notebook)) },
        text = {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = stringResource(R.string.notebook_name),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.save),
                onClick = { onConfirm(name) },
                enabled = name.isNotBlank()
            )
        },
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun DeleteNotebookDialog(
    notebook: com.example.memoryhelper.data.local.entity.Notebook,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.delete_notebook_title)) },
        text = {
            Text(stringResource(R.string.delete_notebook_warning, notebook.name))
        },
        confirmButton = {
            PrimaryButton(
                text = stringResource(R.string.confirm_delete),
                onClick = onConfirm,
                containerColor = MaterialTheme.colorScheme.error
            )
        },
        dismissButton = {
            SecondaryButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss
            )
        }
    )
}

/**
 * Modern header with greeting, task count, and circular progress.
 */
@Composable
private fun ModernHeader(
    pendingCount: Int,
    progress: DailyProgress,
    onSearchClick: () -> Unit
) {
    // Time-based greeting
    val hour = remember { java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY) }
    val greetingResource = when (hour) {
        in 0..11 -> R.string.greeting_morning
        in 12..17 -> R.string.greeting_afternoon
        else -> R.string.greeting_evening
    }

    // Gradient background for header
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Greeting and task count
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(greetingResource),
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = if (pendingCount > 0)
                        stringResource(R.string.tasks_pending, pendingCount)
                    else
                        stringResource(R.string.all_caught_up),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Right: Circular Progress + Search
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
            ) {
                // Circular Progress Ring
                CircularProgressRing(
                    progress = progress.progressPercentage,
                    completed = progress.completedToday,
                    total = progress.totalDueToday
                )

                // Search Icon Button
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shadowElevation = 2.dp,
                    onClick = onSearchClick
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Search,
                            contentDescription = stringResource(R.string.search),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Circular progress ring indicator with modern design.
 */
@Composable
private fun CircularProgressRing(
    progress: Float,
    completed: Int,
    total: Int
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "circularProgress"
    )

    val isComplete = completed >= total && total > 0
    val progressColor = if (isComplete) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.primary
    }

    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val ringBackground = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .size(72.dp)
            .shadow(
                elevation = 6.dp,
                shape = CircleShape,
                clip = false
            )
            .clip(CircleShape)
            .background(ringBackground),
        contentAlignment = Alignment.Center
    ) {
        // Background circle track
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(64.dp)
        ) {
            val strokeWidth = 8.dp.toPx()
            drawCircle(
                color = trackColor,
                radius = (size.minDimension / 2) - strokeWidth / 2,
                style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
            )
        }

        // Progress arc
        androidx.compose.foundation.Canvas(
            modifier = Modifier.size(64.dp)
        ) {
            val strokeWidth = 8.dp.toPx()
            val sweepAngle = 360 * animatedProgress
            drawArc(
                color = progressColor,
                startAngle = -90f,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = androidx.compose.ui.graphics.drawscope.Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Round
                )
            )
        }

        // Center text
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$completed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = progressColor
            )
            Text(
                text = "/$total",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
