package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sync_event",
    indices = [
        Index(value = ["entity_type", "entity_id"]),
        Index(value = ["version"]),
        Index(value = ["synced_at"])
    ]
)
data class SyncEvent(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "entity_type")
    val entityType: String,
    @ColumnInfo(name = "entity_id")
    val entityId: Long,
    @ColumnInfo(name = "action")
    val action: String,
    @ColumnInfo(name = "payload_json")
    val payloadJson: String,
    @ColumnInfo(name = "version")
    val version: Long = 0L,
    @ColumnInfo(name = "synced_at")
    val syncedAt: Long? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
