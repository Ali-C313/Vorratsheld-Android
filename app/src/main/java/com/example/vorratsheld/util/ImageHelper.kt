package com.example.vorratsheld.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import kotlin.math.roundToInt

// Profilbild verkleinern, damit es als Base64 in ein Firestore-Dokument passt
object ImageHelper {

    // Lädt das Bild, verkleinert es auf maxSize px (längere Seite) und gibt JPEG-Bytes zurück
    fun compress(context: Context, uri: Uri, maxSize: Int = 256, quality: Int = 80): ByteArray {
        val resolver = context.contentResolver

        // Erst nur die Größe lesen, damit große Fotos nicht komplett in den Speicher geladen werden
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, bounds) }

        var sampleSize = 1
        while (bounds.outWidth / (sampleSize * 2) >= maxSize && bounds.outHeight / (sampleSize * 2) >= maxSize) {
            sampleSize *= 2
        }

        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }
        val bitmap = resolver.openInputStream(uri).use { BitmapFactory.decodeStream(it, null, options) }
            ?: throw IllegalArgumentException("Bild konnte nicht gelesen werden")

        val scale = minOf(1f, maxSize.toFloat() / maxOf(bitmap.width, bitmap.height))
        val scaled = Bitmap.createScaledBitmap(
            bitmap,
            (bitmap.width * scale).roundToInt().coerceAtLeast(1),
            (bitmap.height * scale).roundToInt().coerceAtLeast(1),
            true
        )

        return ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, quality, out)
            out.toByteArray()
        }
    }
}
