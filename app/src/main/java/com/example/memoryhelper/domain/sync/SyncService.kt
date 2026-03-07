package com.example.memoryhelper.domain.sync

interface SyncService {
    suspend fun pushPull(): SyncReport
}
