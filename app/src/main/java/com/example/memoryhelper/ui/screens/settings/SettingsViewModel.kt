package com.example.memoryhelper.ui.screens.settings

import android.Manifest
import android.app.AlarmManager
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.memoryhelper.data.backup.BackupFormat
import com.example.memoryhelper.data.backup.BackupService
import com.example.memoryhelper.data.backup.ImportResult
import com.example.memoryhelper.domain.importing.ImportReport
import com.example.memoryhelper.domain.importing.ImportService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

data class PermissionStatus(
    val isGranted: Boolean,
    val isRequired: Boolean = true
)

data class BackupResult(
    val success: Boolean,
    val message: String,
    val fileName: String? = null,
    val format: BackupFormat? = null
)

data class RestoreResult(
    val success: Boolean,
    val message: String,
    val importResult: ImportResult? = null
)

data class SettingsUiState(
    val notificationPermission: PermissionStatus = PermissionStatus(false, false),
    val exactAlarmPermission: PermissionStatus = PermissionStatus(false, false),
    val batteryOptimizationWhitelisted: PermissionStatus = PermissionStatus(false, true),
    val isTestNotificationSent: Boolean = false,
    val backupInProgress: Boolean = false,
    val restoreInProgress: Boolean = false,
    val backupResult: BackupResult? = null,
    val restoreResult: RestoreResult? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val importService: ImportService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshAllPermissions()
    }

    fun refreshAllPermissions() {
        _uiState.update { currentState ->
            currentState.copy(
                notificationPermission = checkNotificationPermission(),
                exactAlarmPermission = checkExactAlarmPermission(),
                batteryOptimizationWhitelisted = checkBatteryOptimization()
            )
        }
    }

    private fun checkNotificationPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            PermissionStatus(isGranted = isGranted, isRequired = true)
        } else {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            PermissionStatus(isGranted = notificationManager.areNotificationsEnabled(), isRequired = true)
        }
    }

    private fun checkExactAlarmPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val isGranted = alarmManager?.canScheduleExactAlarms() == true
            PermissionStatus(isGranted = isGranted, isRequired = true)
        } else {
            PermissionStatus(isGranted = true, isRequired = false)
        }
    }

    private fun checkBatteryOptimization(): PermissionStatus {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        return PermissionStatus(isGranted = isIgnoring, isRequired = true)
    }

    fun onTestNotificationSent() {
        _uiState.update { it.copy(isTestNotificationSent = true) }
    }

    fun resetTestNotificationFlag() {
        _uiState.update { it.copy(isTestNotificationSent = false) }
    }

    suspend fun exportToJson(outputStream: OutputStream) {
        _uiState.update { it.copy(backupInProgress = true, backupResult = null) }

        val result = backupService.exportToJson(outputStream)

        _uiState.update { currentState ->
            val fileName = backupService.generateBackupFileName(BackupFormat.JSON)
            currentState.copy(
                backupInProgress = false,
                backupResult = if (result.isSuccess) {
                    BackupResult(
                        success = true,
                        message = "JSON backup completed successfully.",
                        fileName = fileName,
                        format = BackupFormat.JSON
                    )
                } else {
                    BackupResult(
                        success = false,
                        message = "Backup failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    )
                }
            )
        }
    }

    suspend fun exportToCsv(outputStream: OutputStream) {
        _uiState.update { it.copy(backupInProgress = true, backupResult = null) }

        val result = backupService.exportToCsv(outputStream)

        _uiState.update { currentState ->
            val fileName = backupService.generateBackupFileName(BackupFormat.CSV)
            currentState.copy(
                backupInProgress = false,
                backupResult = if (result.isSuccess) {
                    BackupResult(
                        success = true,
                        message = "CSV export completed successfully.",
                        fileName = fileName,
                        format = BackupFormat.CSV
                    )
                } else {
                    BackupResult(
                        success = false,
                        message = "CSV export failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    )
                }
            )
        }
    }

    suspend fun importFromJson(inputStream: InputStream) {
        _uiState.update { it.copy(restoreInProgress = true, restoreResult = null) }

        val result = backupService.importFromJson(inputStream)

        _uiState.update { currentState ->
            currentState.copy(
                restoreInProgress = false,
                restoreResult = if (result.isSuccess) {
                    val importResult = result.getOrNull()
                    RestoreResult(
                        success = importResult?.success ?: false,
                        message = if (importResult?.success == true) {
                            "Imported ${importResult.itemsImported} items, " +
                                "${importResult.notebooksImported} notebooks, " +
                                "${importResult.curvesImported} curves, and " +
                                "${importResult.logsImported} review logs."
                        } else {
                            "Import failed: no data was restored."
                        },
                        importResult = importResult
                    )
                } else {
                    RestoreResult(
                        success = false,
                        message = "Import failed: ${result.exceptionOrNull()?.message ?: "Unknown error"}"
                    )
                }
            )
        }
    }

    suspend fun importFromCsv(inputStream: InputStream) {
        _uiState.update { it.copy(restoreInProgress = true, restoreResult = null) }

        runCatching {
            importService.importCsv(inputStream)
        }.fold(
            onSuccess = { report ->
                _uiState.update { currentState ->
                    currentState.copy(
                        restoreInProgress = false,
                        restoreResult = RestoreResult(
                            success = report.imported > 0 || report.skipped > 0,
                            message = buildImportMessage("CSV import", report)
                        )
                    )
                }
            },
            onFailure = { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        restoreInProgress = false,
                        restoreResult = RestoreResult(
                            success = false,
                            message = "CSV import failed: ${throwable.message ?: "Unknown error"}"
                        )
                    )
                }
            }
        )
    }

    suspend fun importFromAnki(inputStream: InputStream) {
        _uiState.update { it.copy(restoreInProgress = true, restoreResult = null) }

        runCatching {
            importService.importAnki(inputStream)
        }.fold(
            onSuccess = { report ->
                _uiState.update { currentState ->
                    currentState.copy(
                        restoreInProgress = false,
                        restoreResult = RestoreResult(
                            success = report.imported > 0 || report.skipped > 0,
                            message = buildImportMessage("Anki import", report)
                        )
                    )
                }
            },
            onFailure = { throwable ->
                _uiState.update { currentState ->
                    currentState.copy(
                        restoreInProgress = false,
                        restoreResult = RestoreResult(
                            success = false,
                            message = "Anki import failed: ${throwable.message ?: "Unknown error"}"
                        )
                    )
                }
            }
        )
    }

    fun clearBackupResult() {
        _uiState.update { it.copy(backupResult = null) }
    }

    fun clearRestoreResult() {
        _uiState.update { it.copy(restoreResult = null) }
    }

    fun generateBackupFileName(format: BackupFormat): String {
        return backupService.generateBackupFileName(format)
    }

    fun getBackupMimeType(format: BackupFormat): String {
        return backupService.getMimeType(format)
    }

    private fun buildImportMessage(prefix: String, report: ImportReport): String {
        val summary = "$prefix finished: ${report.imported} imported, ${report.skipped} skipped, ${report.failed} failed."
        if (report.errors.isEmpty()) {
            return summary
        }

        val detail = report.errors.take(3).joinToString(separator = " ") { error ->
            "Line ${error.line}: ${error.message}."
        }
        return "$summary $detail"
    }
}
