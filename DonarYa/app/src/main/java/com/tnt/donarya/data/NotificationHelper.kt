package com.tnt.donarya.data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    private const val CHANNEL_ID = "donarya_notificaciones"
    private const val PREFS_NAME = "notifications_shown"
    private const val MAX_DISPLAY = 5

    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        createChannel(context)
    }

    private fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "DonarYa",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notificaciones de la aplicación"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun show(context: Context, id: String, title: String, message: String) {
        if (wasShown(id)) return
        markShown(id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (android.content.pm.PackageManager.PERMISSION_DENIED ==
                context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
            ) return
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(id.hashCode(), notification)
    }

    fun showMultiple(context: Context, notifications: List<Triple<String, String, String>>) {
        notifications.take(MAX_DISPLAY).forEach { (id, title, message) ->
            show(context, id, title, message)
        }
        val remaining = notifications.size - MAX_DISPLAY
        if (remaining > 0) {
            val summary = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("DonarYa")
                .setContentText("$remaining notificaciones más")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .build()
            NotificationManagerCompat.from(context).notify(-1, summary)
        }
    }

    private fun wasShown(id: String): Boolean {
        return prefs?.getBoolean(id, false) ?: false
    }

    private fun markShown(id: String) {
        prefs?.edit()?.putBoolean(id, true)?.apply()
    }
}
