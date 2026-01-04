package com.example.memoryhelper.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.memoryhelper.data.local.entity.ReviewCurve
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for ReviewCurve entity.
 */
@Dao
interface ReviewCurveDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(curve: ReviewCurve): Long

    @Update
    suspend fun update(curve: ReviewCurve)

    @Delete
    suspend fun delete(curve: ReviewCurve)

    @Query("SELECT * FROM review_curves WHERE id = :id")
    suspend fun getById(id: Long): ReviewCurve?

    @Query("SELECT * FROM review_curves ORDER BY name ASC")
    fun getAllCurvesFlow(): Flow<List<ReviewCurve>>

    @Query("SELECT * FROM review_curves ORDER BY name ASC")
    suspend fun getAllCurves(): List<ReviewCurve>

    @Query("SELECT * FROM review_curves WHERE is_default = 1 LIMIT 1")
    suspend fun getDefaultCurve(): ReviewCurve?

    @Query("UPDATE review_curves SET is_default = 0")
    suspend fun clearDefaultCurves()

    @Query("UPDATE review_curves SET is_default = 1 WHERE id = :curveId")
    suspend fun setDefaultCurve(curveId: Long)
}
