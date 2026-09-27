package com.gk.habitstreak

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import com.gk.habitstreak.notification.NotificationHelper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        NotificationHelper.initNotificationChannel(this)
        NotificationHelper.scheduleDailyEveningReminder(this)

        setContent {
            val context = LocalContext.current

            var pendingNotificationPayload: Pair<String, String>? = null

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    NotificationHelper.scheduleDailyEveningReminder(context)
                    val payload = pendingNotificationPayload
                    if (payload != null) {
                        NotificationHelper.sendHabitReminder(context, payload.first, payload.second)
                    } else {
                        NotificationHelper.sendHabitReminder(
                            context,
                            "🔥 HabitStreak Alert",
                            "Notifications enabled! We'll keep your streak safe."
                        )
                    }
                }
            }

            App(
                onTriggerReminder = { title, message ->
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED
                        ) {
                            NotificationHelper.sendHabitReminder(context, title, message)
                        } else {
                            pendingNotificationPayload = Pair(title, message)
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    } else {
                        NotificationHelper.sendHabitReminder(context, title, message)
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}