package com.example.edgeglow

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.view.View
import android.view.animation.LinearInterpolator

class EdgeGlowView(context: Context) : View(context) {

    // Warna default diubah menjadi MERAH terang
    private var glowColor = Color.RED 
    
    // Ketebalan diperbesar
    private var strokeWidth = 50f 
    private var alphaValue = 0
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@EdgeGlowView.strokeWidth
    }

    private val rect = RectF()
    private var animator: ValueAnimator? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        
        if (width == 0 || height == 0) return

        // Area gambar
        rect.set(
            strokeWidth / 2,
            strokeWidth / 2,
            width - strokeWidth / 2,
            height - strokeWidth / 2
        )

        // Gambar 3 lapis efek glow
        for (i in 3 downTo 1) {
            paint.color = glowColor
            paint.alpha = (alphaValue / i) 
            paint.strokeWidth = strokeWidth * i
            paint.maskFilter = BlurMaskFilter(strokeWidth * i, BlurMaskFilter.Blur.SOLID)
            canvas.drawRoundRect(rect, 60f, 60f, paint)
        }
    }

    fun startGlow(color: Int = Color.RED, duration: Long = 4000) {
        glowColor = color
        animator?.cancel()
        
        animator = ValueAnimator.ofInt(0, 255, 255, 0).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener {
                alphaValue = it.animatedValue as Int
                invalidate() 
            }
            start()
        }
    }

    fun stopGlow() {
        animator?.cancel()
        alphaValue = 0
        invalidate()
    }
}
