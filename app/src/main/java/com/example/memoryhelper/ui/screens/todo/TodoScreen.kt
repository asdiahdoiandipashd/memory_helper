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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.memoryhelper.R
import com.example.memoryhelper.data.local.entity.TodoTag

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
            TopAppBar(
                title = { Text(stringResource(R.string.todo_title)) }
            )
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
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.todo_add_task)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            TagFilterRow(
                tags = uiState.tags,
                selectedTagId = uiState.selectedTagId,
                onSelectTag = viewModel::selectTag,
                onManageTags = { showTagDialog = true }
            )

            if (uiState.isEmpty) {
                EmptyState()
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.dailyTasks.isNotEmpty()) {
                        item {
                            SectionHeader(text = stringResource(R.string.todo_section_daily))
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
                            SectionHeader(text = stringResource(R.string.todo_section_active))
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
                            SectionHeader(text = stringResource(R.string.todo_section_completed))
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
            title = { Text(stringResource(R.string.todo_delete_task_title)) },
            text = { Text(stringResource(R.string.todo_delete_task_message)) },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteTask(item.task)
                    deleteTaskTarget = null
                }) {
                    Text(stringResource(R.string.delete_action))
                }
            },
            dismissButton = {
                Button(onClick = { deleteTaskTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LazyRow(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                FilterChip(
                    selected = selectedTagId == null,
                    onClick = { onSelectTag(null) },
                    label = { Text(stringResource(R.string.todo_all_tags)) }
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
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.todo_manage_tags)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
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

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isCompleted,
                onCheckedChange = { onToggle() }
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
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
                        Spacer(modifier = Modifier.width(8.dp))
                        AssistChip(
                            onClick = {},
                            label = { Text(stringResource(R.string.todo_daily_label)) }
                        )
                    }
                }
                if (item.task.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.task.note,
                        style = noteStyle,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (item.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                horizontalAlignment = Alignment.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.todo_edit_task)
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete_action)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.todo_empty_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.todo_empty_body),
                style = MaterialTheme.typography.bodyMedium
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
                text = stringResource(if (item == null) R.string.todo_add_task else R.string.todo_edit_task)
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.todo_task_title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.todo_task_note_hint)) },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.todo_is_daily),
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
                        text = stringResource(R.string.todo_tags_title),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
            Button(
                onClick = { onSave(title.trim(), note.trim(), isDaily, selectedTagIds.toList()) },
                enabled = title.trim().isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
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
        title = { Text(stringResource(R.string.todo_manage_tags)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = newTagName,
                    onValueChange = { newTagName = it },
                    label = { Text(stringResource(R.string.todo_tag_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        val trimmed = newTagName.trim()
                        if (trimmed.isNotEmpty()) {
                            onAddTag(trimmed)
                            newTagName = ""
                        }
                    },
                    enabled = newTagName.trim().isNotBlank()
                ) {
                    Text(stringResource(R.string.todo_add_tag))
                }
                if (tags.isEmpty()) {
                    Text(
                        text = stringResource(R.string.todo_no_tags),
                        style = MaterialTheme.typography.bodyMedium
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = stringResource(R.string.rename)
                                    )
                                }
                                IconButton(onClick = { deleteTarget = tag }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = stringResource(R.string.delete_action)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )

    renameTarget?.let { tag ->
        AlertDialog(
            onDismissRequest = { renameTarget = null },
            title = { Text(stringResource(R.string.todo_rename_tag)) },
            text = {
                OutlinedTextField(
                    value = renameValue,
                    onValueChange = { renameValue = it },
                    label = { Text(stringResource(R.string.todo_tag_name_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = renameValue.trim()
                        if (trimmed.isNotEmpty()) {
                            onRenameTag(tag, trimmed)
                            renameTarget = null
                        }
                    },
                    enabled = renameValue.trim().isNotBlank()
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                Button(onClick = { renameTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    deleteTarget?.let { tag ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            title = { Text(stringResource(R.string.todo_delete_tag_title)) },
            text = { Text(stringResource(R.string.todo_delete_tag_message)) },
            confirmButton = {
                Button(onClick = {
                    onDeleteTag(tag)
                    deleteTarget = null
                }) {
                    Text(stringResource(R.string.delete_action))
                }
            },
            dismissButton = {
                Button(onClick = { deleteTarget = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}
