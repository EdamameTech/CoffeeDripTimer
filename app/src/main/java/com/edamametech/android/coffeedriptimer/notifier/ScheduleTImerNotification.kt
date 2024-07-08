package com.edamametech.android.coffeedriptimer.notifier

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.edamametech.android.coffeedriptimer.notifier.TimerReceiver.Companion.MESSAGE

fun pendingIntentForTimerNotification(context: Context, requestCode: Int, message: String = ""): PendingIntent {
    return PendingIntent.getBroadcast(
        context.applicationContext,
        requestCode,
        Intent(context.applicationContext, TimerReceiver::class.java).putExtra(MESSAGE, message),
        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_CANCEL_CURRENT
    )
}

fun scheduleTimerNotification(context: Context, notifyAt: Long, requestCode: Int, message: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    alarmManager.setExactAndAllowWhileIdle(
        AlarmManager.RTC_WAKEUP,
        notifyAt,
        pendingIntentForTimerNotification(context, requestCode, message)
    )
}

fun cancelTimerNotification(context: Context, minId: Int, maxId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        alarmManager.cancelAll()
    } else {
        for (id in minId..maxId) {
            alarmManager.cancel(pendingIntentForTimerNotification(context, id))
        }
    }
}
