package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "todo_task_tag_cross_ref",
    primaryKeys = ["task_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = TodoTask::class,
            parentColumns = ["id"],
            childColumns = ["task_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TodoTag::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["task_id"]),
        Index(value = ["tag_id"])
    ]
)
data class TodoTaskTagCrossRef(
    @ColumnInfo(name = "task_id")
    val taskId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
