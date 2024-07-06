package com.edamametech.android.coffeedriptimer.notifier

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import com.edamametech.android.coffeedriptimer.notifier.TimerReceiver.Companion.MESSAGE

fun ScheduleTimerNotification(context: Context, notifyAt: Long, requestCode: Int, message: String) {
    val pendingIntent = PendingIntent.getBroadcast(
        context.applicationContext,
        requestCode,
        Intent(context.applicationContext, TimerReceiver::class.java).putExtra(MESSAGE, message),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    try {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            notifyAt,
            pendingIntent
        )
    } catch (e: SecurityException) {
        Log.i("ScheduleNotification", "SecurityException")
    }
}

fun CancelTimerNotification(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.cancelAll()
}
