package com.example.edgeglow

import android.content.Intent
import android.graphics.Color
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

class NotificationListener : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        // Abaikan notifikasi dari aplikasi sendiri
        if (sbn.packageName == packageName) return

        // Tentukan warna berdasarkan aplikasi
        val color = when (sbn.packageName) {
            "com.whatsapp" -> Color.parseColor("#25D366")
            "com.instagram.android" -> Color.parseColor("#E1306C")
            "com.google.android.gm" -> Color.parseColor("#EA4335")
            else -> Color.CYAN
        }

        val intent = Intent(this, EdgeGlowService::class.java).apply {
            putExtra("color", color)
        }
        startService(intent)
    }
}
