package com.dresta0056.free.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

suspend fun compressToImagePart(
    context: Context,
    uri: Uri,
    quality: Int = 80
): MultipartBody.Part = withContext(Dispatchers.IO) {
    val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
        BitmapFactory.decodeStream(stream)
    } ?: throw IOException("Unable to decode image")

    try {
        bitmap.toMultipartBody(quality)
    } finally {
        bitmap.recycle()
    }
}

private fun Bitmap.toMultipartBody(quality: Int): MultipartBody.Part {
    val stream = ByteArrayOutputStream()
    if (!compress(Bitmap.CompressFormat.JPEG, quality, stream)) {
        throw IOException("Unable to compress image")
    }

    val byteArray = stream.toByteArray()
    val requestBody = byteArray.toRequestBody(
        contentType = "image/jpg".toMediaTypeOrNull(),
        offset = 0,
        byteCount = byteArray.size
    )
    return MultipartBody.Part.createFormData(
        name = "image",
        filename = "image.jpg",
        body = requestBody
    )
}
