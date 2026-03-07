package com.example.memoryhelper.data.sync

import com.example.memoryhelper.data.local.dao.SyncEventDao
import com.example.memoryhelper.data.local.dao.SyncStateDao
import com.example.memoryhelper.data.local.entity.SyncState
import com.example.memoryhelper.domain.sync.SyncReport
import com.example.memoryhelper.domain.sync.SyncService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalFirstSyncService @Inject constructor(
    private val syncEventDao: SyncEventDao,
    private val syncStateDao: SyncStateDao
) : SyncService {

    override suspend fun pushPull(): SyncReport {
        val pending = syncEventDao.getPendingEvents()
        val now = System.currentTimeMillis()
        pending.forEach { event ->
            syncEventDao.update(event.copy(syncedAt = now))
        }

        val current = syncStateDao.getState() ?: SyncState()
        syncStateDao.upsert(
            current.copy(
                lastPushedVersion = current.lastPushedVersion + pending.size,
                lastPulledVersion = current.lastPulledVersion,
                lastSyncAt = now
            )
        )

        return SyncReport(
            pushed = pending.size,
            pulled = 0,
            conflicts = emptyList(),
            errors = emptyList()
        )
    }
}
