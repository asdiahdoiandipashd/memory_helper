package com.example.memoryhelper.data.importing

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.text.Html
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
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

@Singleton
class DefaultImportService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryItemDao: MemoryItemDao,
    private val notebookDao: NotebookDao,
    private val reviewCurveDao: ReviewCurveDao,
    private val memoryTagDao: MemoryTagDao,
    private val memoryItemTagDao: MemoryItemTagDao
) : ImportService {

    private val json = Json { ignoreUnknownKeys = true }

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
                        val notebookName = getColumn(cols, index, "notebook").ifBlank { "Imported Cards" }
                        val tagNames = parseCsvTags(getColumn(cols, index, "tags"))
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

                            val tagIds = ensureTags(tagNames)
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
        val defaultCurveId = reviewCurveDao.getDefaultCurve()?.id
        val now = System.currentTimeMillis()
        val firstIntervalMs = 5L * 60_000L
        val tempDir = File(context.cacheDir, "anki_import_${System.currentTimeMillis()}")
        val mediaOutputDir = File(context.filesDir, "imported_media/anki")
        mediaOutputDir.mkdirs()

        return try {
            tempDir.mkdirs()
            val extracted = extractApkg(input, tempDir)
            val modelNames = readModelNames(extracted.databaseFile)
            val deckNames = readDeckNames(extracted.databaseFile)
            val copiedMedia = mutableMapOf<String, File>()
            val errors = mutableListOf<ImportError>()
            var imported = 0
            var skipped = 0

            val database = SQLiteDatabase.openDatabase(
                extracted.databaseFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY
            )

            try {
                val cursor = database.rawQuery(
                    """
                    SELECT
                        n.id AS note_id,
                        n.tags AS tags,
                        n.flds AS flds,
                        n.mid AS mid,
                        MIN(c.did) AS did
                    FROM notes n
                    LEFT JOIN cards c ON c.nid = n.id
                    GROUP BY n.id, n.tags, n.flds, n.mid
                    ORDER BY n.id ASC
                    """.trimIndent(),
                    null
                )

                cursor.use { rows ->
                    var rowIndex = 1
                    while (rows.moveToNext()) {
                        try {
                            val noteId = rows.getLong(rows.getColumnIndexOrThrow("note_id"))
                            val tagsRaw = rows.getString(rows.getColumnIndexOrThrow("tags")).orEmpty()
                            val fieldsRaw = rows.getString(rows.getColumnIndexOrThrow("flds")).orEmpty()
                            val modelId = rows.getLong(rows.getColumnIndexOrThrow("mid"))
                            val deckId = rows.getLong(rows.getColumnIndexOrThrow("did"))

                            val fields = fieldsRaw.split(ANKI_FIELD_SEPARATOR)
                            if (fields.size < 2) {
                                throw IllegalArgumentException("Only basic two-sided notes are supported")
                            }

                            val frontRaw = fields[0]
                            val backRaw = fields[1]
                            val title = sanitizeAnkiField(frontRaw)
                                .ifBlank { throw IllegalArgumentException("Front field is empty after cleanup") }
                            val content = sanitizeAnkiField(backRaw)
                            val modelName = modelNames[modelId].orEmpty()
                            val deckName = deckNames[deckId].takeUnless { it.isNullOrBlank() }
                                ?: modelName.takeUnless { it.isBlank() }
                                ?: "Anki Import"
                            val notebookId = ensureNotebook(deckName)

                            val existingId = memoryItemDao.findIdByDedup(notebookId, title, content)
                            if (existingId != null) {
                                skipped += 1
                                rowIndex += 1
                                continue
                            }

                            val mediaRefs = materializeMediaRefs(
                                noteId = noteId,
                                rawFields = listOf(frontRaw, backRaw),
                                mediaMap = extracted.mediaMap,
                                extractedMediaFiles = extracted.mediaFiles,
                                copiedMedia = copiedMedia,
                                outputDir = mediaOutputDir
                            )

                            val item = MemoryItem(
                                notebookId = notebookId,
                                curveId = defaultCurveId,
                                title = title,
                                content = content,
                                status = MemoryItemStatus.REVIEWING,
                                stageIndex = 0,
                                nextReviewTime = now + firstIntervalMs,
                                lastReviewTime = now,
                                createdAt = now,
                                updatedAt = now,
                                mediaRefs = json.encodeToString(mediaRefs),
                                sourceType = "anki_import"
                            )
                            val itemId = memoryItemDao.insert(item)
                            val tagIds = ensureTags(parseAnkiTags(tagsRaw))
                            if (tagIds.isNotEmpty()) {
                                memoryItemTagDao.replaceItemTags(itemId, tagIds)
                            }
                            imported += 1
                        } catch (e: Exception) {
                            errors.add(ImportError(rowIndex, e.message ?: "Invalid Anki note"))
                        }
                        rowIndex += 1
                    }
                }
            } finally {
                database.close()
            }

            ImportReport(
                imported = imported,
                skipped = skipped,
                failed = errors.size,
                errors = errors
            )
        } catch (e: Exception) {
            ImportReport(
                imported = 0,
                skipped = 0,
                failed = 1,
                errors = listOf(ImportError(1, e.message ?: "Failed to import Anki package"))
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun extractApkg(input: InputStream, workingDir: File): ExtractedApkg {
        var databaseFile: File? = null
        val mediaFiles = mutableMapOf<String, File>()
        var mediaJson = "{}"

        ZipInputStream(input).use { zipInputStream ->
            var entry = zipInputStream.nextEntry
            while (entry != null) {
                if (!entry.isDirectory) {
                    when (entry.name) {
                        "collection.anki2", "collection.anki21" -> {
                            val target = File(workingDir, entry.name.replace('/', '_'))
                            writeZipEntry(zipInputStream, target)
                            databaseFile = target
                        }
                        "media" -> {
                            val mediaFile = File(workingDir, "media.json")
                            writeZipEntry(zipInputStream, mediaFile)
                            mediaJson = mediaFile.readText()
                        }
                        else -> {
                            val target = File(workingDir, entry.name.replace('/', '_'))
                            writeZipEntry(zipInputStream, target)
                            mediaFiles[entry.name] = target
                        }
                    }
                }
                zipInputStream.closeEntry()
                entry = zipInputStream.nextEntry
            }
        }

        return ExtractedApkg(
            databaseFile = databaseFile ?: throw IllegalArgumentException("collection.anki2 not found in package"),
            mediaMap = parseMediaMap(mediaJson),
            mediaFiles = mediaFiles
        )
    }

    private fun writeZipEntry(zipInputStream: ZipInputStream, target: File) {
        FileOutputStream(target).use { output ->
            zipInputStream.copyTo(output)
        }
    }

    private fun parseMediaMap(raw: String): Map<String, String> {
        return runCatching {
            json.parseToJsonElement(raw).jsonObject.mapValues { entry ->
                entry.value.jsonPrimitive.content
            }
        }.getOrDefault(emptyMap())
    }

    private fun readModelNames(databaseFile: File): Map<Long, String> {
        val database = SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            val cursor = database.rawQuery("SELECT models FROM col LIMIT 1", null)
            cursor.use { row ->
                if (!row.moveToFirst()) return emptyMap()
                parseNamedJsonMap(row.getString(0).orEmpty())
            }
        } finally {
            database.close()
        }
    }

    private fun readDeckNames(databaseFile: File): Map<Long, String> {
        val database = SQLiteDatabase.openDatabase(databaseFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        return try {
            val cursor = database.rawQuery("SELECT decks FROM col LIMIT 1", null)
            cursor.use { row ->
                if (!row.moveToFirst()) return emptyMap()
                parseNamedJsonMap(row.getString(0).orEmpty())
            }
        } finally {
            database.close()
        }
    }

    private fun parseNamedJsonMap(raw: String): Map<Long, String> {
        return runCatching {
            json.parseToJsonElement(raw).jsonObject.mapNotNull { (key, value) ->
                val id = key.toLongOrNull() ?: return@mapNotNull null
                val name = value.jsonObject["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
                id to name
            }.toMap()
        }.getOrDefault(emptyMap())
    }

    private fun materializeMediaRefs(
        noteId: Long,
        rawFields: List<String>,
        mediaMap: Map<String, String>,
        extractedMediaFiles: Map<String, File>,
        copiedMedia: MutableMap<String, File>,
        outputDir: File
    ): List<ImportedMediaRef> {
        val referencedNames = linkedSetOf<String>()
        rawFields.forEach { field ->
            IMAGE_REGEX.findAll(field).forEach { match ->
                referencedNames += match.groupValues[1]
            }
            SOUND_REGEX.findAll(field).forEach { match ->
                referencedNames += match.groupValues[1]
            }
        }

        if (referencedNames.isEmpty()) return emptyList()

        val reverseMediaMap = mediaMap.entries.associate { it.value to it.key }
        val refs = mutableListOf<ImportedMediaRef>()
        referencedNames.forEach { originalName ->
            val entryKey = reverseMediaMap[originalName] ?: originalName
            val extracted = extractedMediaFiles[entryKey] ?: extractedMediaFiles[originalName] ?: return@forEach
            val destination = copiedMedia.getOrPut(originalName) {
                copyMediaFile(
                    source = extracted,
                    outputDir = outputDir,
                    fileName = "${noteId}_${sanitizeFileName(originalName)}"
                )
            }
            refs += ImportedMediaRef(
                uri = destination.absolutePath,
                type = detectMediaType(originalName),
                durationMs = 0L,
                sourceName = originalName
            )
        }
        return refs
    }

    private fun copyMediaFile(source: File, outputDir: File, fileName: String): File {
        val destination = File(outputDir, fileName)
        source.copyTo(destination, overwrite = true)
        return destination
    }

    private fun sanitizeAnkiField(raw: String): String {
        val normalized = raw
            .replace("<br>", "\n", ignoreCase = true)
            .replace("<br/>", "\n", ignoreCase = true)
            .replace("<br />", "\n", ignoreCase = true)
        return Html.fromHtml(normalized, Html.FROM_HTML_MODE_LEGACY)
            .toString()
            .replace('\u00A0', ' ')
            .trim()
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

    private suspend fun ensureTags(tagNames: List<String>): List<Long> {
        if (tagNames.isEmpty()) return emptyList()

        val ids = mutableListOf<Long>()
        for (name in tagNames) {
            val existing = memoryTagDao.getByName(name)
            val id = existing?.id ?: memoryTagDao.insert(MemoryTag(name = name))
            ids.add(id)
        }
        return ids
    }

    private fun parseCsvTags(tagsRaw: String): List<String> {
        if (tagsRaw.isBlank()) return emptyList()
        return tagsRaw.split(";", ",")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun parseAnkiTags(tagsRaw: String): List<String> {
        if (tagsRaw.isBlank()) return emptyList()
        return tagsRaw.trim()
            .split(Regex("\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
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

    private fun sanitizeFileName(name: String): String {
        return name.replace(Regex("[^A-Za-z0-9._-]"), "_")
    }

    private fun detectMediaType(fileName: String): String {
        val extension = fileName.substringAfterLast('.', "").lowercase()
        return when (extension) {
            "jpg", "jpeg", "png", "gif", "webp", "bmp" -> "image"
            "mp3", "wav", "ogg", "m4a", "aac" -> "audio"
            "mp4", "webm", "mov" -> "video"
            else -> "file"
        }
    }

    private data class ExtractedApkg(
        val databaseFile: File,
        val mediaMap: Map<String, String>,
        val mediaFiles: Map<String, File>
    )

    @Serializable
    private data class ImportedMediaRef(
        val uri: String,
        val type: String,
        val durationMs: Long = 0L,
        val sourceName: String
    )

    private companion object {
        const val ANKI_FIELD_SEPARATOR = '\u001f'
        val IMAGE_REGEX = Regex("""<img[^>]+src=['"]([^'"]+)['"][^>]*>""", RegexOption.IGNORE_CASE)
        val SOUND_REGEX = Regex("""\[sound:([^\]]+)]""")
    }
}
