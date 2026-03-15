package com.example.memoryhelper.ui.screens.todo

import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Label
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.data.local.entity.TodoTag
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.AppTextField
import com.example.memoryhelper.ui.designsystem.AppTopBar
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.designsystem.SecondaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showTaskDialog by remember { mutableStateOf(false) }
    var editingTask by remember { mutableStateOf<TodoDisplayItem?>(null) }
    var showTagDialog by remember { mutableStateOf(false) }
    var deleteTaskTarget by remember { mutableStateOf<TodoDisplayItem?>(null) }

    Scaffold(
        topBar = {
            AppTopBar(title = { Text("\u5f85\u529e") })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingTask = null
                    showTaskDialog = true
                },
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = "\u6dfb\u52a0\u4efb\u52a1"
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AppSpacing.md)
        ) {
            TaskOverviewCard(
                dailyCount = uiState.dailyTasks.count { !it.isCompleted },
                activeCount = uiState.activeTasks.size,
                completedCount = uiState.completedTasks.size,
                selectedTagName = uiState.tags.firstOrNull { it.id == uiState.selectedTagId }?.name
            )

            TagFilterRow(
                tags = uiState.tags,
                selectedTagId = uiState.selectedTagId,
                onSelectTag = viewModel::selectTag,
                onManageTags = { showTagDialog = true }
            )

            if (uiState.isEmpty) {
                EmptyState(
                    onAdd = {
                        editingTask = null
                        showTaskDialog = true
                    }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = AppSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    if (uiState.dailyTasks.isNotEmpty()) {
                        item {
                            SectionHeader(text = "\u6bcf\u65e5\u4efb\u52a1", count = uiState.dailyTasks.size)
                        }
                        items(uiState.dailyTasks) { item ->
                            TodoTaskCard(
                                item = item,
                                onToggle = { viewModel.toggleTaskCompletion(item) },
                                onEdit = {
                                    editingTask = item
                                    showTaskDialog = true
                                },
                                onDelete = { deleteTaskTarget = item }
                            )
                        }
                    }

                    if (uiState.activeTasks.isNotEmpty()) {
                        item {
                            SectionHeader(text = "\u8fdb\u884c\u4e2d", count = uiState.activeTasks.size)
                        }
                        items(uiState.activeTasks) { item ->
                            TodoTaskCard(
                                item = item,
                                onToggle = { viewModel.toggleTaskCompletion(item) },
                                onEdit = {
                                    editingTask = item
                                    showTaskDialog = true
                                },
                                onDelete = { deleteTaskTarget = item }
                            )
                        }
                    }

                    if (uiState.completedTasks.isNotEmpty()) {
                        item {
                            SectionHeader(text = "\u5df2\u5b8c\u6210", count = uiState.completedTasks.size)
                        }
                        items(uiState.completedTasks) { item ->
                            TodoTaskCard(
                                item = item,
                                onToggle = { viewModel.toggleTaskCompletion(item) },
                                onEdit = {
                                    editingTask = item
                                    showTaskDialog = true
                                },
                                onDelete = { deleteTaskTarget = item }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showTaskDialog) {
        TaskDialog(
            item = editingTask,
            tags = uiState.tags,
            onDismiss = { showTaskDialog = false },
            onSave = { title, note, isDaily, tagIds ->
                if (editingTask == null) {
                    viewModel.addTask(title, note, isDaily, tagIds)
                } else {
                    viewModel.updateTask(editingTask!!.task, title, note, isDaily, tagIds)
                }
                showTaskDialog = false
            }
        )
    }

    if (showTagDialog) {
        TagManagerDialog(
            tags = uiState.tags,
            onAddTag = viewModel::addTag,
            onRenameTag = viewModel::renameTag,
            onDeleteTag = viewModel::deleteTag,
            onDismiss = { showTagDialog = false }
        )
    }

    deleteTaskTarget?.let { item ->
        AlertDialog(
            onDismissRequest = { deleteTaskTarget = null },
            title = { Text("\u5220\u9664\u4efb\u52a1\uff1f") },
            text = { Text("\u6b64\u64cd\u4f5c\u65e0\u6cd5\u64a4\u9500") },
            confirmButton = {
                PrimaryButton(
                    text = "\u5220\u9664",
                    onClick = {
                        viewModel.deleteTask(item.task)
                        deleteTaskTarget = null
                    }
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = "\u53d6\u6d88",
                    onClick = { deleteTaskTarget = null }
                )
            }
        )
    }
}

@Composable
private fun TagFilterRow(
    tags: List<TodoTag>,
    selectedTagId: Long?,
    onSelectTag: (Long?) -> Unit,
    onManageTags: () -> Unit
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.md),
        tone = AppCardTone.Surface,
        padding = PaddingValues(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            LazyRow(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
            ) {
                item {
                    FilterChip(
                        selected = selectedTagId == null,
                        onClick = { onSelectTag(null) },
                        label = { Text("\u5168\u90e8") }
                    )
                }
                items(tags) { tag ->
                    FilterChip(
                        selected = selectedTagId == tag.id,
                        onClick = { onSelectTag(tag.id) },
                        label = { Text(tag.name) }
                    )
                }
            }
            IconButton(onClick = onManageTags) {
                Icon(
                    imageVector = Icons.Rounded.Edit,
                    contentDescription = "\u7ba1\u7406\u6807\u7b7e"
                )
            }
        }
    }
}

@Composable
private fun TaskOverviewCard(
    dailyCount: Int,
    activeCount: Int,
    completedCount: Int,
    selectedTagName: String?
) {
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.md),
        tone = AppCardTone.Accent
    ) {
        Text(
            text = "\u4efb\u52a1\u9762\u677f",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = if (selectedTagName == null) "\u5148\u6536\u53e3\u4eca\u5929\u6700\u91cd\u8981\u7684\u4e8b\u60c5" else "\u5f53\u524d\u6807\u7b7e\uff1a$selectedTagName",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(AppSpacing.xs))
        Text(
            text = "\u6bcf\u65e5\u4efb\u52a1\u6bcf\u5929\u5237\u65b0\uff0c\u666e\u901a\u4efb\u52a1\u6309\u5b8c\u6210\u72b6\u6001\u6c89\u6dc0\u3002\u8fd9\u4e2a\u9875\u9762\u73b0\u5728\u6309\u4efb\u52a1\u7ba1\u7406\u65b9\u5f0f\u7ec4\u7ec7\uff0c\u800c\u4e0d\u662f\u5b66\u4e60\u9644\u5c5e\u5217\u8868\u3002",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.88f)
        )
        Spacer(modifier = Modifier.height(AppSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            TodoMetricChip(
                modifier = Modifier.weight(1f),
                label = "\u4eca\u65e5",
                value = dailyCount.toString()
            )
            TodoMetricChip(
                modifier = Modifier.weight(1f),
                label = "\u8fdb\u884c\u4e2d",
                value = activeCount.toString()
            )
            TodoMetricChip(
                modifier = Modifier.weight(1f),
                label = "\u5df2\u5b8c\u6210",
                value = completedCount.toString()
            )
        }
    }
}

