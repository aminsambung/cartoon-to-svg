package com.aminsambung.cartoontosvg

import android.app.Activity
import android.os.Bundle
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

class MainActivity : Activity() {

    private lateinit var imageView: ImageView
    private var originalBitmap: Bitmap? = null
    private var resultBitmap: Bitmap? = null
    private var pendingSvg: String? = null

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
            val source = originalBitmap

            if (source == null) {
                showMessage("Pilih gambar terlebih dahulu")
            } else {
                try {
                    resultBitmap = makeBlackWhite(source)
                    imageView.setImageBitmap(resultBitmap)
                    showMessage("Gambar berhasil diubah")
                } catch (e: Exception) {
                    showMessage("Gagal mengubah gambar")
                }
            }
        }

        addButton(layout, "Simpan SVG") {
            val source = resultBitmap ?: originalBitmap

            if (source == null) {
                showMessage("Pilih gambar terlebih dahulu")
            } else {
                try {
                    val blackWhite =
                        resultBitmap ?: makeBlackWhite(source)

                    pendingSvg = bitmapToSvg(blackWhite)

                    val intent = Intent(
                        Intent.ACTION_CREATE_DOCUMENT
                    ).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/svg+xml"
                        putExtra(
                            Intent.EXTRA_TITLE,
                            "cartoon.svg"
                        )
                    }

                    startActivityForResult(intent, 101)
                } catch (e: Exception) {
                    showMessage("Gagal membuat SVG")
                }
            }
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
        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (requestCode == 100 &&
            resultCode == RESULT_OK &&
            data?.data != null
        ) {
            try {
                val uri = data.data!!

                contentResolver.openInputStream(uri).use {
                    originalBitmap =
                        BitmapFactory.decodeStream(it)
                }

                resultBitmap = null
                imageView.setImageBitmap(originalBitmap)
                showMessage("Gambar berhasil dipilih")

            } catch (e: Exception) {
                showMessage("Gagal membuka gambar")
            }
        }

        if (requestCode == 101 &&
            resultCode == RESULT_OK
        ) {
            val uri = data?.data
            val svg = pendingSvg

            if (uri != null && svg != null) {
                try {
                    contentResolver.openOutputStream(uri).use {
                        it?.write(
                            svg.toByteArray(Charsets.UTF_8)
                        )
                    }

                    showMessage("SVG berhasil disimpan")
                } catch (e: Exception) {
                    showMessage("Gagal menyimpan SVG")
                }
            }
        }
    }

    private fun makeBlackWhite(
        source: Bitmap
    ): Bitmap {
        val maxSize = 3200

        val scale = minOf(
            1f,
            maxSize.toFloat() / source.width,
            maxSize.toFloat() / source.height
        )

        val width =
            (source.width * scale).toInt().coerceAtLeast(1)
        val height =
            (source.height * scale).toInt().coerceAtLeast(1)

        val bitmap = if (
            width != source.width ||
            height != source.height
        ) {
            Bitmap.createScaledBitmap(
                source,
                width,
                height,
                true
            )
        } else {
            source
        }

        val gray = IntArray(width * height)
        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        for (i in pixels.indices) {
            val c = pixels[i]

            gray[i] = (
                Color.red(c) * 0.299 +
                Color.green(c) * 0.587 +
                Color.blue(c) * 0.114
            ).toInt()
        }

        val result = IntArray(width * height)
        java.util.Arrays.fill(result, Color.WHITE)

        // Ambang deteksi tepi yang telah dinaikkan.
        val threshold = 500

        for (y in 1 until height - 1) {
            for (x in 1 until width - 1) {
                val i = y * width + x

                val gx =
                    -gray[i - width - 1] +
                    gray[i - width + 1] -
                    2 * gray[i - 1] +
                    2 * gray[i + 1] -
                    gray[i + width - 1] +
                    gray[i + width + 1]

                val gy =
                    -gray[i - width - 1] -
                    2 * gray[i - width] -
                    gray[i - width + 1] +
                    gray[i + width - 1] +
                    2 * gray[i + width] +
                    gray[i + width + 1]

                if (abs(gx) + abs(gy) > threshold) {
                    result[i] = Color.BLACK
                }
            }
        }

        val output = Bitmap.createBitmap(
            width,
            height,
            Bitmap.Config.ARGB_8888
        )

        output.setPixels(
            result,
            0,
            width,
            0,
            0,
            width,
            height
        )

        return output
    }

    private fun bitmapToSvg(
        source: Bitmap
    ): String {
        val maxSize = 400

        val scale = minOf(
            1f,
            maxSize.toFloat() / source.width,
            maxSize.toFloat() / source.height
        )

        val width =
            (source.width * scale).toInt().coerceAtLeast(1)
        val height =
            (source.height * scale).toInt().coerceAtLeast(1)

        val bitmap = if (
            width != source.width ||
            height != source.height
        ) {
            Bitmap.createScaledBitmap(
                source,
                width,
                height,
                true
            )
        } else {
            source
        }

        val pixels = IntArray(width * height)

        bitmap.getPixels(
            pixels,
            0,
            width,
            0,
            0,
            width,
            height
        )

        val path = StringBuilder()

        for (y in 0 until height) {
            var start = -1

            for (x in 0..width) {
                val isBlack = if (x < width) {
                    Color.red(pixels[y * width + x]) < 128
                } else {
                    false
                }

                if (isBlack && start == -1) {
                    start = x
                }

                if (!isBlack && start != -1) {
                    val length = x - start

                    path.append("M")
                        .append(start)
                        .append(" ")
                        .append(y)
                        .append("h")
                        .append(length)
                        .append("v1h-")
                        .append(length)
                        .append("z")

                    start = -1
                }
            }
        }

        return """
            <?xml version="1.0" encoding="UTF-8"?>
            <svg xmlns="http://www.w3.org/2000/svg"
                 width="$width"
                 height="$height"
                 viewBox="0 0 $width $height">
                <rect
                    width="100%"
                    height="100%"
                    fill="white"/>
                <path
                    d="$path"
                    fill="black"/>
            </svg>
        """.trimIndent()
    }

    private fun showMessage(message: String) {
        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}
