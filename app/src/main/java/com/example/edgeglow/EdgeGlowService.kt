package com.example.edgeglow

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.app.NotificationCompat

class EdgeGlowService : Service() {

    companion object {
        const val EXTRA_COLOR = "color"
        const val EXTRA_APP_NAME = "app_name"
        const val EXTRA_MESSAGE = "message"
        const val EXTRA_ICON_RES = "icon_res"

        private const val CHANNEL_ID = "edgeglow_channel"
        private const val NOTIF_ID = 1001
        private const val GLOW_DURATION_MS = 3500L
        private const val CARD_ANIM_MS = 300L
    }

    private lateinit var windowManager: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    private var overlayRoot: View? = null
    private var glowView: EdgeGlowView? = null
    private var cardView: LinearLayout? = null
    private var params: WindowManager.LayoutParams? = null

    private var dismissRunnable: Runnable? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
        startForeground(NOTIF_ID, buildForegroundNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val color = intent?.getIntExtra(EXTRA_COLOR, Color.CYAN) ?: Color.CYAN
        val appName = intent?.getStringExtra(EXTRA_APP_NAME).orEmpty()
        val message = intent?.getStringExtra(EXTRA_MESSAGE).orEmpty()
        val iconRes = intent?.getIntExtra(EXTRA_ICON_RES, 0) ?: 0

        showGlow(color, appName, message, iconRes)
        return START_NOT_STICKY
    }

    // ------------------------------------------------------------------
    //  TAMPILKAN OVERLAY
    // ------------------------------------------------------------------
    private fun showGlow(color: Int, appName: String, message: String, iconRes: Int) {
        // Batalkan timer dismiss sebelumnya (jika ada notifikasi baru masuk)
        dismissRunnable?.let { handler.removeCallbacks(it) }

        // Jika overlay sudah tampil, cukup restart glow + update teks
        if (overlayRoot != null) {
            glowView?.startGlow(color)
            updateCard(appName, message, iconRes)
            scheduleDismiss()
            return
        }

        try {
            val inflater = LayoutInflater.from(this)
            val root = inflater.inflate(R.layout.overlay_edge_glow, null)

            glowView = root.findViewById(R.id.edgeGlowView)
            cardView = root.findViewById(R.id.notificationCard)
            val ivAppIcon = root.findViewById<ImageView>(R.id.ivAppIcon)
            val tvAppName = root.findViewById<TextView>(R.id.tvAppName)
            val tvMessage = root.findViewById<TextView>(R.id.tvMessage)

            // Isi kartu notifikasi
            if (appName.isNotEmpty()) {
                tvAppName.text = appName
                tvMessage.text = message
                if (iconRes != 0) {
                    ivAppIcon.setImageResource(iconRes)
                }
                cardView?.visibility = View.VISIBLE
            } else {
                cardView?.visibility = View.GONE
            }

            val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE

            val lp = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                type,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                        or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
            params = lp

            windowManager.addView(root, lp)
            overlayRoot = root

            // Mulai animasi glow
            glowView?.startGlow(color)

            // Animasi masuk kartu notifikasi
            cardView?.let { card ->
                if (card.visibility == View.VISIBLE) {
                    card.alpha = 0f
                    card.translationY = -40f
                    card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(CARD_ANIM_MS)
                        .start()
                }
            }

            scheduleDismiss()

        } catch (e: Exception) {
            e.printStackTrace()
            cleanupOverlay()
            stopSelf()
        }
    }

    private fun updateCard(appName: String, message: String, iconRes: Int) {
        val card = cardView ?: return
        if (appName.isEmpty()) return

        val tvAppName = overlayRoot?.findViewById<TextView>(R.id.tvAppName)
        val tvMessage = overlayRoot?.findViewById<TextView>(R.id.tvMessage)
        val ivAppIcon = overlayRoot?.findViewById<ImageView>(R.id.ivAppIcon)

        tvAppName?.text = appName
        tvMessage?.text = message
        if (iconRes != 0) ivAppIcon?.setImageResource(iconRes)

        if (card.visibility != View.VISIBLE) {
            card.visibility = View.VISIBLE
            card.alpha = 0f
            card.translationY = -40f
        }
        card.animate().alpha(1f).translationY(0f).setDuration(CARD_ANIM_MS).start()
    }

    // ------------------------------------------------------------------
    //  JADWALKAN DISMISS
    // ------------------------------------------------------------------
    private fun scheduleDismiss() {
        dismissRunnable?.let { handler.removeCallbacks(it) }
        dismissRunnable = Runnable { dismissAnimated() }
        handler.postDelayed(dismissRunnable!!, GLOW_DURATION_MS)
    }

    private fun dismissAnimated() {
        val card = cardView
        val root = overlayRoot
        if (root == null) {
            stopSelf()
            return
        }

        if (card != null && card.visibility == View.VISIBLE) {
            card.animate()
                .alpha(0f)
                .translationY(-40f)
                .setDuration(CARD_ANIM_MS)
                .withEndAction {
                    cleanupOverlay()
                    stopSelf()
                }
                .start()
        } else {
            cleanupOverlay()
            stopSelf()
        }
    }

    // ------------------------------------------------------------------
    //  BERSIHKAN OVERLAY
    // ------------------------------------------------------------------
    private fun cleanupOverlay() {
        dismissRunnable?.let { handler.removeCallbacks(it) }
        dismissRunnable = null

        glowView?.stopGlow()
        glowView = null
        cardView = null

        overlayRoot?.let { root ->
            try {
                windowManager.removeView(root)
            } catch (_: IllegalArgumentException) {
                // View sudah tidak terpasang, abaikan
            } catch (_: Exception) {
                // Abaikan error lain
            }
        }
        overlayRoot = null
        params = null
    }

    // ------------------------------------------------------------------
    //  FOREGROUND NOTIFICATION
    // ------------------------------------------------------------------
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "EdgeGlow Service",
                NotificationManager.IMPORTANCE_MIN
            ).apply {
                description = "Menjaga efek cahaya tepi layar tetap aktif"
                setShowBadge(false)
            }
            val nm = getSystemService(NotificationManager::class.java)
            nm.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("EdgeGlow aktif")
            .setContentText("Efek cahaya tepi layar berjalan")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    // ------------------------------------------------------------------
    //  LIFECYCLE
    // ------------------------------------------------------------------
    override fun onDestroy() {
        cleanupOverlay()
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
