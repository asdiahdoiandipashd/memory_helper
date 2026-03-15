package com.example.memoryhelper.ui.screens.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.memoryhelper.data.backup.BackupFormat
import com.example.memoryhelper.ui.designsystem.AppCard
import com.example.memoryhelper.ui.designsystem.AppCardTone
import com.example.memoryhelper.ui.designsystem.AppSpacing
import com.example.memoryhelper.ui.designsystem.PrimaryButton
import com.example.memoryhelper.ui.designsystem.SecondaryButton
import kotlinx.coroutines.launch

@Composable
fun BackupRestoreSection(
    viewModel: SettingsViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsState()

    val createFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    viewModel.exportToJson(outputStream)
                }
            }
        }
    }

    val createCsvFileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                    viewModel.exportToCsv(outputStream)
                }
            }
        }
    }

    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    viewModel.importFromJson(inputStream)
                }
            }
        }
    }

    val openCsvDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    viewModel.importFromCsv(inputStream)
                }
            }
        }
    }

    val openAnkiDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            scope.launch {
                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    viewModel.importFromAnki(inputStream)
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
            Text(
                text = "Backups and imports",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Preserve the full workspace, move data between devices, or seed the system with cards from external tools.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            tone = AppCardTone.Accent
        ) {
            CardHeader(
                icon = Icons.Default.Backup,
                title = "Export workspace",
                supporting = "JSON keeps the full app state. CSV is lighter and better for spreadsheet analysis."
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
            ) {
                PrimaryButton(
                    text = "Export JSON",
                    onClick = {
                        val fileName = viewModel.generateBackupFileName(BackupFormat.JSON)
                        createFileLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.backupInProgress
                )
                SecondaryButton(
                    text = "Export CSV",
                    onClick = {
                        val fileName = viewModel.generateBackupFileName(BackupFormat.CSV)
                        createCsvFileLauncher.launch(fileName)
                    },
                    modifier = Modifier.weight(1f),
                    enabled = !uiState.backupInProgress
                )
            }
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            tone = AppCardTone.Surface
        ) {
            CardHeader(
                icon = Icons.Default.FileUpload,
                title = "Restore a full backup",
                supporting = "Use this only with a JSON export from Memory Helper. It rewrites the local workspace with the backup payload."
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            CalloutRow(
                icon = Icons.Default.Warning,
                text = "Restore is destructive for local data. Export a fresh backup first if you are unsure.",
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            PrimaryButton(
                text = "Import JSON backup",
                onClick = { openDocumentLauncher.launch(arrayOf("application/json")) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.restoreInProgress
            )
        }

        AppCard(
            modifier = Modifier.fillMaxWidth(),
            tone = AppCardTone.Elevated
        ) {
            CardHeader(
                icon = Icons.Default.FileUpload,
                title = "Bring cards in",
                supporting = "Use CSV for structured imports and .apkg for basic Anki front-back cards, tags, deck names, and linked media when available."
            )
            Spacer(modifier = Modifier.height(AppSpacing.md))
            ImportSourceRow(
                title = "CSV cards",
                supporting = "Columns: title, content, notebook, tags, stage, next_review_time.",
                buttonText = "Import CSV",
                onClick = { openCsvDocumentLauncher.launch(arrayOf("text/*", "text/csv")) },
                enabled = !uiState.restoreInProgress
            )
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(AppSpacing.sm))
            ImportSourceRow(
                title = "Anki package",
                supporting = "Imports basic front-back notes from .apkg. Unsupported models are skipped instead of breaking the batch.",
                buttonText = "Import .apkg",
                onClick = { openAnkiDocumentLauncher.launch(arrayOf("*/*")) },
                enabled = !uiState.restoreInProgress
            )
        }

        if (uiState.backupInProgress || uiState.restoreInProgress) {
            AppCard(
                modifier = Modifier.fillMaxWidth(),
                tone = AppCardTone.Surface
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs)) {
                        Text(
                            text = if (uiState.backupInProgress) "Export in progress" else "Import in progress",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Large workspaces can take a moment. The operation continues until the stream finishes.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        uiState.backupResult?.let { result ->
            OperationResultCard(
                title = if (result.success) "Export completed" else "Export failed",
                message = result.message,
                detail = result.fileName?.let { fileName -> "Saved as $fileName" },
                success = result.success,
                onClear = { viewModel.clearBackupResult() }
            )
        }

        uiState.restoreResult?.let { result ->
            OperationResultCard(
                title = if (result.success) "Import completed" else "Import failed",
                message = result.message,
                detail = result.importResult?.let { importResult ->
                    buildString {
                        append("Items ${importResult.itemsImported} imported, ${importResult.itemsSkipped} skipped. ")
                        append("Notebooks ${importResult.notebooksImported}, curves ${importResult.curvesImported}, logs ${importResult.logsImported}.")
                    }
                },
                success = result.success,
                onClear = { viewModel.clearRestoreResult() }
            )
        }
    }
}

@Composable
private fun CardHeader(
    icon: ImageVector,
    title: String,
    supporting: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
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
private fun ImportSourceRow(
    title: String,
    supporting: String,
    buttonText: String,
    onClick: () -> Unit,
    enabled: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(AppSpacing.xxs))
            Text(
                text = supporting,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        SecondaryButton(
            text = buttonText,
            onClick = onClick,
            enabled = enabled
        )
    }
}

@Composable
private fun CalloutRow(
    icon: ImageVector,
    text: String,
    containerColor: androidx.compose.ui.graphics.Color,
    contentColor: androidx.compose.ui.graphics.Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = containerColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = contentColor
            )
        }
    }
}

@Composable
private fun OperationResultCard(
    title: String,
    message: String,
    detail: String?,
    success: Boolean,
    onClear: () -> Unit
) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        tone = if (success) AppCardTone.Accent else AppCardTone.Surface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)
        ) {
            Icon(
                imageVector = if (success) Icons.Default.CheckCircle else Icons.Default.Error,
                contentDescription = null,
                tint = if (success) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(AppSpacing.xxs))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                detail?.let {
                    Spacer(modifier = Modifier.height(AppSpacing.xxs))
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(AppSpacing.md))
        SecondaryButton(
            text = "Dismiss",
            onClick = onClear,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
