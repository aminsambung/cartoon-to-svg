package com.example.edgeglow

import android.content.Intent
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

        findViewById<Button>(R.id.btnOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                ))
            } else {
                Toast.makeText(this, "Izin overlay sudah aktif", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<Button>(R.id.btnNotifAccess).setOnClickListener {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        findViewById<Button>(R.id.btnTest).setOnClickListener {
            startService(Intent(this, EdgeGlowService::class.java)
                .putExtra("color", android.graphics.Color.CYAN))
        }
    }
}
