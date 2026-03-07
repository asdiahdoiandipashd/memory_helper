package com.example.memoryhelper.domain.scheduler

import com.example.memoryhelper.data.local.entity.ReviewGrade

enum class ReviewGradeOption(val dbValue: Int) {
    AGAIN(ReviewGrade.AGAIN),
    HARD(ReviewGrade.HARD),
    GOOD(ReviewGrade.GOOD),
    EASY(ReviewGrade.EASY);

    companion object {
        fun fromDb(value: Int): ReviewGradeOption {
            return entries.firstOrNull { it.dbValue == value } ?: GOOD
        }
    }
}
