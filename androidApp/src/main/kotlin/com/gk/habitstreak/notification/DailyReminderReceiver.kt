package com.gk.habitstreak.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DailyReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "🔥 HabitStreak Check-in"
        val message = intent.getStringExtra("message") ?: "Time to review your habits today and keep your streak alive!"
        NotificationHelper.sendHabitReminder(context, title, message)
    }
}
