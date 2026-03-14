package com.example.memoryhelper.data.importing

import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.MemoryItemTagDao
import com.example.memoryhelper.data.local.dao.MemoryTagDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import com.example.memoryhelper.data.local.entity.MemoryTag
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.domain.importing.ImportError
import com.example.memoryhelper.domain.importing.ImportReport
import com.example.memoryhelper.domain.importing.ImportService
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DefaultImportService @Inject constructor(
    private val memoryItemDao: MemoryItemDao,
    private val notebookDao: NotebookDao,
    private val reviewCurveDao: ReviewCurveDao,
    private val memoryTagDao: MemoryTagDao,
    private val memoryItemTagDao: MemoryItemTagDao
) : ImportService {

    override suspend fun importCsv(input: InputStream): ImportReport {
        BufferedReader(InputStreamReader(input)).use { reader ->
            val headerLine = reader.readLine()
                ?: return ImportReport(0, 0, 1, listOf(ImportError(1, "Empty CSV")))
            val headers = parseCsvLine(headerLine).map { it.trim().lowercase() }
            val index = headers.withIndex().associate { it.value to it.index }

            var lineNo = 1
            var imported = 0
            var skipped = 0
            val errors = mutableListOf<ImportError>()

            val defaultCurveId = reviewCurveDao.getDefaultCurve()?.id
            val now = System.currentTimeMillis()
            val firstIntervalMs = 5L * 60_000L

            var line = reader.readLine()
            while (line != null) {
                lineNo += 1
                if (line.isNotBlank()) {
                    try {
                        val cols = parseCsvLine(line)
                        val title = getColumn(cols, index, "title")
                            .ifBlank { throw IllegalArgumentException("title is required") }
                        val content = getColumn(cols, index, "content")
                        val notebookName = getColumn(cols, index, "notebook").ifBlank { "默认生词本" }
                        val tagsRaw = getColumn(cols, index, "tags")
                        val stage = getColumn(cols, index, "stage").toIntOrNull()?.coerceAtLeast(0) ?: 0
                        val nextReviewTime = getColumn(cols, index, "next_review_time").toLongOrNull()
                            ?: now + firstIntervalMs

                        val notebookId = ensureNotebook(notebookName)
                        val existingId = memoryItemDao.findIdByDedup(notebookId, title, content)
                        if (existingId != null) {
                            skipped += 1
                        } else {
                            val item = MemoryItem(
                                notebookId = notebookId,
                                curveId = defaultCurveId,
                                title = title,
                                content = content,
                                status = MemoryItemStatus.REVIEWING,
                                stageIndex = stage,
                                nextReviewTime = max(nextReviewTime, now),
                                lastReviewTime = now,
                                createdAt = now,
                                updatedAt = now,
                                sourceType = "csv_import"
                            )
                            val itemId = memoryItemDao.insert(item)

                            val tagIds = ensureTags(tagsRaw)
                            if (tagIds.isNotEmpty()) {
                                memoryItemTagDao.replaceItemTags(itemId, tagIds)
                            }
                            imported += 1
                        }
                    } catch (e: Exception) {
                        errors.add(ImportError(lineNo, e.message ?: "Invalid row"))
                    }
                }
                line = reader.readLine()
            }

            return ImportReport(
                imported = imported,
                skipped = skipped,
                failed = errors.size,
                errors = errors
            )
        }
    }

    override suspend fun importAnki(input: InputStream): ImportReport {
        input.close()
        return ImportReport(
            imported = 0,
            skipped = 0,
            failed = 1,
            errors = listOf(
                ImportError(
                    line = 1,
                    message = "Anki package import requires APKG parser and media map support; this build provides CSV import first."
                )
            )
        )
    }

    private suspend fun ensureNotebook(name: String): Long {
        val normalized = name.trim()
        val found = notebookDao.getAllNotebooks().firstOrNull { it.name == normalized }
        return if (found != null) {
            found.id
        } else {
            notebookDao.insert(Notebook(name = normalized))
        }
    }

    private suspend fun ensureTags(tagsRaw: String): List<Long> {
        if (tagsRaw.isBlank()) return emptyList()
        val names = tagsRaw.split(";", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()

        val ids = mutableListOf<Long>()
        for (name in names) {
            val existing = memoryTagDao.getByName(name)
            val id = existing?.id ?: memoryTagDao.insert(MemoryTag(name = name))
            ids.add(id)
        }
        return ids
    }

    private fun getColumn(cols: List<String>, index: Map<String, Int>, key: String): String {
        val i = index[key] ?: return ""
        return cols.getOrNull(i)?.trim().orEmpty()
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val current = StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            when {
                c == '"' -> {
                    if (inQuotes && i + 1 < line.length && line[i + 1] == '"') {
                        current.append('"')
                        i += 1
                    } else {
                        inQuotes = !inQuotes
                    }
                }
                c == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current.clear()
                }
                else -> current.append(c)
            }
            i += 1
        }
        result.add(current.toString())
        return result
    }
}
