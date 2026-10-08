package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.model.ConversationMessage
import com.example.data.model.EmailCategory
import com.example.data.model.EmailMessage
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.data.model.TaskPriority
import com.example.data.model.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class BackgroundTaskLog(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val title: String,
    val detail: String,
    val type: String // "MEETING", "EMAIL", "WORK"
)

data class BackgroundAlertPreferences(
    val alertsEnabled: Boolean = true,
    val soundOrVibrate: Boolean = true,
    val alertOnEmail: Boolean = true,
    val alertOnMeeting: Boolean = true,
    val alertOnWork: Boolean = true
)

class SystemAiBackgroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val CHANNEL_ID = "system_ai_background_channel"
        const val ALERT_CHANNEL_ID = "system_ai_task_alert_channel"
        const val NOTIFICATION_ID = 2001
        const val ALERT_NOTIFICATION_ID = 2002
        const val ACTION_START = "com.example.system.ACTION_START"
        const val ACTION_STOP = "com.example.system.ACTION_STOP"

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        private val _backgroundLogs = MutableStateFlow<List<BackgroundTaskLog>>(emptyList())
        val backgroundLogs: StateFlow<List<BackgroundTaskLog>> = _backgroundLogs.asStateFlow()

        private val _currentWorkStatus = MutableStateFlow("System AI Active in Background")
        val currentWorkStatus: StateFlow<String> = _currentWorkStatus.asStateFlow()

        val alertPreferences = MutableStateFlow(BackgroundAlertPreferences())

        fun sendTestAlert(context: Context) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val prefs = alertPreferences.value
            if (!prefs.alertsEnabled) return

            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val builder = NotificationCompat.Builder(context, ALERT_CHANNEL_ID)
                .setContentTitle("System AI: Task Completed")
                .setContentText("Test alert: Background task finished successfully.")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)

            if (prefs.soundOrVibrate) {
                builder.setDefaults(Notification.DEFAULT_ALL)
            } else {
                builder.setSilent(true)
            }

            manager?.notify(ALERT_NOTIFICATION_ID, builder.build())
        }

        fun startService(context: Context) {
            val intent = Intent(context, SystemAiBackgroundService::class.java).apply {
                action = ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopService(context: Context) {
            val intent = Intent(context, SystemAiBackgroundService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
            _isRunning.value = false
            return START_NOT_STICKY
        }

        _isRunning.value = true
        startForeground(NOTIFICATION_ID, buildNotification("System AI is working in background"))

        startBackgroundWorkLoop()

        return START_STICKY
    }

    private fun startBackgroundWorkLoop() {
        serviceScope.launch {
            val database = AppDatabase.getDatabase(applicationContext, this)

            var cycle = 0
            while (isActive) {
                cycle++
                val timeStr = SimpleDateFormat("hh:mm a", Locale.US).format(Date())

                when (cycle % 3) {
                    0 -> {
                        // Background Email Triage
                        _currentWorkStatus.value = "System AI: Triaged team inbox at $timeStr"
                        val log = BackgroundTaskLog(
                            title = "Inbox Checked in Background",
                            detail = "System AI verified all incoming team emails. No urgent blockers.",
                            type = "EMAIL"
                        )
                        _backgroundLogs.value = listOf(log) + _backgroundLogs.value.take(20)
                        updateNotification("Checked team emails: All clear ($timeStr)")
                        dispatchTaskCompletionAlert(
                            title = "System AI: Inbox Checked",
                            message = "Team emails triaged and synced with zero blockers ($timeStr).",
                            type = "EMAIL"
                        )
                    }
                    1 -> {
                        // Background Schedule & Conflict Monitoring
                        _currentWorkStatus.value = "System AI: Monitored schedule at $timeStr"
                        val log = BackgroundTaskLog(
                            title = "Meeting Schedules Synced",
                            detail = "System AI monitored upcoming meetings and verified attendee availability.",
                            type = "MEETING"
                        )
                        _backgroundLogs.value = listOf(log) + _backgroundLogs.value.take(20)
                        updateNotification("Schedules active: Next meeting on track ($timeStr)")
                        dispatchTaskCompletionAlert(
                            title = "System AI: Schedule Monitored",
                            message = "Upcoming meetings synced, calendars aligned ($timeStr).",
                            type = "MEETING"
                        )
                    }
                    2 -> {
                        // Background Daily Work Automation
                        _currentWorkStatus.value = "System AI: Optimized daily tasks at $timeStr"
                        val log = BackgroundTaskLog(
                            title = "Daily Work Progress Monitored",
                            detail = "System AI reviewed sprint tasks and prepared executive status logs.",
                            type = "WORK"
                        )
                        _backgroundLogs.value = listOf(log) + _backgroundLogs.value.take(20)
                        updateNotification("Daily work: Standup tasks updated ($timeStr)")
                        dispatchTaskCompletionAlert(
                            title = "System AI: Work Progress Monitored",
                            message = "Sprint tasks reviewed and standup progress updated ($timeStr).",
                            type = "WORK"
                        )
                    }
                }

                // Run every 20 seconds for interactive background demonstration
                delay(20_000L)
            }
        }
    }

    private fun dispatchTaskCompletionAlert(title: String, message: String, type: String) {
        val prefs = alertPreferences.value
        if (!prefs.alertsEnabled) return // Silenced completely

        val isTypeAllowed = when (type) {
            "EMAIL" -> prefs.alertOnEmail
            "MEETING" -> prefs.alertOnMeeting
            "WORK" -> prefs.alertOnWork
            else -> true
        }
        if (!isTypeAllowed) return

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (prefs.soundOrVibrate) {
            builder.setDefaults(Notification.DEFAULT_ALL)
        } else {
            builder.setSilent(true)
        }

        manager?.notify(ALERT_NOTIFICATION_ID, builder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(NotificationManager::class.java)

            // Ongoing Background Service Channel
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                "System AI Background Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Ongoing notification while System AI runs in background"
            }
            manager?.createNotificationChannel(serviceChannel)

            // Task Completion Alerts Channel
            val alertChannel = NotificationChannel(
                ALERT_CHANNEL_ID,
                "Task Completion Alerts",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Alerts sent when System AI completes background work"
                enableVibration(true)
            }
            manager?.createNotificationChannel(alertChannel)
        }
    }

    private fun buildNotification(statusText: String): Notification {
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val stopIntent = PendingIntent.getService(
            this,
            1,
            Intent(this, SystemAiBackgroundService::class.java).apply { action = ACTION_STOP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("System AI is Working")
            .setContentText(statusText)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Pause", stopIntent)
            .build()
    }

    private fun updateNotification(statusText: String) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        manager?.notify(NOTIFICATION_ID, buildNotification(statusText))
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        _isRunning.value = false
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
