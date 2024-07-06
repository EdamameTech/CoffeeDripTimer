package com.edamametech.android.coffeedriptimer.notifier

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TimerReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val scheduleNotificationService = TimerNotification(context)
        val message: String = intent?.getStringExtra(MESSAGE) ?: ""
        scheduleNotificationService.showTimerNotification(message)
    }

    companion object {
        const val MESSAGE = "MESSAGE"
    }
}