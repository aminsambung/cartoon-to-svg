package com.example.edgeglow

import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager

class EdgeGlowService : Service() {

    private lateinit var windowManager: WindowManager
    private var glowView: EdgeGlowView? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val color = intent?.getIntExtra("color", Color.CYAN) ?: Color.CYAN
        showGlow(color)
        return START_NOT_STICKY
    }

    private fun showGlow(color: Int) {
        if (glowView != null) {
            glowView?.startGlow(color)
            return
        }

        glowView = EdgeGlowView(this)

        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        else
            WindowManager.LayoutParams.TYPE_PHONE

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                    or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }

        windowManager.addView(glowView, params)
        glowView?.startGlow(color)

        // Auto stop setelah 3.5 detik
        glowView?.postDelayed({
            removeGlow()
        }, 3500)
    }

    private fun removeGlow() {
        glowView?.let {
            try { windowManager.removeView(it) } catch (_: Exception) {}
        }
        glowView = null
        stopSelf()
    }

    override fun onDestroy() {
        removeGlow()
        super.onDestroy()
    }
}
