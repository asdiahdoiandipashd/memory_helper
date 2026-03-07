package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "memory_item_tag_cross_ref",
    primaryKeys = ["memory_item_id", "tag_id"],
    foreignKeys = [
        ForeignKey(
            entity = MemoryItem::class,
            parentColumns = ["id"],
            childColumns = ["memory_item_id"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = MemoryTag::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["memory_item_id"]),
        Index(value = ["tag_id"])
    ]
)
data class MemoryItemTagCrossRef(
    @ColumnInfo(name = "memory_item_id")
    val memoryItemId: Long,
    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
