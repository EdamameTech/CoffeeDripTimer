package com.edamametech.android.coffeedriptimer

import CoffeeDripTimerScreen
import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.edamametech.android.coffeedriptimer.ui.theme.CoffeeDripTimerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.timer_channel_name)
            val descriptionText = getString(R.string.timer_channel_description)
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(TIMER_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        val accessNotificationPermission =
            checkSelfPermission(Manifest.permission.ACCESS_NOTIFICATION_POLICY) == PackageManager.PERMISSION_GRANTED
        val postNotificationPermission =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        if (!accessNotificationPermission || !postNotificationPermission) {
            val request = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                arrayOf(
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY,
                    Manifest.permission.POST_NOTIFICATIONS
                )
            } else {
                arrayOf(
                    Manifest.permission.ACCESS_NOTIFICATION_POLICY
                )
            }
            ActivityCompat.requestPermissions(this, request, REQUEST_PERMISSION_CODE)
        }

        setContent {
            CoffeeDripTimerTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    Surface(
                        modifier = Modifier.safeDrawingPadding().fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        CoffeeDripTimerScreen()
                    }
                }
            }
        }
    }

    companion object {
        const val TIMER_CHANNEL_ID = "DRIPPER_TIMER"
        const val REQUEST_PERMISSION_CODE = 42
    }
}
