package com.example.memoryhelper.data.backup

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backup service for exporting and importing app data
 */
@Singleton
class BackupService @Inject constructor(
    private val memoryItemDao: MemoryItemDao,
    private val notebookDao: NotebookDao,
    private val reviewCurveDao: ReviewCurveDao,
    private val reviewLogDao: ReviewLogDao,
    private val context: Context
) {
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
    }

    /**
     * Export all data to JSON format
     */
    suspend fun exportToJson(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get all data from database
            val items = memoryItemDao.getAllItems()
            val notebooks = notebookDao.getAllNotebooks()
            val curves = reviewCurveDao.getAllCurves()
            val logs = reviewLogDao.getAllLogs()

            // Convert to DTOs
            val itemDtos = items.map { MemoryItemDto.fromEntity(it) }
            val notebookDtos = notebooks.map { NotebookDto.fromEntity(it) }
            val curveDtos = curves.map { ReviewCurveDto.fromEntity(it) }
            val logDtos = logs.map { ReviewLogDto.fromEntity(it) }

            // Create backup data container
            val backupData = BackupData(
                items = itemDtos,
                notebooks = notebookDtos,
                logs = logDtos,
                curves = curveDtos,
                version = 1,
                timestamp = System.currentTimeMillis()
            )

            // Serialize to JSON
            val jsonString = json.encodeToString(backupData)
            
            // Write to output stream
            outputStream.use { stream ->
                stream.write(jsonString.toByteArray())
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Import data from JSON backup
     */
    suspend fun importFromJson(inputStream: InputStream): Result<ImportResult> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Read JSON from input stream
            val jsonString = inputStream.use { stream ->
                stream.bufferedReader().use { reader ->
                    reader.readText()
                }
            }

            // Parse backup data
            val backupData = json.decodeFromString<BackupData>(jsonString)

            // Clear existing data (optional - could be configurable)
            // For safety, we'll keep existing data and add new data with new IDs
            
            // Import notebooks (preserve IDs if possible)
            val notebookIdMap = mutableMapOf<Long, Long>()
            for (notebookDto in backupData.notebooks) {
                val existingNotebook = notebookDao.getById(notebookDto.id)
                if (existingNotebook == null) {
                    // Insert new notebook
                    val newId = notebookDao.insert(notebookDto.toEntity())
                    notebookIdMap[notebookDto.id] = newId
                } else {
                    // Update existing notebook
                    notebookDao.update(notebookDto.toEntity())
                    notebookIdMap[notebookDto.id] = notebookDto.id
                }
            }

            // Import curves
            val curveIdMap = mutableMapOf<Long, Long>()
            for (curveDto in backupData.curves) {
                val existingCurve = reviewCurveDao.getById(curveDto.id)
                if (existingCurve == null) {
                    // Insert new curve
                    val newId = reviewCurveDao.insert(curveDto.toEntity())
                    curveIdMap[curveDto.id] = newId
                } else {
                    // Update existing curve
                    reviewCurveDao.update(curveDto.toEntity())
                    curveIdMap[curveDto.id] = curveDto.id
                }
            }

            // Import memory items
            var itemsImported = 0
            var itemsSkipped = 0
            for (itemDto in backupData.items) {
                val existingItem = memoryItemDao.getById(itemDto.id)
                if (existingItem == null) {
                    // Map notebook and curve IDs
                    val mappedNotebookId = notebookIdMap[itemDto.notebookId] ?: itemDto.notebookId
                    val mappedCurveId = itemDto.curveId?.let { curveIdMap[it] } ?: itemDto.curveId
                    
                    val mappedItem = itemDto.copy(
                        notebookId = mappedNotebookId,
                        curveId = mappedCurveId
                    ).toEntity()
                    
                    memoryItemDao.insert(mappedItem)
                    itemsImported++
                } else {
                    itemsSkipped++
                }
            }

            // Import review logs
            var logsImported = 0
            for (logDto in backupData.logs) {
                val existingLog = reviewLogDao.getById(logDto.id)
                if (existingLog == null) {
                    reviewLogDao.insert(logDto.toEntity())
                    logsImported++
                }
            }

            Result.success(
                ImportResult(
                    itemsImported = itemsImported,
                    itemsSkipped = itemsSkipped,
                    notebooksImported = backupData.notebooks.size,
                    curvesImported = backupData.curves.size,
                    logsImported = logsImported,
                    totalItems = backupData.items.size
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Export data to CSV format
     */
    suspend fun exportToCsv(outputStream: OutputStream): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get all memory items
            val items = memoryItemDao.getAllItems()
            
            // Create CSV header
            val header = "ID,Title,Content,Status,Stage,Next Review,Last Review,Created At\n"
            
            // Create CSV rows
            val rows = items.joinToString("\n") { item ->
                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                val nextReview = if (item.nextReviewTime > 0 && item.nextReviewTime < Long.MAX_VALUE) {
                    dateFormat.format(Date(item.nextReviewTime))
                } else "N/A"
                
                val lastReview = if (item.lastReviewTime > 0) {
                    dateFormat.format(Date(item.lastReviewTime))
                } else "N/A"
                
                val createdAt = dateFormat.format(Date(item.createdAt))
                
                listOf(
                    item.id.toString(),
                    "\"${item.title.replace("\"", "\"\"")}\"",
                    "\"${item.content.replace("\"", "\"\"")}\"",
                    when (item.status) {
                        0 -> "New"
                        1 -> "Reviewing"
                        2 -> "Completed"
                        3 -> "Paused"
                        else -> "Unknown"
                    },
                    item.stageIndex.toString(),
                    nextReview,
                    lastReview,
                    createdAt
                ).joinToString(",")
            }
            
            // Write to output stream
            outputStream.use { stream ->
                stream.write(header.toByteArray())
                stream.write(rows.toByteArray())
            }
            
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Generate backup file name with timestamp
     */
    fun generateBackupFileName(format: BackupFormat): String {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        return when (format) {
            BackupFormat.JSON -> "memory_helper_backup_${timestamp}.json"
            BackupFormat.CSV -> "memory_helper_backup_${timestamp}.csv"
        }
    }

    /**
     * Get backup file MIME type
     */
    fun getMimeType(format: BackupFormat): String {
        return when (format) {
            BackupFormat.JSON -> "application/json"
            BackupFormat.CSV -> "text/csv"
        }
    }
}

/**
 * Result of import operation
 */
data class ImportResult(
    val itemsImported: Int,
    val itemsSkipped: Int,
    val notebooksImported: Int,
    val curvesImported: Int,
    val logsImported: Int,
    val totalItems: Int
) {
    val success: Boolean get() = itemsImported > 0 || notebooksImported > 0 || curvesImported > 0
}

/**
 * Backup format options
 */
enum class BackupFormat {
    JSON,
    CSV
}
