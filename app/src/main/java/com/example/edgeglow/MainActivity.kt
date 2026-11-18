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

        // Inisialisasi Tombol & View
        val btnOverlay = findViewById<Button>(R.id.btnOverlay)
        val btnNotifAccess = findViewById<Button>(R.id.btnNotifAccess)
        val btnTest = findViewById<Button>(R.id.btnTest)
        val btnTestFullScreen = findViewById<Button>(R.id.btnTestFullScreen)
        
        // Perhatikan ID ini harus sama dengan yang ada di activity_main.xml
        val previewGlowView = findViewById<EdgeGlowView>(R.id.previewGlowView) 

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
        }

        // 3. Tombol Tes Preview (Di dalam aplikasi)
        btnTest.setOnClickListener {
            try {
                previewGlowView.startGlow(Color.CYAN, 3000)
                Toast.makeText(this, "Memutar animasi preview...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error Preview: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        // 4. Tombol Tes Overlay Full Screen (Sistem)
        btnTestFullScreen.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "GAGAL: Izinkan Overlay dulu di Tombol 1!", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            try {
                val intent = Intent(this, EdgeGlowService::class.java).apply {
                    putExtra(EdgeGlowService.EXTRA_COLOR, Color.CYAN)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent)
                } else {
                    startService(intent)
                }
                Toast.makeText(this, "Menjalankan Overlay layar penuh...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "Error Service: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
