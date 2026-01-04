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
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject

/**
 * Permission status data class
 */
data class PermissionStatus(
    val isGranted: Boolean,
    val isRequired: Boolean = true  // Some permissions are only required on certain API levels
)

/**
 * Backup result data class
 */
data class BackupResult(
    val success: Boolean,
    val message: String,
    val fileName: String? = null,
    val format: BackupFormat? = null
)

/**
 * Restore result data class
 */
data class RestoreResult(
    val success: Boolean,
    val message: String,
    val importResult: ImportResult? = null
)

/**
 * Settings UI state
 */
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
    private val backupService: BackupService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        refreshAllPermissions()
    }

    /**
     * Refreshes all permission statuses
     */
    fun refreshAllPermissions() {
        _uiState.update { currentState ->
            currentState.copy(
                notificationPermission = checkNotificationPermission(),
                exactAlarmPermission = checkExactAlarmPermission(),
                batteryOptimizationWhitelisted = checkBatteryOptimization()
            )
        }
    }

    /**
     * Check notification permission status
     */
    private fun checkNotificationPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            PermissionStatus(isGranted = isGranted, isRequired = true)
        } else {
            // Check if notifications are enabled via NotificationManager for older versions
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            PermissionStatus(isGranted = notificationManager.areNotificationsEnabled(), isRequired = true)
        }
    }

    /**
     * Check exact alarm permission status (Android 12+)
     */
    private fun checkExactAlarmPermission(): PermissionStatus {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val isGranted = alarmManager?.canScheduleExactAlarms() == true
            PermissionStatus(isGranted = isGranted, isRequired = true)
        } else {
            // Not required before Android 12
            PermissionStatus(isGranted = true, isRequired = false)
        }
    }

    /**
     * Check if app is whitelisted from battery optimization
     */
    private fun checkBatteryOptimization(): PermissionStatus {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val isIgnoring = powerManager?.isIgnoringBatteryOptimizations(context.packageName) == true
        return PermissionStatus(isGranted = isIgnoring, isRequired = true)
    }

    /**
     * Marks that test notification was sent (for UI feedback)
     */
    fun onTestNotificationSent() {
        _uiState.update { it.copy(isTestNotificationSent = true) }
    }

    /**
     * Resets the test notification sent flag
     */
    fun resetTestNotificationFlag() {
        _uiState.update { it.copy(isTestNotificationSent = false) }
    }

    /**
     * Export data to JSON format
     */
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
                        message = "数据备份成功",
                        fileName = fileName,
                        format = BackupFormat.JSON
                    )
                } else {
                    BackupResult(
                        success = false,
                        message = "备份失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            )
        }
    }

    /**
     * Export data to CSV format
     */
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
                        message = "CSV导出成功",
                        fileName = fileName,
                        format = BackupFormat.CSV
                    )
                } else {
                    BackupResult(
                        success = false,
                        message = "CSV导出失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            )
        }
    }

    /**
     * Import data from JSON backup
     */
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
                            "成功导入 ${importResult.itemsImported} 个记忆条目，" +
                            "${importResult.notebooksImported} 个记忆本，" +
                            "${importResult.curvesImported} 个复习曲线，" +
                            "${importResult.logsImported} 条复习记录"
                        } else {
                            "导入失败: 没有数据被导入"
                        },
                        importResult = importResult
                    )
                } else {
                    RestoreResult(
                        success = false,
                        message = "导入失败: ${result.exceptionOrNull()?.message ?: "未知错误"}"
                    )
                }
            )
        }
    }

    /**
     * Clear backup result
     */
    fun clearBackupResult() {
        _uiState.update { it.copy(backupResult = null) }
    }

    /**
     * Clear restore result
     */
    fun clearRestoreResult() {
        _uiState.update { it.copy(restoreResult = null) }
    }

    /**
     * Generate backup file name
     */
    fun generateBackupFileName(format: BackupFormat): String {
        return backupService.generateBackupFileName(format)
    }

    /**
     * Get backup file MIME type
     */
    fun getBackupMimeType(format: BackupFormat): String {
        return backupService.getMimeType(format)
    }
}
