package com.example.memoryhelper.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BootReceiver reschedules alarms when the device is rebooted.
 *
 * Android clears all alarms on reboot, so we need to re-register them.
 * This receiver listens for BOOT_COMPLETED and reschedules the next review alarm.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    companion object {
        private const val TAG = "BootReceiver"
    }

    // Create a coroutine scope for async operations in BroadcastReceiver
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "Device boot completed - rescheduling alarms")

            // Use goAsync() for asynchronous operations
            val pendingResult = goAsync()

            scope.launch {
                try {
                    alarmScheduler.scheduleNextAlarm()
                    Log.d(TAG, "Alarm rescheduled successfully after boot")
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alarm after boot: ${e.message}")
                } finally {
                    // Signal that we're done
                    pendingResult.finish()
                }
            }
        }
    }
}
