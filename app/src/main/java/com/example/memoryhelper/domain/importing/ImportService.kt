package com.example.memoryhelper.domain.importing

import java.io.InputStream

interface ImportService {
    suspend fun importCsv(input: InputStream): ImportReport
    suspend fun importAnki(input: InputStream): ImportReport
}