@Composable
private fun TodoMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.58f)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = AppSpacing.xs, bottom = AppSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = "$count",
                modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xxs),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TodoTaskCard(
    item: TodoDisplayItem,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val textDecoration = if (item.isCompleted) TextDecoration.LineThrough else TextDecoration.None
    val titleStyle = MaterialTheme.typography.titleMedium.merge(
        TextStyle(textDecoration = textDecoration)
    )
    val noteStyle = MaterialTheme.typography.bodyMedium.merge(
        TextStyle(textDecoration = textDecoration)
    )

    val cardTone = if (item.isCompleted) AppCardTone.Surface else AppCardTone.Elevated
    AppCard(
        tone = cardTone,
        padding = PaddingValues(AppSpacing.sm)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = AppSpacing.xs)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.task.title,
                        style = titleStyle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.task.isDaily) {
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        AssistChip(
                            onClick = {},
                            label = { Text("\u6bcf\u65e5") }
                        )
                    }
                    if (item.isCompleted) {
                        Spacer(modifier = Modifier.width(AppSpacing.xs))
                        AssistChip(
                            onClick = {},
                            label = { Text("\u5df2\u5b8c\u6210") }
                        )
                    }
                }
                if (item.task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = item.task.note,
                        style = noteStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(AppSpacing.xs))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
                    ) {
                        item.tags.forEach { tag ->
                            AssistChip(
                                onClick = {},
                                label = { Text(tag.name) }
                            )
                        }
                    }
                }
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = "\u7f16\u8f91\u4efb\u52a1"
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = "\u5220\u9664\u4efb\u52a1"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = AppSpacing.xl),
        contentAlignment = Alignment.TopCenter
    ) {
        AppCard(
            modifier = Modifier.fillMaxWidth(),
            tone = AppCardTone.Surface
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Label,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Text(
                    text = "\u6682\u65f6\u6ca1\u6709\u4efb\u52a1",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(AppSpacing.xs))
                Text(
                    text = "\u5148\u5efa\u7acb\u4e00\u4e2a\u4efb\u52a1\u6c60\u3002\u6bcf\u65e5\u4efb\u52a1\u9002\u5408\u91cd\u590d\u6027\u4e8b\u9879\uff0c\u666e\u901a\u4efb\u52a1\u9002\u5408\u4e00\u6b21\u6027\u6267\u884c\u548c\u5173\u95ed\u3002",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(AppSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    TodoHintChip(
                        modifier = Modifier.weight(1f),
                        label = "\u6bcf\u65e5",
                        supporting = "\u4f8b\u884c\u68c0\u67e5"
                    )
                    TodoHintChip(
                        modifier = Modifier.weight(1f),
                        label = "\u666e\u901a",
                        supporting = "\u4e00\u6b21\u6027\u4ea4\u4ed8"
                    )
                }
                Spacer(modifier = Modifier.height(AppSpacing.md))
                PrimaryButton(
                    text = "\u6dfb\u52a0\u4efb\u52a1",
                    onClick = onAdd,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun TodoHintChip(
    label: String,
    supporting: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TaskDialog(
    item: TodoDisplayItem?,
    tags: List<TodoTag>,
    onDismiss: () -> Unit,
    onSave: (String, String, Boolean, List<Long>) -> Unit
) {
    var title by remember(item) { mutableStateOf(item?.task?.title ?: "") }
    var note by remember(item) { mutableStateOf(item?.task?.note ?: "") }
    var isDaily by remember(item) { mutableStateOf(item?.task?.isDaily ?: false) }
    var selectedTagIds by remember(item, tags) {
        mutableStateOf(item?.tags?.map { it.id }?.toSet() ?: emptySet())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (item == null) "\u6dfb\u52a0\u4efb\u52a1" else "\u7f16\u8f91\u4efb\u52a1"
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = "\u4efb\u52a1\u6807\u9898",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                AppTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = "\u5907\u6ce8\uff08\u53ef\u9009\uff09",
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "\u6bcf\u65e5\u4efb\u52a1",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = isDaily,
                        onCheckedChange = { isDaily = it }
                    )
                }
                if (tags.isNotEmpty()) {
                    Text(
                        text = "\u6807\u7b7e",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                        items(tags) { tag ->
                            val selected = selectedTagIds.contains(tag.id)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedTagIds = if (selected) {
                                        selectedTagIds - tag.id
                                    } else {
                                        selectedTagIds + tag.id
                                    }
                                },
                                label = { Text(tag.name) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "\u4fdd\u5b58",
                onClick = { onSave(title.trim(), note.trim(), isDaily, selectedTagIds.toList()) },
                enabled = title.trim().isNotBlank()
            )
        },
        dismissButton = {
            SecondaryButton(
                text = "\u53d6\u6d88",
                onClick = onDismiss
            )
        }
    )
}

@Composable
private fun TagManagerDialog(
    tags: List<TodoTag>,
    onAddTag: (String) -> Unit,
    onRenameTag: (TodoTag, String) -> Unit,
    onDeleteTag: (TodoTag) -> Unit,
    onDismiss: () -> Unit
) {
    var newTagName by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<TodoTag?>(null) }
    var renameValue by remember(renameTarget) { mutableStateOf(renameTarget?.name ?: "") }
    var deleteTarget by remember { mutableStateOf<TodoTag?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("\u7ba1\u7406\u6807\u7b7e") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                AppTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = "\u6807\u7b7e\u540d\u79f0",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                PrimaryButton(
                    text = "\u6dfb\u52a0\u6807\u7b7e",
                    onClick = {
                        val trimmed = newTagName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddTag(trimmed)
                            newTagName = ""
                        }
                    },
                    enabled = newTagName.trim().isNotBlank()
                )
                if (tags.isEmpty()) {
                    Text(
                        text = "\u6682\u65f6\u6ca1\u6709\u6807\u7b7e",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        tags.forEach { tag ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tag.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = {
                                    renameTarget = tag
                                    renameValue = tag.name
                                }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Edit,
                                        contentDescription = "\u91cd\u547d\u540d\u6807\u7b7e"
                                    )
                                }
                                IconButton(onClick = { deleteTarget = tag }) {
                                    Icon(
                                        imageVector = Icons.Rounded.Delete,
                                        contentDescription = "\u5220\u9664\u6807\u7b7e"
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            PrimaryButton(
                text = "\u5b8c\u6210",
                onClick = onDismiss
            )
        }
    )

    renameTarget?.let { tag ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text("\u91cd\u547d\u540d\u6807\u7b7e") },
            text = {
                AppTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = "\u6807\u7b7e\u540d\u79f0",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                PrimaryButton(
                    text = "\u4fdd\u5b58",
                    onClick = {
                        val trimmed = renameValue.trim()
                        if (trimmed.isNotEmpty()) {
                            onRenameTag(tag, trimmed)
                            renameTarget = null
                        }
                    },
                    enabled = renameValue.trim().isNotBlank()
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = "\u53d6\u6d88",
                    onClick = { renameTarget = null }
                )
            }
        )
    }

    deleteTarget?.let { tag ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text("\u5220\u9664\u6807\u7b7e\uff1f") },
            text = { Text("\u5220\u9664\u540e\u4f1a\u4ece\u4efb\u52a1\u4e2d\u79fb\u9664\u8be5\u6807\u7b7e") },
            confirmButton = {
                PrimaryButton(
                    text = "\u5220\u9664",
                    onClick = {
                        onDeleteTag(tag)
                        deleteTarget = null
                    }
                )
            },
            dismissButton = {
                SecondaryButton(
                    text = "\u53d6\u6d88",
                    onClick = { deleteTarget = null }
                )
            }
        )
    }
}
