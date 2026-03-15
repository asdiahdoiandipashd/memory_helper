package com.example.memoryhelper.domain.scheduler

import com.example.memoryhelper.data.local.entity.MemoryItem
import com.example.memoryhelper.data.local.entity.MemoryItemStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DefaultSchedulerEngineTest {

    private val engine = DefaultSchedulerEngine()

    @Test
    fun again_resets_to_first_stage_and_first_interval() {
        val now = 1_000_000L
        val item = testItem(stageIndex = 2)

        val result = engine.grade(
            item = item,
            intervalsMinutes = listOf(5L, 30L, 120L),
            grade = ReviewGradeOption.AGAIN,
            now = now
        )

        assertEquals(0, result.newStageIndex)
        assertEquals(now + 5L * 60_000L, result.nextReviewTime)
        assertTrue(!result.completed)
    }

    @Test
    fun hard_uses_next_stage_with_multiplier() {
        val now = 2_000_000L
        val item = testItem(stageIndex = 0)

        val result = engine.grade(
            item = item,
            intervalsMinutes = listOf(10L, 30L),
            grade = ReviewGradeOption.HARD,
            now = now
        )

        assertEquals(1, result.newStageIndex)
        assertEquals(now + 36L * 60_000L, result.nextReviewTime)
        assertTrue(!result.completed)
    }

    @Test
    fun good_on_last_stage_marks_item_completed() {
        val item = testItem(stageIndex = 2)

        val result = engine.grade(
            item = item,
            intervalsMinutes = listOf(5L, 30L, 120L),
            grade = ReviewGradeOption.GOOD,
            now = 3_000_000L
        )

        assertTrue(result.completed)
        assertEquals(Long.MAX_VALUE, result.nextReviewTime)
        assertEquals(2, result.newStageIndex)
    }

    private fun testItem(stageIndex: Int): MemoryItem {
        return MemoryItem(
            id = 1L,
            notebookId = 1L,
            title = "Item",
            content = "Content",
            status = MemoryItemStatus.REVIEWING,
            stageIndex = stageIndex,
            nextReviewTime = 0L,
            lastReviewTime = 0L,
            createdAt = 0L,
            updatedAt = 0L
        )
    }
}
