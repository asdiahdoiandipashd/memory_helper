package com.example.memoryhelper.data.local.entity

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class MemoryItemWithTags(
    @Embedded
    val item: MemoryItem,
    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = MemoryItemTagCrossRef::class,
            parentColumn = "memory_item_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<MemoryTag>
)
