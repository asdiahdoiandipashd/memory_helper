package com.example.memoryhelper.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.memoryhelper.data.local.dao.DailyPlanItemDao
import com.example.memoryhelper.data.local.dao.ExamPlanDao
import com.example.memoryhelper.data.local.dao.ExamSubjectDao
import com.example.memoryhelper.data.local.dao.MemoryItemTagDao
import com.example.memoryhelper.data.local.dao.MemoryTagDao
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import com.example.memoryhelper.data.local.dao.NotebookDao
import com.example.memoryhelper.data.local.dao.ReviewCurveDao
import com.example.memoryhelper.data.local.dao.ReviewLogDao
import com.example.memoryhelper.data.local.dao.SyncEventDao
import com.example.memoryhelper.data.local.dao.SyncStateDao
import com.example.memoryhelper.data.local.dao.TodoTagDao
import com.example.memoryhelper.data.local.dao.TodoTaskDao
import com.example.memoryhelper.data.local.entity.DailyPlanItem
import com.example.memoryhelper.data.local.entity.ExamPlan
import com.example.memoryhelper.data.local.entity.ExamSubject
import com.example.memoryhelper.data.local.entity.MemoryItemTagCrossRef
import com.example.memoryhelper.data.local.entity.MemoryTag
import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.Notebook
import com.example.memoryhelper.data.local.entity.ReviewCurve
import com.example.memoryhelper.data.local.entity.ReviewLog
import com.example.memoryhelper.data.local.entity.SyncEvent
import com.example.memoryhelper.data.local.entity.SyncState
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
        TodoTaskTagCrossRef::class,
        ExamPlan::class,
        ExamSubject::class,
        DailyPlanItem::class,
        MemoryTag::class,
        MemoryItemTagCrossRef::class,
        SyncEvent::class,
        SyncState::class
    ],
    version = 7,
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
    abstract fun examPlanDao(): ExamPlanDao
    abstract fun examSubjectDao(): ExamSubjectDao
    abstract fun dailyPlanItemDao(): DailyPlanItemDao
    abstract fun memoryTagDao(): MemoryTagDao
    abstract fun memoryItemTagDao(): MemoryItemTagDao
    abstract fun syncEventDao(): SyncEventDao
    abstract fun syncStateDao(): SyncStateDao

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

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE memory_items ADD COLUMN card_type TEXT NOT NULL DEFAULT 'basic'")
                database.execSQL("ALTER TABLE memory_items ADD COLUMN media_refs TEXT NOT NULL DEFAULT '[]'")
                database.execSQL("ALTER TABLE memory_items ADD COLUMN source_type TEXT NOT NULL DEFAULT 'manual'")
                database.execSQL("ALTER TABLE memory_items ADD COLUMN updated_at INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE memory_items ADD COLUMN deleted_at INTEGER")
                database.execSQL("UPDATE memory_items SET updated_at = created_at WHERE updated_at = 0")

                database.execSQL("ALTER TABLE review_logs ADD COLUMN grade INTEGER NOT NULL DEFAULT 3")
                database.execSQL("ALTER TABLE review_logs ADD COLUMN response_ms INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE review_logs ADD COLUMN due_delta_ms INTEGER NOT NULL DEFAULT 0")
                database.execSQL("ALTER TABLE review_logs ADD COLUMN scheduler_version TEXT NOT NULL DEFAULT 'v2'")

                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_memory_items_next_review_status_notebook ON memory_items(next_review_time, status, notebook_id)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_review_logs_actual_grade_item ON review_logs(actual_review_time, grade, item_id)"
                )
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exam_plan (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        exam_date_epoch_day INTEGER NOT NULL,
                        daily_budget INTEGER NOT NULL,
                        overdue_compensation_limit INTEGER NOT NULL,
                        is_active INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS exam_subject (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exam_plan_id INTEGER NOT NULL,
                        name TEXT NOT NULL,
                        weight REAL NOT NULL,
                        notebook_id INTEGER,
                        daily_budget_override INTEGER,
                        created_at INTEGER NOT NULL,
                        updated_at INTEGER NOT NULL,
                        FOREIGN KEY(exam_plan_id) REFERENCES exam_plan(id) ON DELETE CASCADE,
                        FOREIGN KEY(notebook_id) REFERENCES notebooks(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_exam_subject_exam_plan_id ON exam_subject(exam_plan_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_exam_subject_notebook_id ON exam_subject(notebook_id)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS daily_plan_item (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        exam_plan_id INTEGER NOT NULL,
                        plan_date INTEGER NOT NULL,
                        memory_item_id INTEGER NOT NULL,
                        exam_subject_id INTEGER,
                        priority INTEGER NOT NULL,
                        status INTEGER NOT NULL,
                        scheduled_at INTEGER NOT NULL,
                        created_at INTEGER NOT NULL,
                        FOREIGN KEY(exam_plan_id) REFERENCES exam_plan(id) ON DELETE CASCADE,
                        FOREIGN KEY(memory_item_id) REFERENCES memory_items(id) ON DELETE CASCADE,
                        FOREIGN KEY(exam_subject_id) REFERENCES exam_subject(id) ON DELETE SET NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_plan_date ON daily_plan_item(plan_date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_priority ON daily_plan_item(priority)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_status ON daily_plan_item(status)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_exam_plan_id ON daily_plan_item(exam_plan_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_memory_item_id ON daily_plan_item(memory_item_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_plan_item_exam_subject_id ON daily_plan_item(exam_subject_id)")
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_daily_plan_item_plan_date_priority_status ON daily_plan_item(plan_date, priority, status)"
                )
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS memory_tag (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        name TEXT NOT NULL,
                        color INTEGER NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_memory_tag_name ON memory_tag(name)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS memory_item_tag_cross_ref (
                        memory_item_id INTEGER NOT NULL,
                        tag_id INTEGER NOT NULL,
                        PRIMARY KEY(memory_item_id, tag_id),
                        FOREIGN KEY(memory_item_id) REFERENCES memory_items(id) ON DELETE CASCADE,
                        FOREIGN KEY(tag_id) REFERENCES memory_tag(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_memory_item_tag_cross_ref_memory_item_id ON memory_item_tag_cross_ref(memory_item_id)"
                )
                database.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_memory_item_tag_cross_ref_tag_id ON memory_item_tag_cross_ref(tag_id)"
                )

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_event (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        entity_type TEXT NOT NULL,
                        entity_id INTEGER NOT NULL,
                        action TEXT NOT NULL,
                        payload_json TEXT NOT NULL,
                        version INTEGER NOT NULL,
                        synced_at INTEGER,
                        created_at INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_event_entity_type_entity_id ON sync_event(entity_type, entity_id)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_event_version ON sync_event(version)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_sync_event_synced_at ON sync_event(synced_at)")

                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS sync_state (
                        id INTEGER NOT NULL PRIMARY KEY,
                        last_pulled_version INTEGER NOT NULL,
                        last_pushed_version INTEGER NOT NULL,
                        last_sync_at INTEGER,
                        token_expires_at INTEGER
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    INSERT OR IGNORE INTO sync_state(id, last_pulled_version, last_pushed_version, last_sync_at, token_expires_at)
                    VALUES(1, 0, 0, NULL, NULL)
                    """.trimIndent()
                )
            }
        }
    }
}
