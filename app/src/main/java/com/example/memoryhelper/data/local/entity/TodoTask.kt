package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "todo_tasks",
    indices = [
        Index(value = ["is_daily"]),
        Index(value = ["is_completed"]),
        Index(value = ["updated_at"])
    ]
)
data class TodoTask(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "note")
    val note: String = "",

    @ColumnInfo(name = "is_daily")
    val isDaily: Boolean = false,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "last_completed_day")
    val lastCompletedDay: Long? = null,

    @ColumnInfo(name = "last_completed_at")
    val lastCompletedAt: Long? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
)
