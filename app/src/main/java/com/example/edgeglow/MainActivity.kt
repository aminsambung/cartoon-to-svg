package com.example.edgeglow

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnNotifAccess = findViewById<Button>(R.id.btnNotifAccess)
        val btnTest = findViewById<Button>(R.id.btnTest)

        // 1. Tombol Izin Overlay
        btnOverlay.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                Toast.makeText(this, "Izinkan 'Display over other apps'", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Izin overlay sudah aktif", Toast.LENGTH_SHORT).show()
            }
        }

        // 2. Tombol Izin Notifikasi
        btnNotifAccess.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            Toast.makeText(this, "Cari 'EdgeGlow' dan aktifkan", Toast.LENGTH_LONG).show()
        }

        // 3. Tombol Tes Efek Cahaya
        btnTest.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "GAGAL: Kamu belum memberi izin Overlay! (Klik tombol 1)", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                val intent = Intent(this, EdgeGlowService::class.java).apply {
                    putExtra(EdgeGlowService.EXTRA_COLOR, Color.RED)
                }
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                
                Toast.makeText(this, "Menjalankan efek cahaya...", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
