package com.edamametech.android.coffeedriptimer.notifier

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.edamametech.android.coffeedriptimer.MainActivity
import com.edamametech.android.coffeedriptimer.R

class TimerNotification(val context: Context?) {
    private val notificationManager = context?.getSystemService(NotificationManager::class.java)

    @SuppressLint("MissingPermission")
    fun showTimerNotification(message: String) {
        if (context != null) {

            val builder = NotificationCompat.Builder(context, MainActivity.TIMER_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            NotificationManagerCompat.from(context).notify(notificationId, builder.build())
        }
    }

    companion object {
        const val notificationId = 1
        const val intentId = 99
    }
}