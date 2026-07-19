package com.example.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.R

object NotificationHelper {
    private const val CHANNEL_ID_ALERTS = "onsite_urgent_alerts"
    private const val CHANNEL_NAME_ALERTS = "Urgent Alerts"
    private const val CHANNEL_DESC_ALERTS = "Notifications for urgent manager announcements"

    private const val CHANNEL_ID_CHECKINS = "onsite_checkin_reminders"
    private const val CHANNEL_NAME_CHECKINS = "Check-In Reminders"
    private const val CHANNEL_DESC_CHECKINS = "Notifications for missed shifts and check-ins"

    fun initNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val alertsChannel = NotificationChannel(
                CHANNEL_ID_ALERTS,
                CHANNEL_NAME_ALERTS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_ALERTS
            }

            val checkinsChannel = NotificationChannel(
                CHANNEL_ID_CHECKINS,
                CHANNEL_NAME_CHECKINS,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_CHECKINS
            }

            manager.createNotificationChannel(alertsChannel)
            manager.createNotificationChannel(checkinsChannel)
        }
    }

    fun postUrgentAlertNotification(context: Context, title: String, content: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Use R.mipmap.ic_launcher as small icon
        val builder = NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("🚨 URGENT: $title")
            .setContentText(content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    fun postMissedCheckinNotification(context: Context, employeeName: String, shiftName: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val builder = NotificationCompat.Builder(context, CHANNEL_ID_CHECKINS)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("⚠️ Missed Check-In Alert")
            .setContentText("Hello $employeeName, you have a missed check-in for the $shiftName shift. Please report to a geofenced on-site zone immediately.")
            .setStyle(NotificationCompat.BigTextStyle().bigText("Hello $employeeName, you missed your check-in window for the $shiftName shift. Check-in is required on-site immediately."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
}
