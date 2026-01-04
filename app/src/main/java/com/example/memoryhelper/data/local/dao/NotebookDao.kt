package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.memoryhelper.data.local.entity.Notebook
import kotlinx.coroutines.flow.Flow

@Dao
interface NotebookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(notebook: Notebook): Long

    @androidx.room.Update
    suspend fun update(notebook: Notebook)

    @androidx.room.Delete
    suspend fun delete(notebook: Notebook)

    @Query("SELECT * FROM notebooks ORDER BY id ASC")
    fun getAllNotebooksFlow(): Flow<List<Notebook>>

    @Query("SELECT * FROM notebooks ORDER BY id ASC")
    suspend fun getAllNotebooks(): List<Notebook>

    @Query("SELECT * FROM notebooks WHERE id = :id")
    suspend fun getById(id: Long): Notebook?
}
