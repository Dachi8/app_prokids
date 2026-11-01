package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R

object NotificationHelper {
    const val CHANNEL_ID = "serena_wellness_channel"
    private const val CHANNEL_NAME = "Recordatorios de Bienestar"
    private const val CHANNEL_DESC = "Recordatorios diarios para registrar tu ánimo, respirar y cuidar de ti"
    private const val NOTIFICATION_ID = 1001
    private const val ALARM_REQUEST_CODE = 2002

    private const val PREFS_NAME = "serena_notification_prefs"
    private const val KEY_PERIODIC_ACTIVE = "periodic_active"
    private const val KEY_INTERVAL_HOURS = "interval_hours"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = CHANNEL_DESC
                enableVibration(true)
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showWellnessReminder(
        context: Context,
        title: String = "Serena te acompaña 🌱",
        message: String = "Tómate 2 minutos para respirar y anotar cómo te sientes hoy."
    ): Boolean {
        createNotificationChannel(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        return try {
            with(NotificationManagerCompat.from(context)) {
                notify(NOTIFICATION_ID, builder.build())
            }
            true
        } catch (_: SecurityException) {
            false
        }
    }

    fun schedulePeriodicReminders(context: Context, intervalHours: Int): Boolean {
        createNotificationChannel(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return false

        val intent = Intent(context, WellnessReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalHours.toLong() * 60 * 60 * 1000
        val firstTriggerMillis = SystemClock.elapsedRealtime() + intervalMillis

        alarmManager.setInexactRepeating(
            AlarmManager.ELAPSED_REALTIME_WAKEUP,
            firstTriggerMillis,
            intervalMillis,
            pendingIntent
        )

        // Save preference
        getPrefs(context).edit()
            .putBoolean(KEY_PERIODIC_ACTIVE, true)
            .putInt(KEY_INTERVAL_HOURS, intervalHours)
            .apply()

        // Also trigger an immediate welcome notification confirmation
        showWellnessReminder(
            context = context,
            title = "Recordatorios programados 🌱",
            message = "Serena te enviará una pausa de bienestar cada $intervalHours hora${if (intervalHours > 1) "s" else ""}."
        )

        return true
    }

    fun cancelPeriodicReminders(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, WellnessReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)

        getPrefs(context).edit()
            .putBoolean(KEY_PERIODIC_ACTIVE, false)
            .apply()
    }

    fun isPeriodicReminderActive(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PERIODIC_ACTIVE, false)
    }

    fun getSavedIntervalHours(context: Context): Int {
        return getPrefs(context).getInt(KEY_INTERVAL_HOURS, 4)
    }

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
}
