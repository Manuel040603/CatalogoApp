package com.catalogoapp.consultoras.util

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream

fun bitmapABase64(bitmap: Bitmap, lado: Int = 300, calidad: Int = 70): String {
    val redimensionado = Bitmap.createScaledBitmap(bitmap, lado, lado, true)
    val outputStream = ByteArrayOutputStream()
    redimensionado.compress(Bitmap.CompressFormat.JPEG, calidad, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

fun base64ABitmap(base64: String): Bitmap? {
    return try {
        val bytes = Base64.decode(base64, Base64.DEFAULT)
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    } catch (e: Exception) {
        null
    }
}
