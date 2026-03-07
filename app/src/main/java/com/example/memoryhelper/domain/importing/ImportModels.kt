package com.example.memoryhelper.domain.importing

data class ImportError(
    val line: Int,
    val message: String
)

data class ImportReport(
    val imported: Int,
    val skipped: Int,
    val failed: Int,
    val errors: List<ImportError>
)
