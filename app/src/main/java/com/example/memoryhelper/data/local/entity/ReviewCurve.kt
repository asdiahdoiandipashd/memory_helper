package com.example.memoryhelper.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ReviewCurve entity - stores custom review interval strategies.
 * Based on the Ebbinghaus Forgetting Curve.
 */
@Entity(tableName = "review_curves")
data class ReviewCurve(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    /**
     * JSON string representing List<Long> of intervals in minutes.
     * Example: [5, 30, 720, 1440, 2880] for 5min, 30min, 12h, 1day, 2days
     */
    @ColumnInfo(name = "intervals_json")
    val intervalsJson: String,

    @ColumnInfo(name = "is_default")
    val isDefault: Boolean = false
)
