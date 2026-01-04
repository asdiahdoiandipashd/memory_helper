package com.example.memoryhelper.data.local

import androidx.room.TypeConverter
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Room TypeConverters for complex data types.
 * Uses Kotlin Serialization for JSON conversion.
 */
class Converters {

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * Convert List<Long> to JSON String for storage
     */
    @TypeConverter
    fun fromLongList(value: List<Long>): String {
        return json.encodeToString(value)
    }

    /**
     * Convert JSON String back to List<Long>
     */
    @TypeConverter
    fun toLongList(value: String): List<Long> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Convert List<String> to JSON String for storage (for image paths)
     */
    @TypeConverter
    fun fromStringList(value: List<String>): String {
        return json.encodeToString(value)
    }

    /**
     * Convert JSON String back to List<String>
     */
    @TypeConverter
    fun toStringList(value: String): List<String> {
        return try {
            json.decodeFromString(value)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
