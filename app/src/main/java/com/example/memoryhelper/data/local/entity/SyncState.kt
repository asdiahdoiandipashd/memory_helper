package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_state")
data class SyncState(
    @PrimaryKey
    val id: Int = 1,
    @ColumnInfo(name = "last_pulled_version")
    val lastPulledVersion: Long = 0L,
    @ColumnInfo(name = "last_pushed_version")
    val lastPushedVersion: Long = 0L,
    @ColumnInfo(name = "last_sync_at")
    val lastSyncAt: Long? = null,
    @ColumnInfo(name = "token_expires_at")
    val tokenExpiresAt: Long? = null
)
