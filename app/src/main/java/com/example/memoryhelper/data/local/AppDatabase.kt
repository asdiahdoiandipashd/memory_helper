package com.example.memoryhelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import com.example.memoryhelper.data.local.dao.TodoTagDao
import com.example.memoryhelper.data.local.dao.TodoTaskDao
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.data.local.entity.ReviewCurve
import com.example.memoryhelper.data.local.entity.ReviewLog
import com.example.memoryhelper.data.local.entity.TodoTag
import com.example.memoryhelper.data.local.entity.TodoTask
import com.example.memoryhelper.data.local.entity.TodoTaskTagCrossRef

/**
 * Room Database for Memory Helper app.
 * Contains ReviewCurve, MemoryItem, ReviewLog, and Notebook entities.
 */
@Database(
    entities = [
        ReviewCurve::class,
        MemoryItem::class,
        ReviewLog::class,
        Notebook::class,
        TodoTask::class,
        TodoTag::class,
        TodoTaskTagCrossRef::class
    ],
    version = 4,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reviewCurveDao(): ReviewCurveDao
    abstract fun memoryItemDao(): MemoryItemDao
    abstract fun reviewLogDao(): ReviewLogDao
    abstract fun notebookDao(): NotebookDao
    abstract fun todoTaskDao(): TodoTaskDao
    abstract fun todoTagDao(): TodoTagDao

    companion object {
        const val DATABASE_NAME = "memory_helper_db"

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todo_tasks (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        title TEXT NOT NULL,
                        note TEXT NOT NULL,
                        is_daily INTEGER NOT NULL,
                        is_completed INTEGER NOT NULL,
                        last_completed_day INTEGER,
                        last_completed_at INTEGER,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todo_tasks_is_daily ON todo_tasks(is_daily)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todo_tasks_is_completed ON todo_tasks(is_completed)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todo_tasks_updated_at ON todo_tasks(updated_at)"
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todo_tags (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS todo_task_tag_cross_ref (
                        task_id INTEGER NOT NULL,
                        tag_id INTEGER NOT NULL,
                        PRIMARY KEY(task_id, tag_id),
                        FOREIGN KEY(task_id) REFERENCES todo_tasks(id) ON DELETE CASCADE,
                        FOREIGN KEY(tag_id) REFERENCES todo_tags(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todo_task_tag_cross_ref_task_id ON todo_task_tag_cross_ref(task_id)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_todo_task_tag_cross_ref_tag_id ON todo_task_tag_cross_ref(tag_id)"
                )
            }
        }
    }
}
