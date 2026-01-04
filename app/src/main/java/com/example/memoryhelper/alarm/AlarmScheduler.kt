package com.example.memoryhelper.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AlarmScheduler manages the scheduling of review notifications.
 *
 * Strategy: Only schedule ONE system alarm at a time (the nearest future next_review_time).
 * When the alarm fires -> Show notification -> Query DB again -> Schedule next alarm.
 */
@Singleton
class AlarmScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val memoryItemDao: MemoryItemDao
) {
    companion object {
        private const val TAG = "AlarmScheduler"
        private const val ALARM_REQUEST_CODE = 1001
        const val EXTRA_ITEM_ID = "extra_item_id"
    }

    private val alarmManager: AlarmManager? =
        context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    /**
     * Schedules the next alarm for the nearest review item.
     * Should be called after any item update (add, remember, forgot).
     */
    suspend fun scheduleNextAlarm() {
        val currentTime = System.currentTimeMillis()
        val nextAlarmTime = memoryItemDao.getNextAlarmTime(currentTime)

        if (nextAlarmTime == null) {
            Log.d(TAG, "No pending review items, canceling any existing alarm")
            cancelAlarm()
            return
        }

        Log.d(TAG, "Scheduling next alarm for time: $nextAlarmTime (in ${(nextAlarmTime - currentTime) / 1000}s)")

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(EXTRA_ITEM_ID, -1L) // We'll query due items in the receiver
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Android 12+ requires SCHEDULE_EXACT_ALARM permission
                if (alarmManager?.canScheduleExactAlarms() == true) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                    Log.d(TAG, "Exact alarm scheduled successfully")
                } else {
                    // Fallback to inexact alarm if permission not granted
                    alarmManager?.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        nextAlarmTime,
                        pendingIntent
                    )
                    Log.w(TAG, "Exact alarm permission not granted, using inexact alarm")
                }
            } else {
                // Pre-Android 12
                alarmManager?.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
                Log.d(TAG, "Exact alarm scheduled successfully")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException when scheduling exact alarm: ${e.message}")
            // Fallback to inexact alarm
            try {
                alarmManager?.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    nextAlarmTime,
                    pendingIntent
                )
                Log.w(TAG, "Fallback to inexact alarm due to security exception")
            } catch (e2: Exception) {
                Log.e(TAG, "Failed to schedule any alarm: ${e2.message}")
            }
        }
    }

    /**
     * Cancels the current scheduled alarm.
     */
    fun cancelAlarm() {
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager?.cancel(pendingIntent)
        Log.d(TAG, "Alarm canceled")
    }
}
