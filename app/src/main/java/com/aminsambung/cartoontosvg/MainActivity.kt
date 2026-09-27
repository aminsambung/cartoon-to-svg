package com.aminsambung.cartoontosvg

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private lateinit var imageView: ImageView
    private var originalBitmap: Bitmap? = null
    private var resultBitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 24)
            setBackgroundColor(Color.WHITE)
        }

        val title = TextView(this).apply {
            text = "Cartoon to SVG"
            textSize = 26f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }
        layout.addView(title)

        imageView = ImageView(this).apply {
            adjustViewBounds = true
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val scroll = ScrollView(this)
        scroll.addView(imageView)
        layout.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        addButton(layout, "Pilih Gambar") {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            startActivityForResult(intent, 100)
        }

        addButton(layout, "Hitam Putih") {
            originalBitmap?.let {
                resultBitmap = makeBlackWhite(it)
                imageView.setImageBitmap(resultBitmap)
            } ?: showMessage("Pilih gambar terlebih dahulu")
        }

        addButton(layout, "Simpan SVG") {
            showMessage("Fitur SVG akan ditambahkan berikutnya")
        }

        setContentView(layout)
    }

    private fun addButton(
        layout: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            setOnClickListener { action() }
        }
        layout.addView(button)
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            try {
                val uri: Uri = data.data!!
                contentResolver.openInputStream(uri).use {
                    originalBitmap = BitmapFactory.decodeStream(it)
                }
                imageView.setImageBitmap(originalBitmap)
                showMessage("Gambar berhasil dipilih")
            } catch (e: Exception) {
                showMessage("Gagal membuka gambar")
            }
        }
    }

    private fun makeBlackWhite(source: Bitmap): Bitmap {
        val width = source.width
        val height = source.height
        val output = Bitmap.createBitmap(
            width, height, Bitmap.Config.ARGB_8888
        )

        val pixels = IntArray(width * height)
        source.getPixels(
            pixels, 0, width, 0, 0, width, height
        )

        for (i in pixels.indices) {
            val color = pixels[i]
            val gray = (
                Color.red(color) * 0.299 +
                Color.green(color) * 0.587 +
                Color.blue(color) * 0.114
            ).toInt()

            pixels[i] = if (gray < 160) {
                Color.BLACK
            } else {
                Color.WHITE
            }
        }

        output.setPixels(
            pixels, 0, width, 0, 0, width, height
        )
        return output
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this, message, Toast.LENGTH_SHORT
        ).show()
    }
}
