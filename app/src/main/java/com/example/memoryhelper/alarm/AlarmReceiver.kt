package com.example.memoryhelper.alarm

import android.Manifest
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.memoryhelper.MainActivity
import com.example.memoryhelper.MemoryHelperApplication
import com.example.memoryhelper.R
import com.example.memoryhelper.data.local.dao.MemoryItemDao
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles alarm triggers for review notifications.
 *
 * When the alarm fires:
 * 1. Acquire a WakeLock to prevent the device from sleeping
 * 2. Show a system notification to remind the user
 * 3. Schedule the next alarm to keep the cycle going
 * 4. Release the WakeLock
 */
@AndroidEntryPoint
class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "AlarmReceiver"
        private const val NOTIFICATION_ID = 1001
        private const val WAKELOCK_TAG = "MemoryHelper:AlarmWakeLock"
        private const val WAKELOCK_TIMEOUT_MS = 10_000L // 10 seconds
    }

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    @Inject
    lateinit var memoryItemDao: MemoryItemDao

    // Create a coroutine scope for the receiver
    private val receiverScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Alarm received!")

        // Acquire WakeLock IMMEDIATELY to prevent the device from sleeping
        // before we can show the notification
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            WAKELOCK_TAG
        ).apply {
            // Acquire with timeout to prevent accidental battery drain
            acquire(WAKELOCK_TIMEOUT_MS)
        }
        Log.d(TAG, "WakeLock acquired")

        val pendingResult = goAsync()

        receiverScope.launch {
            try {
                // Get the count of due items
                val currentTime = System.currentTimeMillis()
                val dueCount = memoryItemDao.countDueItems(currentTime)

                Log.d(TAG, "Due items count: $dueCount")

                // Show notification if there are due items
                if (dueCount > 0) {
                    showNotification(context, dueCount)
                }

                // Schedule the next alarm to keep the cycle going
                alarmScheduler.scheduleNextAlarm()

            } catch (e: Exception) {
                Log.e(TAG, "Error processing alarm: ${e.message}", e)
            } finally {
                // Release WakeLock after notification is posted
                if (wakeLock.isHeld) {
                    wakeLock.release()
                    Log.d(TAG, "WakeLock released")
                }
                pendingResult.finish()
            }
        }
    }

    /**
     * Shows a notification to remind the user about pending review items.
     */
    private fun showNotification(context: Context, dueCount: Int) {
        // Check notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted, skipping notification")
                return
            }
        }

        // Create a PendingIntent that opens MainActivity when clicked
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notificationText = if (dueCount == 1) {
            context.getString(R.string.notification_one_item)
        } else {
            context.getString(R.string.notification_multiple_items, dueCount)
        }

        val notification = NotificationCompat.Builder(
            context,
            MemoryHelperApplication.REVIEW_CHANNEL_ID
        )
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(context.getString(R.string.notification_title))
            .setContentText(notificationText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .build()

        // Show the notification
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)

        Log.i(TAG, "Notification shown for $dueCount due item(s)")
    }
}
