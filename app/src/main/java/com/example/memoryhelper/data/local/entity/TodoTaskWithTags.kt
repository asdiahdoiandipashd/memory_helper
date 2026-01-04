package com.example.memoryhelper.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class TodoTaskWithTags(
    @Embedded
    val task: TodoTask,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TodoTaskTagCrossRef::class,
            parentColumn = "task_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TodoTag>
)
