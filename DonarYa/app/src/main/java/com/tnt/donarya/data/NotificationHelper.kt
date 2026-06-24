package com.tnt.donarya.data

import android.Manifest
import com.tnt.donarya.R
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.tnt.donarya.MainActivity

data class NotifPayload(
    val id: String,
    val title: String,
    val message: String,
    val relatedNeedId: String = ""
)

object NotificationHelper {
    private const val CHANNEL_ID = "donarya_notificaciones"
    private const val PREFS_NAME = "notifications_shown"
    private const val MAX_DISPLAY = 5
    const val EXTRA_NEED_ID = "need_id"

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
            val manager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun show(context: Context, payload: NotifPayload) {
        if (wasShown(payload.id)) return
        markShown(payload.id)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (android.content.pm.PackageManager.PERMISSION_DENIED ==
                context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            ) return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_NEED_ID, payload.relatedNeedId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            payload.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_heart)
            .setContentTitle(payload.title)
            .setContentText(payload.message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(payload.message))
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(payload.id.hashCode(), notification)
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showMultiple(context: Context, notifications: List<NotifPayload>) {
        notifications.take(MAX_DISPLAY).forEach { payload ->
            show(context, payload)
        }
        val remaining = notifications.size - MAX_DISPLAY
        if (remaining > 0) {
            val summary = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_heart)
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
