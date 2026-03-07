package com.example.memoryhelper.domain.sync

data class SyncConflict(
    val entityType: String,
    val entityId: Long,
    val conflictCopy: String
)

data class SyncReport(
    val pushed: Int,
    val pulled: Int,
    val conflicts: List<SyncConflict>,
    val errors: List<String>
)
